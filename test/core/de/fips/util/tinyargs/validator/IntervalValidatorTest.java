package de.fips.util.tinyargs.validator;

import static org.fest.assertions.Assertions.assertThat;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import de.fips.util.tinyargs.validator.IntervalValidator;

public class IntervalValidatorTest {

	@Rule
	public ExpectedException thrown = ExpectedException.none();
	
	@Test
	public void whenMinIsLargerThanMax_construcor_shouldThrowException() throws Exception {
		// setup
		thrown.expect(IllegalArgumentException.class);
		thrown.expectMessage("'min' is supposed to be smaller than 'max'");
		// run + assert
		new IntervalValidator<Integer>(100, 0);
	}

	@Test
	public void testValidate() throws Exception {
		IntervalValidator<Integer> validator = new IntervalValidator<Integer>(0, 100);
		assertThat(validator.validate(-1)).isFalse();
		assertThat(validator.validate(0)).isTrue();
		assertThat(validator.validate(50)).isTrue();
		assertThat(validator.validate(100)).isTrue();
		assertThat(validator.validate(101)).isFalse();

		validator = new IntervalValidator<Integer>(null, 100);
		assertThat(validator.validate(-1000)).isTrue();
		assertThat(validator.validate(-1)).isTrue();
		assertThat(validator.validate(0)).isTrue();
		assertThat(validator.validate(50)).isTrue();
		assertThat(validator.validate(100)).isTrue();
		assertThat(validator.validate(101)).isFalse();

		validator = new IntervalValidator<Integer>(0, null);
		assertThat(validator.validate(-1)).isFalse();
		assertThat(validator.validate(0)).isTrue();
		assertThat(validator.validate(50)).isTrue();
		assertThat(validator.validate(100)).isTrue();
		assertThat(validator.validate(1000)).isTrue();

		IntervalValidator<String> validator2 = new IntervalValidator<String>("bbbbb", "ggggg");
		assertThat(validator2.validate("aaaaa")).isFalse();
		assertThat(validator2.validate("bbbbb")).isTrue();
		assertThat(validator2.validate("easda")).isTrue();
		assertThat(validator2.validate("ggggg")).isTrue();
		assertThat(validator2.validate("qeasd")).isFalse();
	}

	@Test
	public void testValidateIsNullSafe() throws Exception {
		IntervalValidator<Integer> validator = new IntervalValidator<Integer>(0, 100);
		assertThat(validator.validate(null)).isFalse();
	}

	@Test
	public void testToString() throws Exception {
		IntervalValidator<Integer> validator = new IntervalValidator<Integer>(0, 100);
		assertThat(validator.toString()).isEqualTo("interval [0, 100]");
		validator = new IntervalValidator<Integer>(null, 100);
		assertThat(validator.toString()).isEqualTo("interval ]..., 100]");
		validator = new IntervalValidator<Integer>(0, null);
		assertThat(validator.toString()).isEqualTo("interval [0, ...[");
		validator = new IntervalValidator<Integer>(null, null);
		assertThat(validator.toString()).isEqualTo("interval ]..., ...[");
	}
}
