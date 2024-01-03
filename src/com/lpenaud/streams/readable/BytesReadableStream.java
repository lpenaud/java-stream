package com.lpenaud.streams.readable;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import com.lpenaud.streams.StreamsUtils;

public class BytesReadableStream implements ReadableStream<ByteBuffer> {

    private final InputStream source;

    private final byte[] buffer;

    public static BytesReadableStream withDefaultBuflen(final InputStream source) {
        return new BytesReadableStream(source, StreamsUtils.DEFAULT_BUFLEN);
    }

    private BytesReadableStream(final InputStream source, final int buflen) {
        this.source = source;
        this.buffer = new byte[buflen];
    }

    @Override
    public void close() throws IOException {
        this.source.close();
    }

    @Override
    public ByteBuffer read() throws IOException {
        final var len = this.source.read(this.buffer, 0, this.buffer.length);
        if (len == -1) {
            return null;
        }
        return ByteBuffer.wrap(this.buffer, 0, len);
    }

}
