package eu.jsparrow.sample.preRule;

import static org.junit.Assert.assertEquals;

import org.junit.jupiter.api.Test;

public class ReplaceJUnit4AssertionsWithJupiterStringsRule {

	@Test()
	public void testAssertEqualsWithStrings() throws Exception {
		assertEquals("HelloWorld!", "HelloWorld!");
	}

	@Test
	public void testAssertEqualsWithMessageAndStrings() throws Exception {
		assertEquals("expected that \"HelloWorld!\" equals \"HelloWorld!\".", "HelloWorld!", "HelloWorld!");
	}
}