package eu.jsparrow.sample.postRule.useOffsetBasedStringMethods;

import static eu.jsparrow.sample.utilities.HelloWorld.HELLO_WORLD;

public class UseOffsetBasedStringMethodsMathAsVariableOrFieldRule {

	void testWithVariableMath() {
		String Math = "";
		int index = java.lang.Math.max(HELLO_WORLD
			.indexOf('d', 6) - 6, -1);
	}

	void testWithoutVariableMath() {
		int index = Math.max(HELLO_WORLD
			.indexOf('d', 6) - 6, -1);
	}

	void testWithMathAsFieldInLocalClass() {
		int index = java.lang.Math.max(HELLO_WORLD
			.indexOf('d', 6) - 6, -1);

		class LocalClassWithMathAsField {
			String Math = "";
		}
	}

	void testWithMathAsVariableInAnonymousClassMethod() {
		int index = java.lang.Math.max(HELLO_WORLD
			.indexOf('d', 6) - 6, -1);

		class LocalClassWithoutMathAsField {
			void test() {
				int index = java.lang.Math.max(HELLO_WORLD
					.indexOf('d', 6) - 6, -1);
			}
		}

		Runnable r = new Runnable() {

			public void run() {
				String Math = "";
			}
		};
	}

	void testWithMathAsVariableInLambda() {
		int index = java.lang.Math.max(HELLO_WORLD
			.indexOf('d', 6) - 6, -1);

		class LocalClassWithoutMathAsField {
			void test() {
				int index = java.lang.Math.max(HELLO_WORLD
					.indexOf('d', 6) - 6, -1);
			}
		}

		Runnable r = () -> {
			String Math = "";
		};
	}

	void testWithMathInLambdaAssignedToLocalClassField() {
		int index = java.lang.Math.max(HELLO_WORLD
			.indexOf('d', 6) - 6, -1);

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
			int index = java.lang.Math.max(HELLO_WORLD
				.indexOf('d', 6) - 6, -1);
		}
	}

	class InnerClassWithoutMathAsField {

		void test() {
			int index = Math.max(HELLO_WORLD
				.indexOf('d', 6) - 6, -1);
		}
	}
}

class ClassWithMathAsField {
	String Math = "";

	class InnerClassWithoutMathAsField {

		void test() {
			int index = java.lang.Math.max(HELLO_WORLD
				.indexOf('d', 6) - 6, -1);
		}
	}
}

class ClassWithLambdaAssignedToField {

	Runnable r = () -> {
		String Math = "";
	};

	void testWithoutVariableMath() {
		int index = Math.max(HELLO_WORLD
			.indexOf('d', 6) - 6, -1);
	}
}

class ClassWithAnonymousClassAssignedToField {

	Runnable r = new Runnable() {
		String Math = "";

		public void run() {
			String Math = "";
		}
	};

	void testWithoutVariableMath() {
		int index = Math.max(HELLO_WORLD
			.indexOf('d', 6) - 6, -1);
	}
}