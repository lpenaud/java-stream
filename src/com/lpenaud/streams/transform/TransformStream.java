package com.lpenaud.streams.transform;

import com.lpenaud.streams.readable.ReadbleStream;

public interface TransformStream<T, U> {

    ReadbleStream<U> readableStream(ReadbleStream<T> readbleStream);
}
