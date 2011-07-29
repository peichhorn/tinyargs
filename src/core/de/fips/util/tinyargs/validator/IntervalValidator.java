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
package de.fips.util.tinyargs.validator;

import lombok.Getter;

/**
 * Simple validator that checks if a given {@link Comparable} is in the interval
 * of accepted values.<br>
 * 
 * @author Philipp Eichhorn
 */
@Getter
public class IntervalValidator<E extends Comparable<E>> implements IValidator<E> {
	private final E min;
	private final E max;

	/**
	 * Constructs an IntervalValidator.
	 * 
	 * @param min
	 *            minimum of the interval
	 * @param max
	 *            maximum of the interval
	 */
	public IntervalValidator(final E min, final E max) {
		super();
		if ((min != null) && (max != null) && (min.compareTo(max) > 0)) {
			throw new IllegalArgumentException("'min' is supposed to be smaller than 'max'");
		}
		this.min = min;
		this.max = max;
	}

	@Override
	public boolean validate(final E value) {
		if (value == null) {
			return false;
		}
		return notSmallerThan(value, getMin()) && notLargerThan(value, getMax());
	}

	private boolean notSmallerThan(final E value, final E min) {
		return (min == null) || (value.compareTo(min) >= 0);
	}

	private boolean notLargerThan(final E value, final E max) {
		return (max == null) || (value.compareTo(max) <= 0);
	}

	@Override
	public String toString() {
		return String.format("interval %s, %s", getMin() == null ? "]..." : "[" + getMin(), getMax() == null ? "...[" : getMax() + "]");
	}
}
