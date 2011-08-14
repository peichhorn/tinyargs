/*
 * Copyright © 2009-2011 Philipp Eichhorn.
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package de.fips.util.tinyargs;

import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import de.fips.util.tinyargs.annotation.ApplicationName;
import de.fips.util.tinyargs.annotation.Option;
import de.fips.util.tinyargs.annotation.OneOf;
import de.fips.util.tinyargs.annotation.InInterval;
import de.fips.util.tinyargs.annotation.EnableHelp;
import de.fips.util.tinyargs.exception.IllegalOptionValueException;
import de.fips.util.tinyargs.exception.UnknownOptionException;
import de.fips.util.tinyargs.option.AbstractOption;
import de.fips.util.tinyargs.validator.IntervalValidator;
import de.fips.util.tinyargs.validator.ValueSetValidator;

/**
 * Starting-point for the annotation-based command-line option parsing. <br>
 * <br>
 * Creates a {@link CommandLineParser} based on the annotations in the
 * command-line application class.
 * 
 * <pre>
 * Example:
 * 
 * &#64;EnableHelp(showOnExeption = true)
 * &#64;ApplicationName("Application")
 * public class Application {
 * 
 *   // -s,--show: show GUI
 *   &#64;Option(longForm="show", shortForm="s", description="show GUI")
 *   private boolean show; // default: false
 * 
 *   // --text; allowed values [Lorem ipsum, foo]
 *   &#64;Option()
 *   &#64;OneOf({ "Lorem ipsum", "foo" })
 *   private String text; // default: "Lorem ipsum"
 * 
 *   // --count
 *   &#64;Option()
 *   private Integer count; // default: null
 * 
 *   // --f; interval [-10.3, 2.6]
 *   &#64;Option()
 *   &#64;InInterval(min = "-10.3f", max = "2.6f")
 *   private float f = 1.23f; // default: 1.23f
 * 	
 *   private Application() {
 *     // required default constructor, 
 *     // can also be used to initialize fields
 *     text = "Lorem ipsum";
 *   }
 * 
 *   public void start() {
 *     if (show) {
 *       doSomethingWithGUI();
 *     } else {
 *       doSomething();
 *     }
 *   }
 *  
 *   public static void main(String[] args) {
 *     CommandLineReader&lt;Application&gt; clr = CommandLineReader.of(Application.class);
 *     Application app = clr.read(args);
 *     app.start();
 *   }
 * }
 * </pre>
 * 
 * @param <E>
 *            Type of the command-line application
 * 
 * @author Philipp Eichhorn
 */
public class CommandLineReader<E> {
	private final E annotatedObject;
	private final Locale locale;
	private AbstractOption<Void> helpOption;
	private CommandLineParser parser;
	private boolean showUsageOnExeption;
	private Set<Field> annotatedFields;
	private Map<Field, AbstractOption<Object>> fieldOptionMap;

	private CommandLineReader(final E annotatedObject, final Locale locale) throws IllegalArgumentException {
		this.annotatedObject = annotatedObject;
		this.locale = locale;
		setup();
	}

	private CommandLineReader(final Class<E> annotatedObjectType, final Locale locale) throws IllegalArgumentException {
		try {
			final Constructor<E> constructor = annotatedObjectType.getDeclaredConstructor();
			constructor.setAccessible(true);
			this.annotatedObject = constructor.newInstance();
		} catch (final Exception e) {
			throw Util.illegalArgument("The class '%s' does not offer a default constructor!", annotatedObjectType);
		}
		this.locale = locale;
		setup();
	}

	public E read(final String[] args) throws IllegalOptionValueException, UnknownOptionException {
		try {
			parser.parse(args, locale);
		} catch (final IllegalOptionValueException e1) {
			throw exception(e1);
		} catch (final UnknownOptionException e2) {
			throw exception(e2);
		}
		try {
			for (final Field field : annotatedFields) {
				final Object value = parser.getOptionValue(fieldOptionMap.get(field), field.get(annotatedObject));
				field.set(annotatedObject, value);
			}
		} catch (final IllegalAccessException ignore) {
		}
		return annotatedObject;
	}

	public boolean helpRequested() {
		return ((helpOption != null) && parser.hasValues(helpOption));
	}

	/**
	 * @return The arguments no option was specified for.
	 */
	public List<String> getRemainingArgs() {
		return parser.getRemainingArgs();
	}

	/**
	 * Sets the {@link PrintStream} used for
	 * {@link CommandLineParser#printUsage()}.
	 */
	public void setPrintStream(final PrintStream out) {
		parser.setPrintStream(out);
	}

	/**
	 * Returns the {@link PrintStream} used for
	 * {@link CommandLineParser#printUsage()}.
	 */
	public PrintStream getPrintStream() {
		return parser.getPrintStream();
	}

