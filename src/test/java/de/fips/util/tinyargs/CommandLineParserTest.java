package de.fips.util.tinyargs;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.Locale;

import org.junit.Rule;
import org.junit.Test;

import de.fips.util.tinyargs.CommandLineParser;
import de.fips.util.tinyargs.junit.ExpectedException;
import de.fips.util.tinyargs.junit.Std;
import de.fips.util.tinyargs.option.AbstractOption;
import de.fips.util.tinyargs.option.BooleanOption;
import de.fips.util.tinyargs.option.DoubleOption;
import de.fips.util.tinyargs.option.FloatOption;
import de.fips.util.tinyargs.option.IntegerOption;
import de.fips.util.tinyargs.option.LongOption;
import de.fips.util.tinyargs.option.StringOption;
import de.fips.util.tinyargs.validator.IntervalValidator;

public class CommandLineParserTest {
	@Rule
	public final Std stdErr = Std.err();
	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Test
	public void testParseFillsOptions() throws Exception {
		// setup
		final CommandLineParser parser = new CommandLineParser();
		final AbstractOption<Integer> size = parser.addOption(new IntegerOption('s', "size", "size"));
		assertThat(parser.getOptionValue(size)).isNull();
		// run
		parser.parse(new String[] { "--size=100" }, Locale.US);
		// assert
		assertThat(parser.getOptionValue(size)).isEqualTo(100);
	}

	@Test
	public void testStandardOptions() throws Exception {
		// setup
		final CommandLineParser parser = new CommandLineParser();
		final AbstractOption<Boolean> verbose = parser.addOption(new BooleanOption('v', "verbose", "forces verbose execution"));
		final AbstractOption<Integer> size = parser.addOption(new IntegerOption('s', "size", ""));
		final AbstractOption<String> name = parser.addOption(new StringOption('n', "name", ""));
		final AbstractOption<Double> fraction = parser.addOption(new DoubleOption('f', "fraction", ""));
		final AbstractOption<Boolean> missing = parser.addOption(new BooleanOption('m', "missing", ""));
		final AbstractOption<Boolean> careful = parser.addOption(new BooleanOption("careful", ""));
		final AbstractOption<Long> bignum = parser.addOption(new LongOption('b', "bignum", ""));
		final Long longValue = Long.valueOf(Long.valueOf(Integer.MAX_VALUE) + 1);
		// run
		parser.parse(new String[] { "-v", "--size=100", "-b", longValue.toString(), "-n", "foo", "-f", "0.1", "rest" }, Locale.US);
		// assert
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
		// setup
		final CommandLineParser parser = new CommandLineParser();
		final Integer fortyTwo = Integer.valueOf(42);
		final Integer thirtySix = Integer.valueOf(36);
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
		// run
		parser.parse(new String[] { "--boolean1", "--boolean2", "--int1=42", "--int2=42", "--string1=Hello", "--string2=Hello", });
		// assert
		assertThat(parser.getOptionValue(boolean1)).isTrue();
		assertThat(parser.getOptionValue(boolean2, Boolean.FALSE)).isTrue();
		assertThat(parser.getOptionValue(boolean3)).isNull();
		assertThat(parser.getOptionValue(boolean4, Boolean.FALSE)).isFalse();
		assertThat(parser.getOptionValue(boolean5, Boolean.TRUE)).isTrue();
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
		// setup
		final CommandLineParser parser = new CommandLineParser();
		final AbstractOption<Boolean> verbose = parser.addOption(new BooleanOption('v', "verbose", ""));
		parser.addOption(new BooleanOption('f', "foo", ""));
		parser.addOption(new BooleanOption('b', "bar", ""));
		// run
		parser.parse(new String[] { "--foo", "-v", "-v", "--verbose", "-v", "-b", "rest" });
		// assert
		assertThat(countBooleanOption(parser, verbose)).isEqualTo(4);
	}

