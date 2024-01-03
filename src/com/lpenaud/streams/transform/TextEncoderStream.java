package com.lpenaud.streams.transform;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import com.lpenaud.streams.StreamsUtils;
import com.lpenaud.streams.readable.ReadableStream;

public class TextEncoderStream implements TransformStream<CharBuffer, ByteBuffer> {

	private static class TextEncoderReadableStream implements ReadableStream<ByteBuffer> {

		private final CharsetEncoder encoder;

		private final ByteBuffer buffer;

		private final ReadableStream<CharBuffer> source;

		private CharBuffer charBuffer;

		private boolean flushOverflow;

		private TextEncoderReadableStream(final TextEncoderStream textEncoder, final ReadableStream<CharBuffer> source) {
			this.encoder = textEncoder.charset.newEncoder();
			this.buffer = ByteBuffer.allocate(textEncoder.bufferLength);
			this.source = Objects.requireNonNull(source);
			this.charBuffer = null;
			this.flushOverflow = false;
		}

		@Override
		public void close() throws IOException {
			this.source.close();
		}

		@Override
		public ByteBuffer read() throws IOException {
			if (this.charBuffer == null) {
				this.charBuffer = this.source.read();
				if (this.charBuffer == null) {
					return null;
				}
			}
			this.buffer.position(0);
			var result = CoderResult.UNDERFLOW;
			if (this.flushOverflow) {
				result = this.flush();
			}
			while (!result.isOverflow()) {
				result = this.encoder.encode(this.charBuffer, this.buffer, this.charBuffer.remaining() <= this.buffer.capacity());
				if (result.isError()) {
					result.throwException();
				}
				if (result.isUnderflow()) {
					this.flush();
					this.charBuffer = this.source.read();
					if (this.charBuffer == null) {
						break;
					}
				}
			}
			return this.slice();
		}

		private CoderResult flush() {
			final var result = this.encoder.flush(this.buffer);
			this.flushOverflow = result.isOverflow();
			if (!this.flushOverflow) {
				this.encoder.reset();
				this.charBuffer = null;
			}
			return result;
		}

		private ByteBuffer slice() {
			return this.buffer.slice(0, this.buffer.position());
		}
	}

	private final Charset charset;

	private final int bufferLength;

	public static TextEncoderStream iso88591() {
		return new TextEncoderStream(StandardCharsets.ISO_8859_1);
	}

	public static TextEncoderStream iso88591(final int bufferLength) {
		return new TextEncoderStream(StandardCharsets.ISO_8859_1, bufferLength);
	}

	public static TextEncoderStream usAscii() {
		return new TextEncoderStream(StandardCharsets.US_ASCII);
	}

	public static TextEncoderStream usAscii(final int bufferLength) {
		return new TextEncoderStream(StandardCharsets.US_ASCII, bufferLength);
	}

	public static TextEncoderStream utf16Be() {
		return new TextEncoderStream(StandardCharsets.UTF_16BE);
	}

	public static TextEncoderStream utf16Be(final int bufferLength) {
		return new TextEncoderStream(StandardCharsets.UTF_16BE, bufferLength);
	}

	public static TextEncoderStream utf16Le() {
		return new TextEncoderStream(StandardCharsets.UTF_16LE);
	}

	public static TextEncoderStream utf16Le(final int bufferLength) {
		return new TextEncoderStream(StandardCharsets.UTF_16LE, bufferLength);
	}

	public static TextEncoderStream utf16() {
		return new TextEncoderStream(StandardCharsets.UTF_16);
	}

	public static TextEncoderStream utf16(final int bufferLength) {
		return new TextEncoderStream(StandardCharsets.UTF_16, bufferLength);
	}

	public static TextEncoderStream utf8() {
		return new TextEncoderStream(StandardCharsets.UTF_8);
	}

	public static TextEncoderStream utf8(final int bufferLength) {
		return new TextEncoderStream(StandardCharsets.UTF_8, bufferLength);
	}

	public static TextEncoderStream defaultCharset() {
		return new TextEncoderStream(Charset.defaultCharset());
	}

	public static TextEncoderStream defaultCharset(final int bufferLength) {
		return new TextEncoderStream(Charset.defaultCharset(), bufferLength);
	}

	public static TextEncoderStream charset(final Charset charset) {
		return new TextEncoderStream(charset);
	}

	public static TextEncoderStream charset(final Charset charset, final int bufferLength) {
		return new TextEncoderStream(charset, bufferLength);
	}

	public static TextEncoderStream charset(final String charsetName) {
		return TextEncoderStream.charset(Charset.forName(charsetName));
	}

	public static TextEncoderStream charset(final String charsetName, final int bufferLength) {
		return new TextEncoderStream(Charset.forName(charsetName), bufferLength);
	}

	private TextEncoderStream(final Charset charset) {
		this(charset, StreamsUtils.DEFAULT_BUFLEN);
	}

	private TextEncoderStream(final Charset charset, final int bufferLength) {
		this.charset = Objects.requireNonNull(charset);
		this.bufferLength = bufferLength;
	}

	@Override
	public ReadableStream<ByteBuffer> readableStream(final ReadableStream<CharBuffer> readbleStream) {
		return new TextEncoderReadableStream(this, readbleStream);
	}

}
