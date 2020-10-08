package eu.jsparrow.sample.postRule.allRules;

import org.apache.commons.lang3.StringUtils;

public class UseOffsetBasedStringMethodsMathAsVariableOrFieldRule {

	void testWithVariableMath() {
		final String Math = "";
		final String str = "Hello World!";
		final int index = java.lang.Math.max(StringUtils.indexOf(str, 'd', 6) - 6, -1);
	}

	void testWithoutVariableMath() {
		final String str = "Hello World!";
		final int index = Math.max(StringUtils.indexOf(str, 'd', 6) - 6, -1);
	}

	void testWitMathAsFieldInLocalClass() {
		final String str = "Hello World!";
		final int index = java.lang.Math.max(StringUtils.indexOf(str, 'd', 6) - 6, -1);

		class LocalClassWithMathAsField {
			String Math = "";
		}
	}

	void testWitMathAsVariableInAnonymousClassMethod() {
		final String str = "Hello World!";
		final int index = java.lang.Math.max(StringUtils.indexOf(str, 'd', 6) - 6, -1);

		class LocalClassWithoutMathAsField {
			void test() {
				final int index = java.lang.Math.max(StringUtils.indexOf(str, 'd', 6) - 6, -1);
			}
		}

		final Runnable r = () -> {
			final String Math = "";
		};
	}

	void testWitMathAsVariableInLambda() {
		final String str = "Hello World!";
		final int index = java.lang.Math.max(StringUtils.indexOf(str, 'd', 6) - 6, -1);

		class LocalClassWithoutMathAsField {
			void test() {
				final int index = java.lang.Math.max(StringUtils.indexOf(str, 'd', 6) - 6, -1);
			}
		}

		final Runnable r = () -> {
			final String Math = "";
		};
	}

	void max() {
	}

	class InnerClassWithMathAsField {
		String Math = "";

		void test() {
			final String str = "Hello World!";
			final int index = java.lang.Math.max(StringUtils.indexOf(str, 'd', 6) - 6, -1);
		}
	}

	class InnerClassWithoutMathAsField {

		void test() {
			final String str = "Hello World!";
			final int index = Math.max(StringUtils.indexOf(str, 'd', 6) - 6, -1);
		}
	}
}

class ClassWithMathAsField {
	String Math = "";

	class InnerClassWithoutMathAsField {

		void test() {
			final String str = "Hello World!";
			final int index = java.lang.Math.max(StringUtils.indexOf(str, 'd', 6) - 6, -1);
		}
	}
}

class ClassWithLambdaAssignedToField {

	Runnable r = () -> {
		String Math = "";
	};

	void testWithoutVariableMath() {
		final String str = "Hello World!";
		final int index = Math.max(StringUtils.indexOf(str, 'd', 6) - 6, -1);
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
		final String str = "Hello World!";
		final int index = Math.max(StringUtils.indexOf(str, 'd', 6) - 6, -1);
	}
}