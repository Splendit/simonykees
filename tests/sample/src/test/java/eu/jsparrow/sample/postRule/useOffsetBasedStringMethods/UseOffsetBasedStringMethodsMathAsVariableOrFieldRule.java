package eu.jsparrow.sample.postRule.useOffsetBasedStringMethods;

public class UseOffsetBasedStringMethodsMathAsVariableOrFieldRule {

	void testWithVariableMath() {
		String Math = "";
		String str = "Hello World!";
		int index = java.lang.Math.max(str
			.indexOf('d', 6) - 6, -1);
	}

	void testWithoutVariableMath() {
		String str = "Hello World!";
		int index = Math.max(str
			.indexOf('d', 6) - 6, -1);
	}

	void testWitMathAsFieldInLocalClass() {
		String str = "Hello World!";
		int index = java.lang.Math.max(str
			.indexOf('d', 6) - 6, -1);

		class LocalClassWithMathAsField {
			String Math = "";
		}
	}

	void testWitMathAsVariableInAnonymousClassMethod() {
		String str = "Hello World!";
		int index = java.lang.Math.max(str
			.indexOf('d', 6) - 6, -1);

		class LocalClassWithoutMathAsField {
			void test() {
				int index = java.lang.Math.max(str
					.indexOf('d', 6) - 6, -1);
			}
		}

		Runnable r = new Runnable() {

			public void run() {
				String Math = "";
			}
		};
	}

	void testWitMathAsVariableInLambda() {
		String str = "Hello World!";
		int index = java.lang.Math.max(str
			.indexOf('d', 6) - 6, -1);

		class LocalClassWithoutMathAsField {
			void test() {
				int index = java.lang.Math.max(str
					.indexOf('d', 6) - 6, -1);
			}
		}

		Runnable r = () -> {
			String Math = "";
		};
	}

	void max() {
	}

	class InnerClassWithMathAsField {
		String Math = "";

		void test() {
			String str = "Hello World!";
			int index = java.lang.Math.max(str
				.indexOf('d', 6) - 6, -1);
		}
	}

	class InnerClassWithoutMathAsField {

		void test() {
			String str = "Hello World!";
			int index = Math.max(str
				.indexOf('d', 6) - 6, -1);
		}
	}
}

class ClassWithMathAsField {
	String Math = "";

	class InnerClassWithoutMathAsField {

		void test() {
			String str = "Hello World!";
			int index = java.lang.Math.max(str
				.indexOf('d', 6) - 6, -1);
		}
	}
}

class ClassWithLambdaAssignedToField {

	Runnable r = () -> {
		String Math = "";
	};

	void testWithoutVariableMath() {
		String str = "Hello World!";
		int index = Math.max(str
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
		String str = "Hello World!";
		int index = Math.max(str
			.indexOf('d', 6) - 6, -1);
	}
}