	private void setup() throws IllegalArgumentException {
		final Class<?> annotatedObjectType = annotatedObject.getClass();
		setupAnnotatedFields();
		parser = new CommandLineParser();
		fieldOptionMap = new HashMap<Field, AbstractOption<Object>>();
		for (final Field field : annotatedFields) {
			Option option = field.getAnnotation(Option.class);
			final AbstractOption<Object> optionForField = parser.addOption(optionForField(field, option));
			OneOf oneOf = field.getAnnotation(OneOf.class);
			InInterval inInterval = field.getAnnotation(InInterval.class);
			if (oneOf != null) {
				tryToAddValueSetValidator(annotatedObjectType, oneOf, optionForField);
			}
			if (inInterval != null) {
				tryToAddIntervalValidator(annotatedObjectType, inInterval, optionForField);
			}
			fieldOptionMap.put(field, optionForField);
		}
		final EnableHelp enableHelp = annotatedObjectType.getAnnotation(EnableHelp.class);
		if (enableHelp != null) {
			helpOption = parser.addHelpOption();
			showUsageOnExeption = enableHelp.showOnExeption();
		}
		final ApplicationName applicationName = annotatedObjectType.getAnnotation(ApplicationName.class);
		if (applicationName != null) {
			parser.setApplicationName(applicationName.value());
			if (applicationName.fromJar()) {
				parser.setApplicationNameFormJar();
			}
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void tryToAddIntervalValidator(final Class<?> annotatedObjectType, final InInterval inInterval, final AbstractOption<Object> option) throws IllegalArgumentException {
		tryToAddIntervalValidatorSafe(annotatedObjectType, inInterval, (AbstractOption) option);
	}

	private <T extends Comparable<T>> void tryToAddIntervalValidatorSafe(final Class<?> annotatedObjectType, final InInterval inInterval, final AbstractOption<T> option) throws IllegalArgumentException {
		try {
			final String min = inInterval.min();
			final String max = inInterval.max();
			if (!Util.isEmpty(min) || !Util.isEmpty(max)) {
				final T minValue = Util.isEmpty(min) ? null : option.parseValue(min, locale);
				final T maxValue = Util.isEmpty(max) ? null : option.parseValue(max, locale);
				option.addValidator(new IntervalValidator<T>(minValue, maxValue));
			}
		} catch (final IllegalOptionValueException e2) {
			throw Util.illegalArgument("The parameter 'min' and 'max' of the CommandLineValidator can not be used to validate objects of the type '%s'!", annotatedObjectType.getName());
		}
	}

	private void tryToAddValueSetValidator(final Class<?> annotatedObjectType, final OneOf oneOf, final AbstractOption<Object> option) throws IllegalArgumentException {
		try {
			final String[] values = oneOf.value();
			if (!Util.isEmpty(values)) {
				final Set<Object> validValues = new HashSet<Object>();
				for (final String value : values) {
					validValues.add(option.parseValue(value, locale));
				}
				option.addValidator(new ValueSetValidator<Object>(validValues));
			}
		} catch (final IllegalOptionValueException e) {
			throw Util.illegalArgument("The parameter 'values' of the CommandLineValidator can not be used to validate objects of the type '%s'!", annotatedObjectType.getName());
		}
	}

	private void setupAnnotatedFields() throws IllegalArgumentException {
		annotatedFields = new HashSet<Field>();
		final Class<?> annotatedObjectType = annotatedObject.getClass();
		Option commandLineOption;
		for (final Field field : annotatedObjectType.getDeclaredFields()) {
			field.setAccessible(true);
			commandLineOption = field.getAnnotation(Option.class);
			if (commandLineOption != null) {
				annotatedFields.add(field);
			}
		}
		if (annotatedFields.isEmpty()) {
			throw Util.illegalArgument("The class '%s' does not have any CommandLineOption-annotated fields!", annotatedObject.getClass());
		}
	}

	private AbstractOption<Object> optionForField(final Field field, final Option annotation) throws IllegalArgumentException {
		String longForm = annotation.longForm();
		if (Util.isEmpty(longForm)) {
			longForm = field.getName();
		}
		return optionForField(field.getType(), annotation.shortForm(), longForm, annotation.description());
	}

	private AbstractOption<Object> optionForField(final Class<?> fieldType, final String shortForm, final String longForm, final String description) throws IllegalArgumentException {
		final String optionTypeName = Util.getOptionTypeName(fieldType);
		Class<? extends AbstractOption<Object>> optionClass = null;
		try {
			optionClass = Util.uncheckedCast(Class.forName(optionTypeName));
		} catch (final Exception e) {
			throw Util.illegalArgument("Could not find '%s' required to fill the CommandLineOption", optionTypeName);
		}
		try {
			if (Util.isEmpty(shortForm)) {
				final Constructor<? extends AbstractOption<Object>> constructor = optionClass.getConstructor(String.class, String.class);
				return constructor.newInstance(longForm, description);
			} else {
				final Constructor<? extends AbstractOption<Object>> constructor = optionClass.getConstructor(Character.TYPE, String.class, String.class);
				return constructor.newInstance(shortForm.charAt(0), longForm, description);
			}
		} catch (final Exception e) {
			throw Util.illegalArgument("Could not find constructor '%s(char, String, String)' or '%s(String, String)'.", optionTypeName, optionTypeName);
		}
	}

	private <T extends Throwable> T exception(final T e) {
		if (showUsageOnExeption) {
			parser.getPrintStream().println(e.getMessage());
			parser.getPrintStream().println("");
			parser.printUsage();
		}
		return e;
	}

	public static <T> CommandLineReader<T> of(final T annotatedObject) throws IllegalArgumentException {
		return of(annotatedObject, Locale.getDefault());
	}

	public static <T> CommandLineReader<T> of(final T annotatedObject, final Locale locale) throws IllegalArgumentException {
		return new CommandLineReader<T>(annotatedObject, locale);
	}

	public static <T> CommandLineReader<T> of(final Class<T> annotatedObjectType) throws IllegalArgumentException {
		return of(annotatedObjectType, Locale.getDefault());
	}

	public static <T> CommandLineReader<T> of(final Class<T> annotatedObjectType, final Locale locale) throws IllegalArgumentException {
		return new CommandLineReader<T>(annotatedObjectType, locale);
	}
}
