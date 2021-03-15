package eu.jsparrow.sample.postRule.allRules;

import static org.junit.Assert.assertEquals;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ReplaceJUnit4AssertWithJupiterGenericMethodCallsAsArgumentsRule {

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

	class TestUnAmbiguousMethodInvocationReturnType {
		@Test
		void test() {
			Assertions.assertEquals(this.<Byte>getGenericReturnValue(), this.<Byte>getGenericReturnValue());
		}

		@SuppressWarnings("unchecked")
		<RET> RET getGenericReturnValue() {
			return (RET) Byte.valueOf((byte) 0);
		}
	}

	class TestUnAmbiguousSuperMethodInvocationReturnType extends TestUnAmbiguousMethodInvocationReturnType {
		@Override
		@Test
		void test() {
			Assertions.assertEquals(super.<Byte>getGenericReturnValue(), super.<Byte>getGenericReturnValue());
		}
	}
}