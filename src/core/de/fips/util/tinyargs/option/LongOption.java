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

import java.util.Locale;

import de.fips.util.tinyargs.exception.IllegalOptionValueException;

/**
 * An option that expects an long integer value.
 * 
 * @author Philipp Eichhorn
 * @author All JArgs authors see JARGS_LICENSE
 */
public class LongOption extends AbstractOption<Long> {

	public LongOption(String longForm, String description) {
		super(longForm, description, true);
	}

	public LongOption(char shortForm, String longForm, String description) {
		super(shortForm, longForm, description, true);
	}

	@Override
	public Long parseValue(String arg, Locale locale) throws IllegalOptionValueException {
		try {
			return new Long(arg);
		} catch (final NumberFormatException e) {
			throw new IllegalOptionValueException(this, arg);
		}
	}
}