	@Test
	public void testCombinedFlags() throws Exception {
		// setup
		final CommandLineParser parser = new CommandLineParser();
		final AbstractOption<Boolean> alt = parser.addOption(new BooleanOption('a', "alt", ""));
		final AbstractOption<Boolean> debug = parser.addOption(new BooleanOption('d', "debug", ""));
		final AbstractOption<Boolean> verbose = parser.addOption(new BooleanOption('v', "verbose", ""));
		// run
		parser.parse(new String[] { "-dv" });
		// assert
		assertThat(parser.getOptionValue(alt)).isNull();
		assertThat(parser.getOptionValue(debug)).isTrue();
		assertThat(parser.getOptionValue(verbose)).isTrue();
	}

	@Test
	public void testExplictlyTerminatedOptions() throws Exception {
		// setup
		final CommandLineParser parser = new CommandLineParser();
		final AbstractOption<Boolean> alt = parser.addOption(new BooleanOption('a', "alt", ""));
		final AbstractOption<Boolean> debug = parser.addOption(new BooleanOption('d', "debug", ""));
		final AbstractOption<Boolean> verbose = parser.addOption(new BooleanOption('v', "verbose", ""));
		final AbstractOption<Double> fraction = parser.addOption(new DoubleOption('f', "fraction", ""));
		final AbstractOption<Float> start = parser.addOption(new FloatOption('s', "start", ""));
		// run
		parser.parse(new String[] { "-a", "hello", "-d", "-f", "10", "-s", "3.14", "--", "goodbye", "-v", "welcome", "-f", "-10"}, Locale.US);
		// assert
		assertThat(parser.getOptionValue(alt)).isTrue();
		assertThat(parser.getOptionValue(debug)).isTrue();
		assertThat(parser.getOptionValue(verbose)).isNull();
		assertThat(parser.getOptionValue(fraction)).isEqualTo(10.0);
		assertThat(parser.getOptionValue(start)).isEqualTo(3.14f);
		assertThat(parser.getRemainingArgs()).containsExactly("hello", "goodbye", "-v", "welcome", "-f", "-10");
	}

	@Test
	public void testGetOptionValues() throws Exception {
		// setup
		final CommandLineParser parser = new CommandLineParser();
		final AbstractOption<Boolean> verbose = parser.addOption(new BooleanOption('v', "verbose", "forces verbose execution"));
		parser.addOption(new BooleanOption('f', "foo", ""));
		parser.addOption(new BooleanOption('b', "bar", ""));
		// run
		parser.parse(new String[] { "--foo", "-v", "-v", "--verbose", "-v", "-b", "rest" });
		// assert
		assertThat(countBooleanOption(parser, verbose)).isEqualTo(4);
	}

	@Test
	public void testBadFormat() throws Exception {
		// setup
		final CommandLineParser parser = new CommandLineParser();
		parser.addOption(new IntegerOption('s', "size", ""));
		// run + assert
		thrown.expectIllegalOptionValueException("Illegal value 'blah' for option -s/--size");
		parser.parse(new String[] { "--size=blah" });
	}

	@Test
	public void testResetBetweenParse() throws Exception {
		// setup
		final CommandLineParser parser = new CommandLineParser();
		final AbstractOption<Boolean> verbose = parser.addOption(new BooleanOption('v', "verbose", "forces verbose execution"));
		// run 1
		parser.parse(new String[] { "-v" });
		// assert 1
		assertThat(parser.getOptionValue(verbose)).isTrue();
		// run 2
		parser.parse(new String[] {});
		// assert 2
		assertThat(parser.getOptionValue(verbose)).isNull();
	}

	@Test
	public void testLocale_US() throws Exception {
		// setup
		final CommandLineParser parser = new CommandLineParser();
		final AbstractOption<Double> fraction = parser.addOption(new DoubleOption('f', "fraction", ""));
		// run
		parser.parse(new String[] { "--fraction=0.2" }, Locale.US);
		// assert
		assertThat(parser.getOptionValue(fraction)).isEqualTo(0.2);
	}

	@Test
	public void testLocale_GERMANY() throws Exception {
		// setup
		final CommandLineParser parser = new CommandLineParser();
		final AbstractOption<Double> fraction = parser.addOption(new DoubleOption('f', "fraction", ""));
		// run
		parser.parse(new String[] { "--fraction=0,2" }, Locale.GERMANY);
		// assert
		assertThat(parser.getOptionValue(fraction)).isEqualTo(0.2);
	}

