package de.fips.util.tinyargs;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.Locale;

import org.junit.Before;
import org.junit.Test;

import de.fips.util.tinyargs.CommandLineParser;
import de.fips.util.tinyargs.exception.IllegalOptionValueException;
import de.fips.util.tinyargs.exception.UnknownOptionException;
import de.fips.util.tinyargs.option.AbstractOption;
import de.fips.util.tinyargs.option.BooleanOption;
import de.fips.util.tinyargs.option.DoubleOption;
import de.fips.util.tinyargs.option.IntegerOption;
import de.fips.util.tinyargs.option.LongOption;
import de.fips.util.tinyargs.option.StringOption;
import de.fips.util.tinyargs.validator.IntervalValidator;

public class CommandLineParserTest {
	private CommandLineParser parser;
	private TestPrintStream console;

	@Before
	public void setUp() {
		parser = spy(new CommandLineParser());
		console = new TestPrintStream();
		parser.setPrintStream(console);
	}

	@Test
	public void testParseFillsOptions() throws Exception {
		final AbstractOption<Integer> size = parser.addOption(new IntegerOption('s', "size", "size"));
		assertThat(parser.getOptionValue(size)).isNull();
		parser.parse(new String[] { "--size=100" }, Locale.US);
		assertThat(parser.getOptionValue(size)).isEqualTo(100);
	}

	@Test
	public void testStandardOptions() throws Exception {
		final AbstractOption<Boolean> verbose = parser.addOption(new BooleanOption('v', "verbose", "forces verbose execution"));
		final AbstractOption<Integer> size = parser.addOption(new IntegerOption('s', "size", ""));
		final AbstractOption<String> name = parser.addOption(new StringOption('n', "name", ""));
		final AbstractOption<Double> fraction = parser.addOption(new DoubleOption('f', "fraction", ""));
		final AbstractOption<Boolean> missing = parser.addOption(new BooleanOption('m', "missing", ""));
		final AbstractOption<Boolean> careful = parser.addOption(new BooleanOption("careful", ""));
		final AbstractOption<Long> bignum = parser.addOption(new LongOption('b', "bignum", ""));

		final Long longValue = Long.valueOf(Long.valueOf(Integer.MAX_VALUE) + 1);
		parser.parse(new String[] { "-v", "--size=100", "-b", longValue.toString(), "-n", "foo", "-f", "0.1", "rest" }, Locale.US);

		assertThat(parser.getOptionValue(verbose)).isTrue();
		assertThat(parser.getOptionValue(size)).isEqualTo(100);
		assertThat(parser.getOptionValue(name)).isEqualTo("foo");
		assertThat(parser.getOptionValue(fraction)).isEqualTo(0.1);
		assertThat(parser.getOptionValue(missing)).isNull();
		assertThat(parser.getOptionValue(careful)).isNull();
		assertThat(parser.getOptionValue(bignum)).isEqualTo(longValue);
		assertThat(parser.getRemainingArgs()).containsExactly("rest");
	}

