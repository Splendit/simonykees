package eu.jsparrow.sample.postRule.migrateJUnitToJupiter;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ReplaceJUnit4AssertionsWithJupiterStringsRule {

	@Test()
	public void testAssertEqualsWithStrings() throws Exception {
		assertEquals("HelloWorld!", "HelloWorld!");
	}

	@Test
	public void testAssertEqualsWithMessageAndStrings() throws Exception {
		assertEquals("HelloWorld!", "HelloWorld!", "expected that \"HelloWorld!\" equals \"HelloWorld!\".");
	}
}