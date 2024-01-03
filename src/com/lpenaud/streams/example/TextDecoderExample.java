package com.lpenaud.streams.example;

import java.io.FileInputStream;
import java.io.IOException;

import com.lpenaud.streams.readable.BytesReadableStream;
import com.lpenaud.streams.transform.TextDecoderStream;
import com.lpenaud.streams.transform.TextEncoderStream;
import com.lpenaud.streams.writable.BytesWritableStream;

public class TextDecoderExample {

	public static void main(final String[] args) throws IOException {
		System.exit(TextDecoderExample.mainImpl(args));
	}

	private static int mainImpl(final String[] args) {
		if (args.length < 1) {
			System.err.format("Usage: %s INFILE%n", TextDecoderExample.class.getName());
			return 1;
		}
		try (final var stream = BytesReadableStream.withDefaultBuflen(new FileInputStream(args[0]))) {
			stream.pipeThrough(TextDecoderStream.utf8()).pipeThrough(TextEncoderStream.defaultCharset())
			.pipeTo(new BytesWritableStream(System.out));
		} catch (final IOException e) {
			e.printStackTrace();
			return 2;
		}
		return 0;
	}
}
