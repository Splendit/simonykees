package eu.jsparrow.sample.postRule.allRules;

import junit.framework.TestCase;

public class ReplaceJUnit3TestCasesAmbiguousSuperMethodReturnTypeRule extends TestCase {

	static class ClassWithMethodWithAmbiguousReturnType {

		@SuppressWarnings("unchecked")
		<T> T getGenericReturnValue() {
			return (T) Byte.valueOf((byte) 0);
		}

	}

	static class SubClassUsingMethodWithAmbiguousReturnType extends ClassWithMethodWithAmbiguousReturnType {
		void testAssertion() {
			assertEquals(super.getGenericReturnValue(), super.getGenericReturnValue());
		}
	}
}