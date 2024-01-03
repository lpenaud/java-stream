package com.lpenaud.streams.example;

import java.io.FileInputStream;
import java.io.IOException;

import com.lpenaud.streams.readable.BytesReadableStream;
import com.lpenaud.streams.transform.TextDecoder;
import com.lpenaud.streams.transform.TextEncoder;
import com.lpenaud.streams.writable.BytesWritableStream;

public class TextDecoderExample {

    public static void main(final String[] args) throws IOException {
        try (final var stream = BytesReadableStream.withDefaultBuflen(new FileInputStream(args[0]))) {
            stream.pipeThrough(TextDecoder.utf8())
                            .pipeThrough(TextEncoder.defaultCharset())
                            .pipeTo(new BytesWritableStream(System.out));
        }
    }
}