	@Test
	public void testDefaults() throws Exception {
		final AbstractOption<Boolean> boolean1 = parser.addOption(new BooleanOption("boolean1", ""));
		final AbstractOption<Boolean> boolean2 = parser.addOption(new BooleanOption("boolean2", ""));
		final AbstractOption<Boolean> boolean3 = parser.addOption(new BooleanOption("boolean3", ""));
		final AbstractOption<Boolean> boolean4 = parser.addOption(new BooleanOption("boolean4", ""));
		final AbstractOption<Boolean> boolean5 = parser.addOption(new BooleanOption("boolean5", ""));

		final AbstractOption<Integer> int1 = parser.addOption(new IntegerOption("int1", ""));
		final AbstractOption<Integer> int2 = parser.addOption(new IntegerOption("int2", ""));
		final AbstractOption<Integer> int3 = parser.addOption(new IntegerOption("int3", ""));
		final AbstractOption<Integer> int4 = parser.addOption(new IntegerOption("int4", ""));

		final AbstractOption<String> string1 = parser.addOption(new StringOption("string1", ""));
		final AbstractOption<String> string2 = parser.addOption(new StringOption("string2", ""));
		final AbstractOption<String> string3 = parser.addOption(new StringOption("string3", ""));
		final AbstractOption<String> string4 = parser.addOption(new StringOption("string4", ""));

		parser.parse(new String[] { "--boolean1", "--boolean2", "--int1=42", "--int2=42", "--string1=Hello", "--string2=Hello", });

		assertThat(parser.getOptionValue(boolean1)).isTrue();
		assertThat(parser.getOptionValue(boolean2, Boolean.FALSE)).isTrue();
		assertThat(parser.getOptionValue(boolean3)).isNull();
		assertThat(parser.getOptionValue(boolean4, Boolean.FALSE)).isFalse();
		assertThat(parser.getOptionValue(boolean5, Boolean.TRUE)).isTrue();

		final Integer fortyTwo = Integer.valueOf(42);
		final Integer thirtySix = Integer.valueOf(36);

		
		assertThat(parser.getOptionValue(int1)).isEqualTo(fortyTwo);
		assertThat(parser.getOptionValue(int2, thirtySix)).isEqualTo(fortyTwo);
		assertThat(parser.getOptionValue(int3)).isNull();
		assertThat(parser.getOptionValue(int4, thirtySix)).isEqualTo(thirtySix);

		assertThat(parser.getOptionValue(string1)).isEqualTo("Hello");
		assertThat(parser.getOptionValue(string2, "Goodbye")).isEqualTo("Hello");
		assertThat(parser.getOptionValue(string3)).isNull();
		assertThat(parser.getOptionValue(string4, "Goodbye")).isEqualTo("Goodbye");
	}

	@Test
	public void testMultipleUses() throws Exception {
		final AbstractOption<Boolean> verbose = parser.addOption(new BooleanOption('v', "verbose", ""));
		parser.addOption(new BooleanOption('f', "foo", ""));
		parser.addOption(new BooleanOption('b', "bar", ""));
		parser.parse(new String[] { "--foo", "-v", "-v", "--verbose", "-v", "-b", "rest" });

		assertThat(countBooleanOption(verbose)).isEqualTo(4);
	}

	@Test
	public void testCombinedFlags() throws Exception {
		final AbstractOption<Boolean> alt = parser.addOption(new BooleanOption('a', "alt", ""));
		final AbstractOption<Boolean> debug = parser.addOption(new BooleanOption('d', "debug", ""));
		final AbstractOption<Boolean> verbose = parser.addOption(new BooleanOption('v', "verbose", ""));
		parser.parse(new String[] { "-dv" });

		assertThat(parser.getOptionValue(alt)).isNull();
		assertThat(parser.getOptionValue(debug)).isTrue();
		assertThat(parser.getOptionValue(verbose)).isTrue();
	}

	@Test
	public void testExplictlyTerminatedOptions() throws Exception {
		final AbstractOption<Boolean> alt = parser.addOption(new BooleanOption('a', "alt", ""));
		final AbstractOption<Boolean> debug = parser.addOption(new BooleanOption('d', "debug", ""));
		final AbstractOption<Boolean> verbose = parser.addOption(new BooleanOption('v', "verbose", ""));
		final AbstractOption<Double> fraction = parser.addOption(new DoubleOption('f', "fraction", ""));
		parser.parse(new String[] { "-a", "hello", "-d", "-f", "10", "--", "goodbye", "-v", "welcome", "-f", "-10" });

		
		assertThat(parser.getOptionValue(alt)).isTrue();
		assertThat(parser.getOptionValue(debug)).isTrue();
		assertThat(parser.getOptionValue(verbose)).isNull();
		assertThat(parser.getOptionValue(fraction)).isEqualTo(10.0);

		assertThat(parser.getRemainingArgs()).containsExactly("hello", "goodbye", "-v", "welcome", "-f", "-10");
	}

	@Test
	public void testGetOptionValues() throws Exception {
		final AbstractOption<Boolean> verbose = parser.addOption(new BooleanOption('v', "verbose", "forces verbose execution"));
		parser.addOption(new BooleanOption('f', "foo", ""));
		parser.addOption(new BooleanOption('b', "bar", ""));
		parser.parse(new String[] { "--foo", "-v", "-v", "--verbose", "-v", "-b", "rest" });

		assertThat(countBooleanOption(verbose)).isEqualTo(4);
	}

