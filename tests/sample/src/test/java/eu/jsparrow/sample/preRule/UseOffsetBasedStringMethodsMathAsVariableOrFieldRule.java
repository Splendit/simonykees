package eu.jsparrow.sample.preRule;

import static eu.jsparrow.sample.utilities.HelloWorld.HELLO_WORLD;

public class UseOffsetBasedStringMethodsMathAsVariableOrFieldRule {

	void testWithVariableMath() {
		String Math = "";
		int index = HELLO_WORLD.substring(6)
			.indexOf('d');
	}

	void testWithoutVariableMath() {
		int index = HELLO_WORLD.substring(6)
			.indexOf('d');
	}

	void testWithMathAsFieldInLocalClass() {
		int index = HELLO_WORLD.substring(6)
			.indexOf('d');

		class LocalClassWithMathAsField {
			String Math = "";
		}
	}

	void testWithMathAsVariableInAnonymousClassMethod() {
		int index = HELLO_WORLD.substring(6)
			.indexOf('d');

		class LocalClassWithoutMathAsField {
			void test() {
				int index = HELLO_WORLD.substring(6)
					.indexOf('d');
			}
		}

		Runnable r = new Runnable() {

			public void run() {
				String Math = "";
			}
		};
	}

	void testWithMathAsVariableInLambda() {
		int index = HELLO_WORLD.substring(6)
			.indexOf('d');

		class LocalClassWithoutMathAsField {
			void test() {
				int index = HELLO_WORLD.substring(6)
					.indexOf('d');
			}
		}

		Runnable r = () -> {
			String Math = "";
		};
	}

	void testWithMathInLambdaAssignedToLocalClassField() {
		int index = HELLO_WORLD.substring(6)
			.indexOf('d');

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
			int index = HELLO_WORLD.substring(6)
				.indexOf('d');
		}
	}

	class InnerClassWithoutMathAsField {

		void test() {
			int index = HELLO_WORLD.substring(6)
				.indexOf('d');
		}
	}
}

class ClassWithMathAsField {
	String Math = "";

	class InnerClassWithoutMathAsField {

		void test() {
			int index = HELLO_WORLD.substring(6)
				.indexOf('d');
		}
	}
}

class ClassWithLambdaAssignedToField {

	Runnable r = () -> {
		String Math = "";
	};

	void testWithoutVariableMath() {
		int index = HELLO_WORLD.substring(6)
			.indexOf('d');
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
		int index = HELLO_WORLD.substring(6)
			.indexOf('d');
	}
}