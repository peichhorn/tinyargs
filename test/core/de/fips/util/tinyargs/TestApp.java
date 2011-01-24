package de.fips.util.tinyargs;

import de.fips.util.tinyargs.annotation.CommandLineApplication;
import de.fips.util.tinyargs.annotation.CommandLineOption;
import de.fips.util.tinyargs.annotation.CommandLineValidator;

@CommandLineApplication(appName = "TestApp", enableHelp = true, showUsageOnExeption = true)
public class TestApp {
	@CommandLineOption(longForm = "size", shortForm = "s", description = "a fancy size value")
	@CommandLineValidator(min = "0", max = "100")
	private int size;

	@CommandLineOption()
	@CommandLineValidator({ "Hello World", "foo" })
	public String text;

	@CommandLineOption()
	public double d = -123.23;

	public float f;

	private TestApp() {
		f = 1.13f;
	}

	public int getSize() {
		return size;
	}
}