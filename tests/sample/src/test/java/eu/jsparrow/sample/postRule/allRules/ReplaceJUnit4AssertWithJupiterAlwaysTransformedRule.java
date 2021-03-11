package eu.jsparrow.sample.postRule.allRules;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class ReplaceJUnit4AssertWithJupiterAlwaysTransformedRule {

	class TestAssertEqualsWithStrings {

		@Test()
		public void testAssertEqualsWithStrings() throws Exception {
			assertEquals("HelloWorld!", "HelloWorld!");
		}

		@Test
		public void testAssertEqualsWithMessageAndStrings() throws Exception {
			assertEquals("HelloWorld!", "HelloWorld!", "expected that \"HelloWorld!\" equals \"HelloWorld!\".");
		}
	};

	class TestAssertEqualsWithDoubleObjects {

		@Test()
		public void testAssertEqualsWithStrings() throws Exception {
			assertEquals(Double.valueOf(1.0), Double.valueOf(1.0));
		}

		@Test
		public void testAssertEqualsWithMessageAndStrings() throws Exception {
			assertEquals(Double.valueOf(1.0), Double.valueOf(1.0),
					"expected that Double.valueOf(1.0)equals Double.valueOf(1.0).");
		}
	};
}