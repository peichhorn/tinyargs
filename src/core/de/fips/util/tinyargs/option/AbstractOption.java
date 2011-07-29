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
package de.fips.util.tinyargs.option;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import lombok.Getter;

import de.fips.util.tinyargs.exception.IllegalOptionValueException;
import de.fips.util.tinyargs.validator.IValidator;

/**
 * Representation of an command-line option.
 * 
 * @param <E>
 *            Type of options value.
 * 
 * @author Philipp Eichhorn
 * @author All JArgs authors see JARGS_LICENSE
 */
@Getter
public abstract class AbstractOption<E> {
	private final String shortForm;
	private final String longForm;
	private final String description;
	private final boolean isValueNeeded;
	private final List<IValidator<E>> validators;

	public AbstractOption(final String longForm, final String description, final boolean wantsValue) {
		this(null, longForm, description, wantsValue);
	}

	public AbstractOption(final char shortForm, final String longForm, final String description, final boolean wantsValue) {
		this(String.valueOf(shortForm), longForm, description, wantsValue);
	}

	private AbstractOption(final String shortForm, final String longForm, final String description, final boolean wantsValue) {
		if (longForm == null) {
			throw new IllegalArgumentException("longForm may not be null");
		}
		this.shortForm = shortForm;
		this.longForm = longForm;
		this.description = description;
		this.isValueNeeded = wantsValue;
		validators = new ArrayList<IValidator<E>>();
	}

	/**
	 * Adds a validator to this option.
	 */
	public AbstractOption<E> addValidator(final IValidator<E> validator) {
		validators.add(validator);
		return this;
	}

	/**
	 * @throws IllegalOptionValueException
	 */
	public final E getValue(final String arg, final Locale locale) throws IllegalOptionValueException {
		if (isValueNeeded()) {
			if (arg == null) {
				throw new IllegalOptionValueException(this, "");
			}
		}
		final E value = parseValue(arg, locale);
		for (final IValidator<E> validator : getValidators()) {
			if (!validator.validate(value)) {
				throw new IllegalOptionValueException(this, arg);
			}
		}
		return value;
	}

	public final E parseValue(final String arg, final Locale locale) throws IllegalOptionValueException {
		try {
			return guardedParseValue(arg, locale);
		} catch (final Exception e) {
			throw new IllegalOptionValueException(this, arg);
		}		
	}

	/**
	 * Override to extract and convert an option value passed on the
	 * command-line.
	 * 
	 * @param arg
	 *            A command-line argument.
	 * @param locale
	 *            The specified Locale.
	 * @return The parsed option value.
	 * @throws Exception
	 */
	public abstract E guardedParseValue(final String arg, final Locale locale) throws Exception;

	@Override
	public String toString() {
		final StringBuilder option = new StringBuilder();
		if (getShortForm() != null) {
			option.append(" -").append(getShortForm()).append(",");
		}
		option.append("--").append(getLongForm());
		if (!getDescription().isEmpty()) {
			option.append(": ").append(getDescription());
		}
		for (final IValidator<E> validator : getValidators()) {
			option.append("; ").append(validator.toString());
		}
		return option.toString();
	}
}