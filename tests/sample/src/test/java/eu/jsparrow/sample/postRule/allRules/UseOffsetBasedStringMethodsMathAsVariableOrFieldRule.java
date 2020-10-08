package eu.jsparrow.sample.postRule.allRules;

import static eu.jsparrow.sample.utilities.HelloWorld.HELLO_WORLD;

import org.apache.commons.lang3.StringUtils;

public class UseOffsetBasedStringMethodsMathAsVariableOrFieldRule {

	void testWithVariableMath() {
		final String Math = "";
		final int index = java.lang.Math.max(StringUtils.indexOf(HELLO_WORLD, 'd', 6) - 6, -1);
	}

	void testWithoutVariableMath() {
		final int index = Math.max(StringUtils.indexOf(HELLO_WORLD, 'd', 6) - 6, -1);
	}

	void testWithMathAsFieldInLocalClass() {
		final int index = java.lang.Math.max(StringUtils.indexOf(HELLO_WORLD, 'd', 6) - 6, -1);

		class LocalClassWithMathAsField {
			String Math = "";
		}
	}

	void testWithMathAsVariableInAnonymousClassMethod() {
		final int index = java.lang.Math.max(StringUtils.indexOf(HELLO_WORLD, 'd', 6) - 6, -1);

		class LocalClassWithoutMathAsField {
			void test() {
				final int index = java.lang.Math.max(StringUtils.indexOf(HELLO_WORLD, 'd', 6) - 6, -1);
			}
		}

		final Runnable r = () -> {
			final String Math = "";
		};
	}

	void testWithMathAsVariableInLambda() {
		final int index = java.lang.Math.max(StringUtils.indexOf(HELLO_WORLD, 'd', 6) - 6, -1);

		class LocalClassWithoutMathAsField {
			void test() {
				final int index = java.lang.Math.max(StringUtils.indexOf(HELLO_WORLD, 'd', 6) - 6, -1);
			}
		}

		final Runnable r = () -> {
			final String Math = "";
		};
	}

	void testWithMathInLambdaAssignedToLocalClassField() {
		final int index = java.lang.Math.max(StringUtils.indexOf(HELLO_WORLD, 'd', 6) - 6, -1);

		class LocalClassWithLambda {
			Runnable r = () -> {
				String Math = "";
			};
		}
	}

	class ClassWithMaxMethod {
		void max() {
		}
	}

	class InnerClassWithMathAsField {
		String Math = "";

		void test() {
			final int index = java.lang.Math.max(StringUtils.indexOf(HELLO_WORLD, 'd', 6) - 6, -1);
		}
	}

	class InnerClassWithoutMathAsField {

		void test() {
			final int index = Math.max(StringUtils.indexOf(HELLO_WORLD, 'd', 6) - 6, -1);
		}
	}
}

class ClassWithMathAsField {
	String Math = "";

	class InnerClassWithoutMathAsField {

		void test() {
			final int index = java.lang.Math.max(StringUtils.indexOf(HELLO_WORLD, 'd', 6) - 6, -1);
		}
	}
}

class ClassWithLambdaAssignedToField {

	Runnable r = () -> {
		String Math = "";
	};

	void testWithoutVariableMath() {
		final int index = Math.max(StringUtils.indexOf(HELLO_WORLD, 'd', 6) - 6, -1);
	}
}

class ClassWithAnonymousClassAssignedToField {

	Runnable r = new Runnable() {
		String Math = "";

		@Override
		public void run() {
			String Math = "";
		}
	};

	void testWithoutVariableMath() {
		final int index = Math.max(StringUtils.indexOf(HELLO_WORLD, 'd', 6) - 6, -1);
	}
}