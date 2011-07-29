/*
Copyright © 2009-2011 Philipp Eichhorn.

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
 */
package de.fips.util.tinyargs;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;

import de.fips.util.tinyargs.exception.IllegalOptionValueException;
import de.fips.util.tinyargs.exception.NotFlagException;
import de.fips.util.tinyargs.exception.UnknownOptionException;
import de.fips.util.tinyargs.exception.UnknownSuboptionException;
import de.fips.util.tinyargs.option.AbstractOption;
import de.fips.util.tinyargs.option.OptionComparator;

/**
 * Largely GNU-compatible command-line options parser. Has short (-v) and
 * long-form (--verbose) option support, and also allows options with associated
 * values (-d 2, --debug 2, --debug=2). Option processing can be explicitly
 * terminated by the argument '--'.
 * 
 * @author Philipp Eichhorn
 * @author All JArgs authors see JARGS_LICENSE
 */
public class CommandLineParser {
	@Getter
	private final List<String> remainingArgs = new ArrayList<String>();
	private final Map<String, AbstractOption<?>> parameterOptionsMap = new HashMap<String, AbstractOption<?>>();
	private final List<AbstractOption<?>> options = new ArrayList<AbstractOption<?>>();
	private final Map<String, List<Object>> values = new HashMap<String, List<Object>>();
	@Setter
	private String applicationName = System.getProperty("app.name", "appname");
	@Getter @Setter
	private PrintStream printStream = System.err;

	/**
	 * Add the specified Option to the list of accepted options
	 * 
	 * @param option
	 *            The specified Option.
	 * @param <E>
	 *            Type of options value.
	 * @return The specified Option itself.
	 */
	public <E> AbstractOption<E> addOption(AbstractOption<E> option) {
		if (option.getShortForm() != null) {
			parameterOptionsMap.put("-" + option.getShortForm(), option);
		}
		parameterOptionsMap.put("--" + option.getLongForm(), option);
		options.add(option);
		return option;
	}

	/**
	 * Adds the help option.
	 * 
	 * <pre>
	 * usage:
	 * 
	 * CommandLineParser parser;
	 * AbstractOption<Void> help = parser.addHelpOption();
	 * parser.parse(args);
	 * if (!hasValues(help)) {
	 *   // do something
	 * }
	 * 
	 * same as:
	 * 
	 * CommandLineParser parser;
	 * AbstractOption<Boolean> help = parser.addOption(new BooleanOption('h', "help", "display help"));
	 * parser.parse(args);
	 * if (hasValues(help)) {
	 *   parser.printUsage();
	 * } else {
	 *   // do something
	 * }
	 * </pre>
	 * 
	 * @return the AbstractOption representing the help option
	 */
	public AbstractOption<Void> addHelpOption() {
		return addOption(new HelpOption());
	}

	/**
	 * Equivalent to {@link #getOptionValue(AbstractOption, Object)
	 * getOptionValue(option, null)}.
	 */
	public <E> E getOptionValue(AbstractOption<E> option) {
		return getOptionValue(option, null);
	}

	/**
	 * @param option
	 *            The specified Option.
	 * @param def
	 *            The default value.
	 * @param <E>
	 *            Type of options value.
	 * @return The parsed value of the specified Option, or a default value if
	 *         the option was not set.
	 */
	public <E> E getOptionValue(AbstractOption<E> option, E def) {
		final List<Object> v = values.get(option.getLongForm());
		if (Util.isEmpty(v)) {
			return def;
		} else {
			return Util.<E> uncheckedCast(v.get(0));
		}
	}

	/**
	 * @param option
	 *            The specified Option.
	 * @param <E>
	 *            Type of options value.
	 * @return A List giving the parsed values of all the occurrences of the
	 *         given Option, or an empty List if the option was not set.
	 */
	public <E> List<E> getOptionValues(AbstractOption<E> option) {
		final List<E> result = new ArrayList<E>();
		final List<Object> optionValues = values.get(option.getLongForm());
		if (optionValues != null) {
			for (final Object value : optionValues) {
				final E e = Util.<E> uncheckedCast(value);
				result.add(e);
			}
		}
		return result;
	}

