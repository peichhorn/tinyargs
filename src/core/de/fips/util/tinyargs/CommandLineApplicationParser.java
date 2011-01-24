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
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import de.fips.util.tinyargs.annotation.CommandLineApplication;
import de.fips.util.tinyargs.annotation.CommandLineOption;
import de.fips.util.tinyargs.annotation.CommandLineValidator;
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
 * &#64;CommandLineApplication(appName = "Application", enableHelp = true, showUsageOnExeption = true)
 * public class Application {
 * 
 *   // -s,--show: show GUI
 *   &#64;CommandLineOption(longForm="show", shortForm="s", description="show GUI")
 *   private boolean show; // default: false
 * 
 *   // --text; allowed values [Lorem ipsum, foo]
 *   &#64;CommandLineOption()
 *   &#64;CommandLineValidator({ "Lorem ipsum", "foo" })
 *   private String text; // default: "Lorem ipsum"
 * 
 *   // --count
 *   &#64;CommandLineOption()
 *   private Integer count; // default: null
 * 
 *   // --f; interval [-10.3, 2.6]
 *   &#64;CommandLineOption()
 *   &#64;CommandLineValidator(min = "-10.3f", max = "2.6f")
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
 *     CommandLineApplicationParser&lt;Application&gt; clap = CommandLineApplicationParser.of(Application.class);
 *     Application app = clap.parse(args);
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
public class CommandLineApplicationParser<E> {
	private final E annotatedObject;
	private final Locale locale;
	private AbstractOption<Void> helpOption;
	private CommandLineParser parser;
	private boolean showUsageOnExeption;
	private Set<Field> annotatedFields;
	private Map<Field, AbstractOption<Object>> fieldOptionMap;

	private CommandLineApplicationParser(E annotatedObject, Locale locale) throws IllegalArgumentException {
		this.annotatedObject = annotatedObject;
		this.locale = locale;
		setup();
	}

	private CommandLineApplicationParser(Class<E> annotatedObjectType, Locale locale) throws IllegalArgumentException {
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

	public E parse(String[] args) throws IllegalOptionValueException, UnknownOptionException {
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
	public void setPrintStream(PrintStream out) {
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
		final CommandLineApplication commandLineApplication = annotatedObjectType.getAnnotation(CommandLineApplication.class);
		if (commandLineApplication == null) {
			throw Util.illegalArgument("The class '%s' was not annotated with CommandLineApplication", annotatedObjectType);
		}
		setupAnnotatedFields();
		parser = new CommandLineParser();
		fieldOptionMap = new HashMap<Field, AbstractOption<Object>>();
		CommandLineOption commandLineOption;
		CommandLineValidator commandLineValidator;
		for (final Field field : annotatedFields) {
			commandLineOption = field.getAnnotation(CommandLineOption.class);
			final AbstractOption<Object> option = parser.addOption(optionForField(field, commandLineOption));
			commandLineValidator = field.getAnnotation(CommandLineValidator.class);
			if (commandLineValidator != null) {
				tryToAddIntervalValidator(annotatedObjectType, commandLineValidator, option);
				tryToAddValueSetValidator(annotatedObjectType, commandLineValidator, option);
			}
			fieldOptionMap.put(field, option);
		}

		// evaluate CommandLineApplication parameter
		if (commandLineApplication.enableHelp()) {
			helpOption = parser.addHelpOption();
		}
		showUsageOnExeption = commandLineApplication.showUsageOnExeption();
		if (commandLineApplication.appNameFromJar()) {
			parser.setApplicationNameFormJar();
		}
		if (!Util.isEmpty(commandLineApplication.appName())) {
			parser.setApplicationName(commandLineApplication.appName());
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void tryToAddIntervalValidator(Class<?> annotatedObjectType, CommandLineValidator commandLineValidator, AbstractOption<Object> option) throws IllegalArgumentException {
		tryToAddIntervalValidatorSafe(annotatedObjectType, commandLineValidator, (AbstractOption) option);
	}

	private <T extends Comparable<T>> void tryToAddIntervalValidatorSafe(Class<?> annotatedObjectType, CommandLineValidator commandLineValidator, AbstractOption<T> option) throws IllegalArgumentException {
		try {
			final String min = commandLineValidator.min();
			final String max = commandLineValidator.max();
			if (!Util.isEmpty(min) || !Util.isEmpty(max)) {
				final T minValue = Util.isEmpty(min) ? null : option.parseValue(min, locale);
				final T maxValue = Util.isEmpty(max) ? null : option.parseValue(max, locale);
				option.addValidator(new IntervalValidator<T>(minValue, maxValue));
			}
		} catch (final IllegalOptionValueException e2) {
			throw Util.illegalArgument("The parameter 'min' and 'max' of the CommandLineValidator can not be used to validate objects of the type '%s'!", annotatedObjectType.getName());
		}
	}

	private void tryToAddValueSetValidator(Class<?> annotatedObjectType, CommandLineValidator commandLineValidator, AbstractOption<Object> option) throws IllegalArgumentException {
		try {
			final String[] values = commandLineValidator.value();
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
		CommandLineOption commandLineOption;
		for (final Field field : annotatedObjectType.getDeclaredFields()) {
			field.setAccessible(true);
			commandLineOption = field.getAnnotation(CommandLineOption.class);
			if (commandLineOption != null) {
				annotatedFields.add(field);
			}
		}
		if (annotatedFields.isEmpty()) {
			throw Util.illegalArgument("The class '%s' does not have any CommandLineOption-annotated fields!", annotatedObject.getClass());
		}
	}

	private AbstractOption<Object> optionForField(Field field, CommandLineOption annotation) throws IllegalArgumentException {
		String longForm = annotation.longForm();
		if (Util.isEmpty(longForm)) {
			longForm = field.getName();
		}
		return optionForField(field.getType(), annotation.shortForm(), longForm, annotation.description());
	}

	private AbstractOption<Object> optionForField(Class<?> fieldType, String shortForm, String longForm, String description) throws IllegalArgumentException {
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
	
	private <T extends Throwable> T exception(T e) {
		if (showUsageOnExeption) {
			parser.getPrintStream().println(e.getMessage());
			parser.getPrintStream().println("");
			parser.printUsage();
		}
		return e;
	}

	public static <T> CommandLineApplicationParser<T> of(T annotatedObject) throws IllegalArgumentException {
		return of(annotatedObject, Locale.getDefault());
	}

	public static <T> CommandLineApplicationParser<T> of(T annotatedObject, Locale locale) throws IllegalArgumentException {
		return new CommandLineApplicationParser<T>(annotatedObject, locale);
	}

	public static <T> CommandLineApplicationParser<T> of(Class<T> annotatedObjectType) throws IllegalArgumentException {
		return of(annotatedObjectType, Locale.getDefault());
	}

	public static <T> CommandLineApplicationParser<T> of(Class<T> annotatedObjectType, Locale locale) throws IllegalArgumentException {
		return new CommandLineApplicationParser<T>(annotatedObjectType, locale);
	}
}
