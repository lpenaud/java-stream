package com.lpenaud.streams.writable;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

import com.lpenaud.streams.StreamsUtils;

public class BytesWritableStream implements WritableStream<ByteBuffer> {

    private final OutputStream source;

    private final byte[] buffer;

    public BytesWritableStream(final OutputStream outputStream) {
        this(outputStream, StreamsUtils.DEFAULT_BUFLEN);
    }

    public BytesWritableStream(final OutputStream outputStream, final int buflen) {
        this.source = outputStream;
        this.buffer = new byte[buflen];
    }

    @Override
    public void close() throws IOException {
        this.source.close();
    }

    @Override
    public void write(final ByteBuffer chunk) throws IOException {
        int remaining;
        while ((remaining = chunk.remaining()) > this.buffer.length) {
            chunk.get(this.buffer);
            this.source.write(this.buffer);
        }
        if (remaining > 0) {
            chunk.get(this.buffer, 0, remaining);
            this.source.write(this.buffer, 0, remaining);
        }
    }
}
