package de.fips.util.tinyargs;

import static org.fest.assertions.Assertions.assertThat;
import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Test;

import de.fips.util.tinyargs.CommandLineApplicationParser;
import de.fips.util.tinyargs.exception.IllegalOptionValueException;
import de.fips.util.tinyargs.exception.UnknownOptionException;

public class CommandLineApplicationParserTest {
	CommandLineApplicationParser<TestApp> app;
	private TestPrintStream console;

	@Before
	public void setUp() {
		console = new TestPrintStream();
		app = CommandLineApplicationParser.of(TestApp.class);
		app.setPrintStream(console);
	}

	@Test
	public void testParseFillsObject() throws Exception {
		final TestApp object = app.parse(new String[] { "-s", "100", "--text", "Hello World" });
		assertThat(object.getSize()).isEqualTo(100);
		assertThat(object.text).isEqualTo("Hello World");
		assertThat(object.f).isEqualTo(1.13f);
	}

	@Test
	public void testHelpString() throws Exception {
		app.parse(new String[] { "-h" });
		assertThat(app.helpRequested()).isTrue();

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
			app.parse(new String[] { invalidOption });
			fail();
		} catch (final UnknownOptionException e) {
			assertThat(e.getOptionName()).isEqualTo(invalidOption);
		}
		assertThat(app.helpRequested()).isFalse();

		final String ls = System.getProperty("line.separator");
		String expected = //
			"Illegal option: 'i' in '-invalidOption'" + ls + ls + //
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
		app.parse(new String[] { "--size", "string" });
	}

	@Test(expected = IllegalOptionValueException.class)
	public void testParseValidatorInterval() throws Exception {
		app.parse(new String[] { "-s", "1000" });
	}

	@Test(expected = IllegalOptionValueException.class)
	public void testParseValidatorValueSet() throws Exception {
		app.parse(new String[] { "--text", "invalidValue" });
	}
}
