package de.fips.util.tinyargs;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class TestPrintStream extends PrintStream {
	private final static ByteArrayOutputStream baos = new ByteArrayOutputStream();

	public TestPrintStream() {
		super(baos);
		baos.reset();
	}

	@Override
	public String toString() {
		return baos.toString();
	}
}
