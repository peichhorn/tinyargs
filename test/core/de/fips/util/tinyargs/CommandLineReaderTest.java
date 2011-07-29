package de.fips.util.tinyargs;

import static org.fest.assertions.Assertions.assertThat;
import static org.junit.Assert.fail;

import org.junit.Rule;
import org.junit.Test;

import de.fips.util.tinyargs.CommandLineReader;
import de.fips.util.tinyargs.exception.UnknownOptionException;
import de.fips.util.tinyargs.junit.ExpectedException;
import de.fips.util.tinyargs.junit.Std;

public class CommandLineReaderTest {
	@Rule
	public final Std stdErr = Std.err();
	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Test
	public void testParseFillsObject() throws Exception {
		// setup
		final CommandLineReader<TestApp> reader = CommandLineReader.of(TestApp.class);
		// run
		final TestApp object = reader.read(new String[] { "-s", "100", "--text", "Hello World" });
		// assert
		assertThat(object.getSize()).isEqualTo(100);
		assertThat(object.text).isEqualTo("Hello World");
		assertThat(object.f).isEqualTo(1.13f);
	}

	@Test
	public void testHelpString() throws Exception {
		// setup
		final CommandLineReader<TestApp> reader = CommandLineReader.of(TestApp.class);
		final String ls = System.getProperty("line.separator");
		final String expected = //
			"usage: TestApp [options]" + ls + //
			"options:" + ls + //
			"\t--d" + ls + //
			"\t -h,--help: display help" + ls + //
			"\t -s,--size: a fancy size value; interval [0, 100]" + ls + //
			"\t--text; allowed values [Hello World, foo]" + ls;
		// run
		reader.read(new String[] { "-h" });
		// assert
		assertThat(reader.helpRequested()).isTrue();
		assertThat(stdErr.getContent()).contains(expected);
	}

	@Test
	public void testHelpStringOnException() throws Exception {
		// setup
		final CommandLineReader<TestApp> reader = CommandLineReader.of(TestApp.class);
		final String invalidOption = "-invalidOption";
		final String ls = System.getProperty("line.separator");
		final String expected = //
			"Illegal option 'i' in '-invalidOption'" + ls + ls + //
			"usage: TestApp [options]" + ls + //
			"options:" + ls + //
			"\t--d" + ls + //
			"\t -h,--help: display help" + ls + //
			"\t -s,--size: a fancy size value; interval [0, 100]" + ls + //
			"\t--text; allowed values [Hello World, foo]" + ls;

		try {
			// run
			reader.read(new String[] { invalidOption });
			fail();
		} catch (final UnknownOptionException e) {
			// assert
			assertThat(e.getOptionName()).isEqualTo(invalidOption);
			assertThat(reader.helpRequested()).isFalse();
			assertThat(stdErr.getContent()).contains(expected);
		}
	}

	@Test
	public void testParseValidatorWrongType() throws Exception {
		// setup
		final CommandLineReader<TestApp> reader = CommandLineReader.of(TestApp.class);
		// run + assert
		thrown.expectIllegalOptionValueException("Illegal value 'string' for option -s/--size");
		reader.read(new String[] { "--size", "string" });
	}

	@Test
	public void testParseValidatorInterval() throws Exception {
		// setup
		final CommandLineReader<TestApp> reader = CommandLineReader.of(TestApp.class);
		// run + assert
		thrown.expectIllegalOptionValueException("Illegal value '1000' for option -s/--size");
		reader.read(new String[] { "-s", "1000" });
	}

	@Test
	public void testParseValidatorValueSet() throws Exception {
		// setup
		final CommandLineReader<TestApp> reader = CommandLineReader.of(TestApp.class);
		// run + assert
		thrown.expectIllegalOptionValueException("Illegal value 'invalidValue' for option --text");
		reader.read(new String[] { "--text", "invalidValue" });
	}
}