	@Test(expected = IllegalOptionValueException.class)
	public void testBadFormat() throws Exception {
		parser.addOption(new IntegerOption('s', "size", ""));
		parser.parse(new String[] { "--size=blah" });
	}

	@Test
	public void testResetBetweenParse() throws Exception {
		final AbstractOption<Boolean> verbose = parser.addOption(new BooleanOption('v', "verbose", "forces verbose execution"));
		parser.parse(new String[] { "-v" });
		assertThat(parser.getOptionValue(verbose)).isTrue();
		parser.parse(new String[] {});
		assertThat(parser.getOptionValue(verbose)).isNull();
	}

	@Test
	public void testLocale() throws Exception {
		final AbstractOption<Double> fraction = parser.addOption(new DoubleOption('f', "fraction", ""));
		parser.parse(new String[] { "--fraction=0.2" }, Locale.US);
		assertThat(parser.getOptionValue(fraction)).isEqualTo(0.2);
		parser.parse(new String[] { "--fraction=0,2" }, Locale.GERMANY);
		assertThat(parser.getOptionValue(fraction)).isEqualTo(0.2);
	}

	@Test(expected = UnknownOptionException.class)
	public void testDetachedOption() throws Exception {
		final AbstractOption<Boolean> detached = new BooleanOption('v', "verbose", "forces verbose execution");
		assertThat(parser.getOptionValue(detached)).isNull();
		parser.parse(new String[] { "-v" });
	}

	@Test(expected = IllegalOptionValueException.class)
	public void testMissingValueForStringOption() throws Exception {
		parser.addOption(new BooleanOption('v', "verbose", "forces verbose execution"));
		parser.addOption(new StringOption('c', "config", "configuration"));
		parser.parse(new String[] { "-v", "-c" });
	}

	@Test
	public void testWhitespaceValueForStringOption() throws Exception {
		final AbstractOption<String> opt = parser.addOption(new StringOption('o', "option", "option"));
		parser.parse(new String[] { "-o", " " });
		assertThat(parser.getOptionValue(opt)).isEqualTo(" ");
	}

	@Test
	public void testPrintUsage() throws Exception {
		parser.addOption(new StringOption('i', "input", "inputfile"));
		parser.addOption(new StringOption('o', "output", "outputfile"));
		parser.addOption(new IntegerOption('u', "u", "(optional) texture width"));
		parser.addOption(new IntegerOption('v', "v", "(optional) texture height"));
		parser.addHelpOption();
		parser.printUsage();

		final String ls = System.getProperty("line.separator");
		String expected = //
			"usage: appname [options]" + ls + //
			"options:" + ls + //
			"\t -h,--help: display help" + ls + //
			"\t -i,--input: inputfile" + ls + //
			"\t -o,--output: outputfile" + ls + //
			"\t -u,--u: (optional) texture width" + ls + //
			"\t -v,--v: (optional) texture height" + ls;
		assertThat(console.toString()).isEqualTo(expected);
	}

	@Test
	public void testAddHelpOptionShort() throws Exception {
		parser.addHelpOption();
		parser.parse(new String[] { "-h" });
		verify(parser, atLeastOnce()).printUsage();
	}

	@Test
	public void testAddHelpOptionLong() throws Exception {
		parser.addHelpOption();
		parser.parse(new String[] { "--help" });
		verify(parser, atLeastOnce()).printUsage();
	}

	@Test(expected = IllegalOptionValueException.class)
	public void testAddValidator() throws Exception {
		final AbstractOption<Integer> size = parser.addOption(new IntegerOption('s', "size", "size of something")).addValidator(
				new IntervalValidator<Integer>(0, 100));
		assertThat(size.toString()).isEqualTo(" -s,--size: size of something; interval [0, 100]");
		parser.parse(new String[] { "--size=50" });
		assertThat(parser.getOptionValue(size)).isEqualTo(50);
		parser.parse(new String[] { "--size=1000" });
	}

	private int countBooleanOption(AbstractOption<Boolean> opt) {
		int count = 0;
		final List<Boolean> v = parser.getOptionValues(opt);
		for (final Boolean b : v) {
			assertThat(b).isNotNull();
			if (b) {
				count++;
			} else {
				count--;
			}
		}
		return count;
	}
}