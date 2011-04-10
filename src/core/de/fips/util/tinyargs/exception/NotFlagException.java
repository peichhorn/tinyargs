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
package de.fips.util.tinyargs.exception;

import lombok.Getter;

/**
 * Thrown when the parsed commandline contains multiple concatenated short
 * options, such as -abcd, where one or more requires a value.
 * 
 * @author Philipp Eichhorn
 * @author All JArgs authors see JARGS_LICENSE
 */
@Getter
public class NotFlagException extends UnknownOptionException {
	private static final long serialVersionUID = -4265983372323265248L;

	private final char optionChar;

	/**
	 * Constructor
	 * 
	 * @param option
	 *            The name of the option whose value was illegal (e.g. "-u").
	 * @param notFlag
	 *            The first character which wasn't a boolean
	 */
	public NotFlagException(final String option, final char optionChar) {
		super(option, String.format("Illegal option: '%s', '%s' requires a value", option, optionChar));
		this.optionChar = optionChar;
	}
}