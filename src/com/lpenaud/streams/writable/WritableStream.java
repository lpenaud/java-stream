package com.lpenaud.streams.writable;

import java.io.Closeable;
import java.io.IOException;

public interface WritableStream<T> extends Closeable {

    void write(T chunk) throws IOException;

}
