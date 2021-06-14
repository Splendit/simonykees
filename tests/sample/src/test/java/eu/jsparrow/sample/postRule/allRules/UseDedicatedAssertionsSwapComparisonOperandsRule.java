package eu.jsparrow.sample.postRule.allRules;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.Test;

public class UseDedicatedAssertionsSwapComparisonOperandsRule {

	public static final String HELLO_WORLD = "Hello World!";

	final String helloWorld = "Hello World!";

	String s = "Hello World!";

	@Test
	public void testComparisonmWithUnQualifiedConstant() {
		assertEquals(HELLO_WORLD, s);
	}

	@Test
	public void testComparisonWithQualifiedConstant() {
		assertEquals(UseDedicatedAssertionsSwapComparisonOperandsRule.HELLO_WORLD, s);
	}

	@Test
	public void testComparisonWithThisHelloWorld() {
		assertEquals(this.helloWorld, s);
	}

	@Test
	public void testComparisonWithFinalLocalVariable() {
		final Object o = new Object();
		final Object oFinal = new Object();
		assertNotEquals(o, oFinal);
	}

	@Test
	public void testComparisonWithExpectedLocalVariable() {
		final int expected = 1;
		final int actual = 1;
		assertEquals(actual, expected);
	}

	class ChildClass extends UseDedicatedAssertionsSwapComparisonOperandsRule {

		@Test
		public void testHelloWorldWithSuperHelloWorld() {
			assertEquals(super.helloWorld, s);
		}
	}
}