	@Test
	public void testDetachedOption() throws Exception {
		// setup
		final CommandLineParser parser = new CommandLineParser();
		// run + assert
		thrown.expectUnknownOptionException("Unknown option '-v'");
		parser.parse(new String[] { "-v" });
	}

	@Test
	public void testMissingValueForStringOption() throws Exception {
		// setup
		final CommandLineParser parser = new CommandLineParser();
		parser.addOption(new BooleanOption('v', "verbose", "forces verbose execution"));
		parser.addOption(new StringOption('c', "config", "configuration"));
		// run + assert
		thrown.expectIllegalOptionValueException("Illegal value '' for option -c/--config");
		parser.parse(new String[] { "-v", "-c" });
	}

	@Test
	public void testWhitespaceValueForStringOption() throws Exception {
		// setup
		final CommandLineParser parser = new CommandLineParser();
		final AbstractOption<String> opt = parser.addOption(new StringOption('o', "option", "option"));
		// run
		parser.parse(new String[] { "-o", " " });
		// assert
		assertThat(parser.getOptionValue(opt)).isEqualTo(" ");
	}

	@Test
	public void testPrintUsage() throws Exception {
		// setup
		final CommandLineParser parser = new CommandLineParser();
		parser.addOption(new StringOption('i', "input", "inputfile"));
		parser.addOption(new StringOption('o', "output", "outputfile"));
		parser.addOption(new IntegerOption('u', "u", "(optional) texture width"));
		parser.addOption(new IntegerOption('v', "v", "(optional) texture height"));
		parser.addHelpOption();
		final String ls = System.getProperty("line.separator");
		final String expected = //
			"usage: appname [options]" + ls + //
			"options:" + ls + //
			"\t -h,--help: display help" + ls + //
			"\t -i,--input: inputfile" + ls + //
			"\t -o,--output: outputfile" + ls + //
			"\t -u,--u: (optional) texture width" + ls + //
			"\t -v,--v: (optional) texture height" + ls;
		// run
		parser.printUsage();
		// assert
		assertThat(stdErr.getContent()).contains(expected);
	}

	@Test
	public void testAddHelpOptionShort() throws Exception {
		// setup
		final CommandLineParser parser = spy(new CommandLineParser());
		parser.addHelpOption();
		// run
		parser.parse(new String[] { "-h" });
		// assert
		verify(parser, atLeastOnce()).printUsage();
	}

	@Test
	public void testAddHelpOptionLong() throws Exception {
		// setup
		final CommandLineParser parser = spy(new CommandLineParser());
		parser.addHelpOption();
		// run
		parser.parse(new String[] { "--help" });
		// assert
		verify(parser, atLeastOnce()).printUsage();
	}

	@Test
	public void testAddValidator_match() throws Exception {
		// setup
		final CommandLineParser parser = new CommandLineParser();
		final AbstractOption<Integer> size = parser.addOption(new IntegerOption('s', "size", "size of something"));
		size.addValidator(new IntervalValidator<Integer>(0, 100));
		assertThat(size.toString()).isEqualTo(" -s,--size: size of something; interval [0, 100]");
		// run
		parser.parse(new String[] { "--size=50" });
		// assert
		assertThat(parser.getOptionValue(size)).isEqualTo(50);
	}

	@Test
	public void testAddValidator_noMatch() throws Exception {
		// setup
		final CommandLineParser parser = new CommandLineParser();
		final AbstractOption<Integer> size = parser.addOption(new IntegerOption('s', "size", "size of something"));
		size.addValidator(new IntervalValidator<Integer>(0, 100));
		assertThat(size.toString()).isEqualTo(" -s,--size: size of something; interval [0, 100]");
		// run + assert
		thrown.expectIllegalOptionValueException("Illegal value '1000' for option -s/--size");
		parser.parse(new String[] { "--size=1000" });
	}

	private int countBooleanOption(final CommandLineParser parser, final AbstractOption<Boolean> opt) {
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