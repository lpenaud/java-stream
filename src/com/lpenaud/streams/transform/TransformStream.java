package com.lpenaud.streams.transform;

import com.lpenaud.streams.readable.ReadableStream;

public interface TransformStream<T, U> {

    ReadableStream<U> readableStream(ReadableStream<T> readbleStream);
}
