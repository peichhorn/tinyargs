package de.fips.util.tinyargs;

import de.fips.util.tinyargs.annotation.ApplicationName;
import de.fips.util.tinyargs.annotation.EnableHelp;
import de.fips.util.tinyargs.annotation.InInterval;
import de.fips.util.tinyargs.annotation.Option;
import de.fips.util.tinyargs.annotation.OneOf;

@EnableHelp(showOnExeption = true)
@ApplicationName("TestApp")
public class TestApp {
	@Option(longForm = "size", shortForm = "s", description = "a fancy size value")
	@InInterval(min = "0", max = "100")
	private int size;

	@Option()
	@OneOf({ "Hello World", "foo" })
	public String text;

	@Option()
	public double d = -123.23;

	public float f;

	private TestApp() {
		f = 1.13f;
	}

	public int getSize() {
		return size;
	}
}