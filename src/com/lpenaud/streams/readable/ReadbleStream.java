package com.lpenaud.streams.readable;

import java.io.Closeable;
import java.io.IOException;

import com.lpenaud.streams.transform.TransformStream;
import com.lpenaud.streams.writable.WritableStream;

public interface ReadbleStream<T> extends Closeable {

    default <U> ReadbleStream<U> pipeThrough(final TransformStream<T, U> transformStream) throws IOException {
        return transformStream.readableStream(this);
    }

    default void pipeTo(final WritableStream<T> writableStream) throws IOException {
        try (writableStream; this) {
            T chunk;
            while ((chunk = this.read()) != null) {
                writableStream.write(chunk);
            }
        }
    }

    T read() throws IOException;
}
