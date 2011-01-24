package de.fips.util.tinyargs.validator;

import static org.fest.assertions.Assertions.assertThat;

import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import de.fips.util.tinyargs.validator.ValueSetValidator;

public class ValueSetValidatorTest {

	private ValueSetValidator<String> validator;
	private ValueSetValidator<Integer> validator2;

	@Before
	public void setUp() {
		final Set<String> valid = new HashSet<String>();
		valid.add("this");
		valid.add("that");
		final Set<Integer> valid2 = new HashSet<Integer>();
		valid2.add(13);
		valid2.add(45);
		validator = new ValueSetValidator<String>(valid);
		validator2 = new ValueSetValidator<Integer>(valid2);
	}

	@Test
	public void testValidate() throws Exception {
		assertThat(validator.validate("this")).isTrue();
		assertThat(validator.validate("that")).isTrue();
		assertThat(validator.validate("not this")).isFalse();

		assertThat(validator2.validate(45)).isTrue();
		assertThat(validator2.validate(13)).isTrue();
		assertThat(validator2.validate(234)).isFalse();
	}

	@Test
	public void testValidateIsNullSafe() throws Exception {
		assertThat(validator.validate(null)).isFalse();
	}

	@Test
	public void testToString() throws Exception {
		assertThat(validator.toString()).isEqualTo("allowed values [that, this]");
	}
}
