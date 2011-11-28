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
package de.fips.util.tinyargs.exception;

import lombok.Getter;
import de.fips.util.tinyargs.option.AbstractOption;

/**
 * Thrown when an illegal or missing value is given by the user for an option
 * that takes a value.
 * 
 * @author Philipp Eichhorn
 * @author All JArgs authors see JARGS_LICENSE
 */
@Getter
public class IllegalOptionValueException extends OptionException {
	private static final long serialVersionUID = 3047817875698671483L;

	private final AbstractOption<?> option;
	private final String value;

	/**
	 * Constructor
	 * 
	 * @param option
	 *            The option whose value was illegal (e.g. "-u").
	 * @param value
	 *            The illegal value.
	 */
	public IllegalOptionValueException(final AbstractOption<?> option, final String value) {
		super(String.format("Illegal value '%s' for option %s--%s", value, option.getShortForm() != null ? "-" + option.getShortForm() + "/" : "", option.getLongForm()));
		this.option = option;
		this.value = value;
	}
}
