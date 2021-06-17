package eu.jsparrow.sample.postRule.dedicatedAssertions;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

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

	class ChildClass extends UseDedicatedAssertionsSwapComparisonOperandsRule {

		@Test
		public void testHelloWorldWithSuperHelloWorld() {
			assertEquals(super.helloWorld, s);
		}
	}
	
	@Test
	public void testComparisonWithFinalLocalVariable() {
		Object o = new Object();
		final Object oFinal = new Object();
		assertNotEquals(oFinal, o);
	}
	
	@Test
	public void testComparisonWithExpectedLocalVariable() {
		int expected = 1;
		int actual = 1;
		assertEquals(expected, actual);
	}
}