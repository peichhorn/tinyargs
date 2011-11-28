package de.fips.util.tinyargs.validator;

import static org.fest.assertions.Assertions.assertThat;

import java.util.Arrays;
import java.util.HashSet;

import org.junit.Test;

import de.fips.util.tinyargs.validator.ValueSetValidator;

public class ValueSetValidatorTest {

	@Test
	public void testValidate_String() throws Exception {
		// setup
		final ValueSetValidator<String> validator = valueSetValidator("this", "that");
		// run + assert
		assertThat(validator.validate("this")).isTrue();
		assertThat(validator.validate("that")).isTrue();
		assertThat(validator.validate("not this")).isFalse();
	}

	@Test
	public void testValidate_Integer() throws Exception {
		// setup
		final ValueSetValidator<Integer> validator = valueSetValidator(13, 45);
		// run + assert
		assertThat(validator.validate(45)).isTrue();
		assertThat(validator.validate(13)).isTrue();
		assertThat(validator.validate(234)).isFalse();
	}

	@Test
	public void testValidateIsNullSafe() throws Exception {
		// setup
		final ValueSetValidator<String> validator = valueSetValidator("this", "that");
		// run + assert
		assertThat(validator.validate(null)).isFalse();
	}

	@Test
	public void testToString() throws Exception {
		// setup
		final ValueSetValidator<String> validator = valueSetValidator("this", "that");
		// run + assert
		assertThat(validator.toString()).isEqualTo("allowed values [that, this]");
	}

	private <T> ValueSetValidator<T> valueSetValidator(final T... values) {
		return new ValueSetValidator<T>(new HashSet<T>(Arrays.<T>asList(values)));
	}
}
