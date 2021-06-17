package eu.jsparrow.sample.preRule;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class UseDedicatedAssertionsSwapComparisonOperandsRule {

	public static final String HELLO_WORLD = "Hello World!";

	final String helloWorld = "Hello World!";

	String s = "Hello World!";

	@Test
	public void testComparisonmWithUnQualifiedConstant() {
		assertTrue(s.equals(HELLO_WORLD));
	}

	@Test
	public void testComparisonWithQualifiedConstant() {
		assertTrue(s.equals(UseDedicatedAssertionsSwapComparisonOperandsRule.HELLO_WORLD));
	}

	@Test
	public void testComparisonWithThisHelloWorld() {
		assertTrue(s.equals(this.helloWorld));
	}

	class ChildClass extends UseDedicatedAssertionsSwapComparisonOperandsRule {

		@Test
		public void testHelloWorldWithSuperHelloWorld() {
			assertTrue(s.equals(super.helloWorld));
		}
	}
	
	@Test
	public void testComparisonWithFinalLocalVariable() {
		Object o = new Object();
		final Object oFinal = new Object();
		assertFalse(o.equals(oFinal));
	}
	
	@Test
	public void testComparisonWithExpectedLocalVariable() {
		int expected = 1;
		int actual = 1;
		assertTrue(actual == expected);
	}
}