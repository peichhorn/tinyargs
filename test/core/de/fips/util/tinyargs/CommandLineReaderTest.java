package de.fips.util.tinyargs;

import static org.fest.assertions.Assertions.assertThat;
import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Test;

import de.fips.util.tinyargs.CommandLineReader;
import de.fips.util.tinyargs.exception.IllegalOptionValueException;
import de.fips.util.tinyargs.exception.UnknownOptionException;

public class CommandLineReaderTest {
	CommandLineReader<TestApp> reader;
	private TestPrintStream console;

	@Before
	public void setUp() {
		console = new TestPrintStream();
		reader = CommandLineReader.of(TestApp.class);
		reader.setPrintStream(console);
	}

	@Test
	public void testParseFillsObject() throws Exception {
		final TestApp object = reader.read(new String[] { "-s", "100", "--text", "Hello World" });
		assertThat(object.getSize()).isEqualTo(100);
		assertThat(object.text).isEqualTo("Hello World");
		assertThat(object.f).isEqualTo(1.13f);
	}

	@Test
	public void testHelpString() throws Exception {
		reader.read(new String[] { "-h" });
		assertThat(reader.helpRequested()).isTrue();

		final String ls = System.getProperty("line.separator");
		String expected = //
			"usage: TestApp [options]" + ls + //
			"options:" + ls + //
			"\t--d" + ls + //
			"\t -h,--help: display help" + ls + //
			"\t -s,--size: a fancy size value; interval [0, 100]" + ls + //
			"\t--text; allowed values [Hello World, foo]" + ls;
		assertThat(console.toString()).isEqualTo(expected);
	}

	@Test
	public void testHelpStringOnException() throws Exception {
		final String invalidOption = "-invalidOption";
		try {
			reader.read(new String[] { invalidOption });
			fail();
		} catch (final UnknownOptionException e) {
			assertThat(e.getOptionName()).isEqualTo(invalidOption);
		}
		assertThat(reader.helpRequested()).isFalse();

		final String ls = System.getProperty("line.separator");
		String expected = //
			"Illegal option 'i' in '-invalidOption'" + ls + ls + //
			"usage: TestApp [options]" + ls + //
			"options:" + ls + //
			"\t--d" + ls + //
			"\t -h,--help: display help" + ls + //
			"\t -s,--size: a fancy size value; interval [0, 100]" + ls + //
			"\t--text; allowed values [Hello World, foo]" + ls;
		assertThat(console.toString()).isEqualTo(expected);
	}

	@Test(expected = IllegalOptionValueException.class)
	public void testParseValidatorWrongType() throws Exception {
		reader.read(new String[] { "--size", "string" });
	}

	@Test(expected = IllegalOptionValueException.class)
	public void testParseValidatorInterval() throws Exception {
		reader.read(new String[] { "-s", "1000" });
	}

	@Test(expected = IllegalOptionValueException.class)
	public void testParseValidatorValueSet() throws Exception {
		reader.read(new String[] { "--text", "invalidValue" });
	}
}
