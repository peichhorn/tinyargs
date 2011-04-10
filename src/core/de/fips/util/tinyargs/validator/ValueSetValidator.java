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

import java.util.Set;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Simple validator that checks if a given Object is in the {@link Set} of
 * accepted values.
 * 
 * @author Philipp Eichhorn
 */
@RequiredArgsConstructor
@Getter
public class ValueSetValidator<E> implements IValidator<E> {
	private final Set<E> validValues;

	@Override
	public boolean validate(final E value) {
		if (value == null) {
			return false;
		}
		return getValidValues().contains(value);
	}

	@Override
	public String toString() {
		return "allowed values " + getValidValues();
	}
}
