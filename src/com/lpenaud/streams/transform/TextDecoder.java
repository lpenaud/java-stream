package com.lpenaud.streams.transform;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import com.lpenaud.streams.readable.ReadbleStream;

public class TextDecoder implements TransformStream<ByteBuffer, CharBuffer> {

    private final Charset charset;

    public static TextDecoder utf8() {
        return new TextDecoder(StandardCharsets.UTF_8);
    }

    public static TextDecoder defaultCharset() {
        return new TextDecoder(Charset.defaultCharset());
    }

    public static TextDecoder charset(final Charset charset) {
        return new TextDecoder(charset);
    }

    private TextDecoder(final Charset charset) {
        this.charset = Objects.requireNonNull(charset);
    }

    @Override
    public ReadbleStream<CharBuffer> readableStream(final ReadbleStream<ByteBuffer> readbleStream) {
        // TODO: Create implementation similar to TextEncoder
        return new ReadbleStream<>() {

            @Override
            public void close() throws IOException {
                readbleStream.close();
            }

            @Override
            public CharBuffer read() throws IOException {
                final var buf = readbleStream.read();
                if (buf == null) {
                    return null;
                }
                return TextDecoder.this.charset.decode(buf);
            }
        };
    }
}
