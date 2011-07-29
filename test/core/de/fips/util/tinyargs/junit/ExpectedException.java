/*
Copyright © 2011 Philipp Eichhorn.

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
package de.fips.util.tinyargs.junit;

import lombok.Delegate;
import lombok.NoArgsConstructor;

import org.junit.rules.MethodRule;

import de.fips.util.tinyargs.exception.IllegalOptionValueException;
import de.fips.util.tinyargs.exception.UnknownOptionException;

@NoArgsConstructor(staticName="none")
public class ExpectedException implements MethodRule {
	@Delegate
	private final org.junit.rules.ExpectedException delegate = org.junit.rules.ExpectedException.none();

	public void expectIllegalArgumentException(String message) {
		expect(IllegalArgumentException.class);
		expectMessage(message);
	}

	public void expectIllegalOptionValueException(String message) {
		expect(IllegalOptionValueException.class);
		expectMessage(message);
	}
	
	public void expectUnknownOptionException(String message) {
		expect(UnknownOptionException.class);
		expectMessage(message);
	}
}