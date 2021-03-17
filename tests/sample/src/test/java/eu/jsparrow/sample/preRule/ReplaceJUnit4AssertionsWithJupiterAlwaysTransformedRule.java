package eu.jsparrow.sample.preRule;

import static org.junit.Assert.assertEquals;

import org.junit.jupiter.api.Test;

public class ReplaceJUnit4AssertionsWithJupiterAlwaysTransformedRule {

	class TestAssertEqualsWithStrings {

		@Test()
		public void testAssertEqualsWithStrings() throws Exception {
			assertEquals("HelloWorld!", "HelloWorld!");
		}

		@Test
		public void testAssertEqualsWithMessageAndStrings() throws Exception {
			assertEquals("expected that \"HelloWorld!\" equals \"HelloWorld!\".", "HelloWorld!", "HelloWorld!");
		}
	};

	class TestAssertEqualsWithDoubleObjects {

		@Test()
		public void testAssertEqualsWithStrings() throws Exception {
			assertEquals(Double.valueOf(1.0), Double.valueOf(1.0));
		}

		@Test
		public void testAssertEqualsWithMessageAndStrings() throws Exception {
			assertEquals("expected that Double.valueOf(1.0)equals Double.valueOf(1.0).", Double.valueOf(1.0),
					Double.valueOf(1.0));
		}
	};
}