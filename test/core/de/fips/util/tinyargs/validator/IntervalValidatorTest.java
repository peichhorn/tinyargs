package de.fips.util.tinyargs.validator;

import static org.fest.assertions.Assertions.assertThat;

import org.junit.Rule;
import org.junit.Test;

import de.fips.util.tinyargs.junit.ExpectedException;
import de.fips.util.tinyargs.validator.IntervalValidator;

public class IntervalValidatorTest {
	@Rule
	public ExpectedException thrown = ExpectedException.none();
	
	@Test
	public void whenMinIsLargerThanMax_construcor_shouldThrowException() throws Exception {
		// setup
		thrown.expectIllegalArgumentException("'min' is supposed to be smaller than 'max'");
		// run + assert
		new IntervalValidator<Integer>(100, 0);
	}

	@Test
	public void testValidate() throws Exception {
		// setup
		final IntervalValidator<Integer> validator = new IntervalValidator<Integer>(0, 100);
		// run + assert
		assertThat(validator.validate(-1)).isFalse();
		assertThat(validator.validate(0)).isTrue();
		assertThat(validator.validate(50)).isTrue();
		assertThat(validator.validate(100)).isTrue();
		assertThat(validator.validate(101)).isFalse();
	}

	@Test
	public void testValidate_withoutMin() throws Exception {
		// setup
		final IntervalValidator<Integer> validator = new IntervalValidator<Integer>(null, 100);
		// run + assert
		assertThat(validator.validate(-1000)).isTrue();
		assertThat(validator.validate(-1)).isTrue();
		assertThat(validator.validate(0)).isTrue();
		assertThat(validator.validate(50)).isTrue();
		assertThat(validator.validate(100)).isTrue();
		assertThat(validator.validate(101)).isFalse();
	}

	@Test
	public void testValidate_withoutMax() throws Exception {
		// setup
		final IntervalValidator<Integer> validator = new IntervalValidator<Integer>(0, null);
		// run + assert
		assertThat(validator.validate(-1)).isFalse();
		assertThat(validator.validate(0)).isTrue();
		assertThat(validator.validate(50)).isTrue();
		assertThat(validator.validate(100)).isTrue();
		assertThat(validator.validate(1000)).isTrue();
	}

	@Test
	public void testValidate_String() throws Exception {
		// setup
		final IntervalValidator<String> validator2 = new IntervalValidator<String>("bbbbb", "ggggg");
		// run + assert
		assertThat(validator2.validate("aaaaa")).isFalse();
		assertThat(validator2.validate("bbbbb")).isTrue();
		assertThat(validator2.validate("easda")).isTrue();
		assertThat(validator2.validate("ggggg")).isTrue();
		assertThat(validator2.validate("qeasd")).isFalse();
	}

	@Test
	public void testValidateIsNullSafe() throws Exception {
		// setup
		final IntervalValidator<Integer> validator = new IntervalValidator<Integer>(0, 100);
		// run + assert
		assertThat(validator.validate(null)).isFalse();
	}

	@Test
	public void testToString() throws Exception {
		// setup
		final IntervalValidator<Integer> validator = new IntervalValidator<Integer>(0, 100);
		// run + assert
		assertThat(validator.toString()).isEqualTo("interval [0, 100]");
	}

	@Test
	public void testToString_withoutMin() throws Exception {
		// setup
		final IntervalValidator<Integer> validator = new IntervalValidator<Integer>(null, 100);
		// run + assert
		assertThat(validator.toString()).isEqualTo("interval ]..., 100]");
	}

	@Test
	public void testToStringt_withoutMax() throws Exception {
		// setup
		final IntervalValidator<Integer> validator = new IntervalValidator<Integer>(0, null);
		// run + assert
		assertThat(validator.toString()).isEqualTo("interval [0, ...[");
	}

	@Test
	public void testToString_withoutMinAndMax() throws Exception {
		// setup
		final IntervalValidator<Integer> validator = new IntervalValidator<Integer>(null, null);
		// run + assert
		assertThat(validator.toString()).isEqualTo("interval ]..., ...[");
	}
}