	/**
	 * @param option
	 *            The specified Option.
	 * @param <E>
	 *            Type of options value.
	 * @return Status flag which indicates whether there are values for a given
	 *         options or not.
	 */
	public <E> boolean hasValues(AbstractOption<E> option) {
		return !Util.isEmpty(values.get(option.getLongForm()));
	}

	/**
	 * Equivalent to {@link #parse(String[], Locale) parse(args,
	 * Locale.getDefault())}.
	 */
	public void parse(String[] args) throws IllegalOptionValueException, UnknownOptionException {
		parse(args, Locale.getDefault());
	}

	/**
	 * Extract the options and non-option arguments from the given list of
	 * command-line arguments. The specified locale is used for parsing options
	 * whose values might be locale-specific.
	 * 
	 * @param args
	 *            List of command-line arguments.
	 * @throws IllegalOptionValueException
	 * @throws UnknownOptionException
	 */
	public void parse(String[] args, Locale locale) throws IllegalOptionValueException, UnknownOptionException {
		final List<String> otherArgs = new ArrayList<String>();
		this.values.clear();
		boolean allFine = true;
		for (int position = 0; position < args.length; position++) {
			String curArg = args[position];
			if (allFine && curArg.startsWith("-")) {
				if (curArg.equals("--")) {
					allFine = false;
					continue;
				}
				String valueArg = null;
				if (curArg.startsWith("--")) {
					final int equalsPos = curArg.indexOf("=");
					if (equalsPos != -1) {
						valueArg = curArg.substring(equalsPos + 1);
						curArg = curArg.substring(0, equalsPos);
					}
				} else if (curArg.length() > 2) {
					for (int i = 1; i < curArg.length(); i++) {
						final AbstractOption<?> opt = this.parameterOptionsMap.get("-" + curArg.charAt(i));
						if (opt == null) {
							throw new UnknownSuboptionException(curArg, curArg.charAt(i));
						}
						if (opt.isValueNeeded()) {
							throw new NotFlagException(curArg, curArg.charAt(i));
						}
						addValue(opt, opt.getValue(null, locale));
					}
					continue;
				}

				final AbstractOption<?> opt = this.parameterOptionsMap.get(curArg);
				if (opt == null) {
					throw new UnknownOptionException(curArg);
				}
				if (opt.isValueNeeded()) {
					if (valueArg == null) {
						position++;
						if (position < args.length) {
							valueArg = args[position];
						}
					}
				} else {
					valueArg = null;
				}
				Object value = opt.getValue(valueArg, locale);
				addValue(opt, value);
			} else {
				otherArgs.add(curArg);
			}
		}
		remainingArgs.clear();
		remainingArgs.addAll(otherArgs);
	}

	/**
	 * Sets the application name by looking into the first entry of the
	 * <code>classpath</code>.
	 */
	public void setApplicationNameFormJar() {
		final String[] classpath = System.getProperty("java.class.path").split(";");
		if ((classpath.length > 0) && (classpath[0].endsWith(".jar"))) {
			setApplicationName(classpath[0]);
		}
	}

	/**
	 * Prints the usage-message
	 */
	public void printUsage() {
		Collections.sort(options, new OptionComparator());
		printStream.println("usage: " + applicationName + " [options]");
		printStream.println("options:");
		for (final AbstractOption<?> option : options) {
			printStream.println("\t" + option);
		}
	}

	private void addValue(AbstractOption<?> opt, Object value) {
		final String lf = opt.getLongForm();
		List<Object> v = values.get(lf);
		if (v == null) {
			v = new ArrayList<Object>();
			values.put(lf, v);
		}
		v.add(value);
	}

	private final class HelpOption extends AbstractOption<Void> {
		public HelpOption() {
			super('h', "help", "display help", false);
		}

		@Override
		public Void guardedParseValue(String arg, Locale locale) {
			printUsage();
			return null;
		}
	}
}
