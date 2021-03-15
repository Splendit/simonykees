package eu.jsparrow.sample.postRule.allRules;

import static org.junit.Assert.assertEquals;

import org.junit.jupiter.api.Test;

public class ReplaceJUnit4AssertWithJupiterAmbiguousArgumentTypeRule {

	class TestAmbiguousMethodInvocationReturnType {
		@Test
		void test() {
			assertEquals(getGenericReturnValue(), getGenericReturnValue());
		}

		@SuppressWarnings("unchecked")
		<RET> RET getGenericReturnValue() {
			return (RET) Byte.valueOf((byte) 0);
		}
	}

	class TestAmbiguousSuperMethodInvocationReturnType extends TestAmbiguousMethodInvocationReturnType {
		@Override
		@Test
		void test() {
			assertEquals(super.getGenericReturnValue(), super.getGenericReturnValue());
		}
	}
}