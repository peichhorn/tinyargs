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
package de.fips.util.tinyargs.option;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.Locale;

/**
 * An option that expects a date value.
 * 
 * @author Philipp Eichhorn
 */
public final class DateOption extends AbstractOption<Date> {
	private DateFormat dateFormat;

	public DateOption(final String longForm, final String description) {
		this(longForm, description, null);
	}

	public DateOption(final char shortForm, final String longForm, final String description) {
		this(shortForm, longForm, description, null);
	}

	public DateOption(final String longForm, final String description, final DateFormat dateFormat) {
		super(longForm, description, false);
		this.dateFormat = dateFormat;
	}

	public DateOption(final char shortForm, final String longForm, final String description, final DateFormat dateFormat) {
		super(shortForm, longForm, description, false);
		this.dateFormat = dateFormat;
	}

	@Override
	public Date guardedParseValue(final String arg, final Locale locale) throws ParseException {
		if (dateFormat == null) {
			dateFormat = DateFormat.getDateInstance(DateFormat.SHORT, locale);
		}
		return dateFormat.parse(arg);
	}
}
