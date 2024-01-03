package com.lpenaud.streams.transform;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CoderResult;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import com.lpenaud.streams.StreamsUtils;
import com.lpenaud.streams.readable.ReadableStream;

public class TextDecoderStream implements TransformStream<ByteBuffer, CharBuffer> {

	private static class TextDecoderReadableStream implements ReadableStream<CharBuffer> {

		private final CharsetDecoder decoder;

		private final CharBuffer buffer;

		private final ReadableStream<ByteBuffer> source;

		private ByteBuffer byteBuffer;

		private boolean flushOverflow;

		private TextDecoderReadableStream(final TextDecoderStream textDecoder,
				final ReadableStream<ByteBuffer> source) {
			this.decoder = textDecoder.charset.newDecoder();
			this.buffer = CharBuffer.allocate(textDecoder.bufferLength);
			this.source = Objects.requireNonNull(source);
			this.byteBuffer = null;
			this.flushOverflow = false;
		}

		@Override
		public void close() throws IOException {
			this.source.close();
		}

		@Override
		public CharBuffer read() throws IOException {
			if (this.byteBuffer == null) {
				this.byteBuffer = this.source.read();
				if (this.byteBuffer == null) {
					return null;
				}
			}
			this.buffer.position(0);
			var result = CoderResult.UNDERFLOW;
			if (this.flushOverflow) {
				result = this.flush();
			}
			while (!result.isOverflow()) {
				result = this.decoder.decode(this.byteBuffer, this.buffer,
						this.byteBuffer.remaining() <= this.buffer.capacity());
				if (result.isError()) {
					result.throwException();
				}
				if (result.isUnderflow()) {
					this.flush();
					this.byteBuffer = this.source.read();
					if (this.byteBuffer == null) {
						break;
					}
				}
			}
			return this.slice();
		}

		private CoderResult flush() {
			final var result = this.decoder.flush(this.buffer);
			this.flushOverflow = result.isOverflow();
			if (!this.flushOverflow) {
				this.decoder.reset();
				this.byteBuffer = null;
			}
			return result;
		}

		private CharBuffer slice() {
			return this.buffer.slice(0, this.buffer.position());
		}

	}

	private final Charset charset;

	private final int bufferLength;

	public static TextDecoderStream iso88591() {
		return new TextDecoderStream(StandardCharsets.ISO_8859_1);
	}

	public static TextDecoderStream iso88591(final int bufferLength) {
		return new TextDecoderStream(StandardCharsets.ISO_8859_1, bufferLength);
	}

	public static TextDecoderStream usAscii() {
		return new TextDecoderStream(StandardCharsets.US_ASCII);
	}

	public static TextDecoderStream usAscii(final int bufferLength) {
		return new TextDecoderStream(StandardCharsets.US_ASCII, bufferLength);
	}

	public static TextDecoderStream utf16Be() {
		return new TextDecoderStream(StandardCharsets.UTF_16BE);
	}

	public static TextDecoderStream utf16Be(final int bufferLength) {
		return new TextDecoderStream(StandardCharsets.UTF_16BE, bufferLength);
	}

	public static TextDecoderStream utf16Le(final int bufferLength) {
		return new TextDecoderStream(StandardCharsets.UTF_16LE);
	}

	public static TextDecoderStream utf16Le() {
		return new TextDecoderStream(StandardCharsets.UTF_16LE);
	}

	public static TextDecoderStream utf8() {
		return new TextDecoderStream(StandardCharsets.UTF_8);
	}

	public static TextDecoderStream utf8(final int bufferLength) {
		return new TextDecoderStream(StandardCharsets.UTF_8);
	}

	public static TextDecoderStream defaultCharset() {
		return new TextDecoderStream(Charset.defaultCharset());
	}

	public static TextDecoderStream defaultCharset(final int bufferLength) {
		return new TextDecoderStream(Charset.defaultCharset());
	}

	public static TextDecoderStream charset(final Charset charset) {
		return new TextDecoderStream(charset);
	}

	public static TextDecoderStream charset(final Charset charset, final int bufferLength) {
		return new TextDecoderStream(charset);
	}

	public static TextDecoderStream charset(final String charsetName) {
		return new TextDecoderStream(Charset.forName(charsetName));
	}

	public static TextDecoderStream charset(final String charsetName, final int bufferLength) {
		return new TextDecoderStream(Charset.forName(charsetName), bufferLength);
	}

	private TextDecoderStream(final Charset charset) {
		this(charset, StreamsUtils.DEFAULT_BUFLEN);
	}

	private TextDecoderStream(final Charset charset, final int bufferLength) {
		this.charset = Objects.requireNonNull(charset);
		this.bufferLength = bufferLength;
	}

	@Override
	public ReadableStream<CharBuffer> readableStream(final ReadableStream<ByteBuffer> readbleStream) {
		return new TextDecoderReadableStream(this, readbleStream);
	}
}
