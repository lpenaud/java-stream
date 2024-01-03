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
import com.lpenaud.streams.readable.ReadbleStream;

public class TextEncoder implements TransformStream<CharBuffer, ByteBuffer> {

    private static class TextEncoderReadableStream implements ReadbleStream<ByteBuffer> {

        private final CharsetEncoder encoder;

        private final ByteBuffer buffer;

        private final ReadbleStream<CharBuffer> source;

        private CharBuffer charBuffer = null;

        private boolean flushOverflow = false;

        private TextEncoderReadableStream(final TextEncoder textEncoder, final ReadbleStream<CharBuffer> source) {
            this.encoder = textEncoder.charset.newEncoder();
            this.buffer = ByteBuffer.allocate(textEncoder.bufferLength);
            this.source = Objects.requireNonNull(source);
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
            CoderResult result = CoderResult.UNDERFLOW;
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

    public static TextEncoder iso88591() {
        return new TextEncoder(StandardCharsets.ISO_8859_1);
    }

    public static TextEncoder usAscii() {
        return new TextEncoder(StandardCharsets.US_ASCII);
    }

    public static TextEncoder utf16Be() {
        return new TextEncoder(StandardCharsets.UTF_16BE);
    }

    public static TextEncoder utf16Le() {
        return new TextEncoder(StandardCharsets.UTF_16LE);
    }

    public static TextEncoder utf16() {
        return new TextEncoder(StandardCharsets.UTF_16);
    }

    public static TextEncoder utf8() {
        return new TextEncoder(StandardCharsets.UTF_8);
    }

    public static TextEncoder defaultCharset() {
        return new TextEncoder(Charset.defaultCharset());
    }

    public static TextEncoder charset(final Charset charset) {
        return new TextEncoder(charset);
    }

    public static TextEncoder charset(final String charsetName) {
        return TextEncoder.charset(Charset.forName(charsetName));
    }

    private TextEncoder(final Charset charset) {
        this(charset, StreamsUtils.DEFAULT_BUFLEN);
    }

    private TextEncoder(final Charset charset, final int bufferLength) {
        this.charset = Objects.requireNonNull(charset);
        this.bufferLength = bufferLength;
    }

    @Override
    public ReadbleStream<ByteBuffer> readableStream(final ReadbleStream<CharBuffer> readbleStream) {
        return new TextEncoderReadableStream(this, readbleStream);
    }

}
