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

import java.util.Collection;

import de.fips.util.tinyargs.option.AbstractOption;

/**
 * Collection of utility methods.
 * 
 * @author Philipp Eichhorn
 */
final class Util {
	private final static String OPTION_PACKAGE = AbstractOption.class.getPackage().getName();

	private Util() {
		// prevent instantiation
	}

	@SuppressWarnings("unchecked")
	public static <T> T uncheckedCast(Object value) {
		return (T) value;
	}

	public static IllegalArgumentException illegalArgument(String message, Object... args) {
		return new IllegalArgumentException(String.format(message, args));
	}
	
	/**
	 * <pre>
	 * int     -> jargs.gnu.option.IntegerOption
	 * boolean -> jargs.gnu.option.BooleanOption
	 * String  -> jargs.gnu.option.StringOption
	 * Custom  -> jargs.gnu.option.CustomOption
	 * </pre>
	 */
	public static String getOptionTypeName(Class<?> fieldType) {
		final StringBuilder builder = new StringBuilder(OPTION_PACKAGE);
		builder.append(".");
		if (fieldType == Character.TYPE) {
			builder.append("Character");
		} else if (fieldType == Integer.TYPE) {
			builder.append("Integer");
		} else {
			builder.append(capitalizeName(fieldType.getSimpleName()));
		}
		builder.append("Option");
		return builder.toString();
	}

	/**
	 * <pre>
	 * isEmpty(null)  = true
	 * isEmpty("")    = true
	 * isEmpty("abc") = false
	 * </pre>
	 */
	public static boolean isEmpty(final String s) {
		return (s == null) || s.isEmpty();
	}

	/**
	 * <pre>
	 * isEmpty(null)               = true
	 * isEmpty(new int[]{})        = true
	 * isEmpty(new int[]{1, 2, 3}) = false
	 * </pre>
	 */
	public static <T> boolean isEmpty(final T[] v) {
		return (v == null) || (v.length <= 0);
	}
	
	/**
	 * <pre>
	 * isEmpty(null)      = true
	 * isEmpty([])        = true
	 * isEmpty([1, 2, 3]) = false
	 * </pre>
	 */
	public static <T> boolean isEmpty(Collection<T> v) {
		return (v == null) || v.isEmpty();
	}

	/**
	 * name -> Name
	 */
	public static String capitalizeName(final String name) {
		return name.substring(0, 1).toUpperCase() + name.substring(1);
	}
}
