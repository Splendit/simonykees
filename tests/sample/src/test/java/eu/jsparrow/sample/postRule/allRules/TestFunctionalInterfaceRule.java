package eu.jsparrow.sample.postRule.allRules;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings({ "nls", "unused", "rawtypes" })
public class TestFunctionalInterfaceRule {

	private static final Logger log = LoggerFactory.getLogger(TestFunctionalInterfaceRule.class);

	private final String FINAL_STRING_FIELD;
	private final String NOT_INITIALIZED_FIELD;

	private final AFunctionalInterface usingUnDeclaredField = new AFunctionalInterface() {
		@Override
		public void method(int a) {
			final String s = FINAL_INITIALIZED_STRING_FIELD;
		}
	};

	private final AFunctionalInterface reusingFieldDuringInitialization = new AFunctionalInterface() {
		@Override
		public void method(int a) {
			reusingFieldDuringInitialization.toString();
		}
	};

	private final String FINAL_INITIALIZED_STRING_FIELD = "initialized";

	private final AFunctionalInterface usingUnInitializedField = new AFunctionalInterface() {
		@Override
		public void method(int a) {
			final String s = FINAL_STRING_FIELD;
		}
	};

	private final AFunctionalInterface usingInitializedField = (int a) -> {
		/*
		 * Using initialized field
		 */
		final String s = FINAL_INITIALIZED_STRING_FIELD;
	};

	private final AFunctionalInterface usingWildcardsInBody = new AFunctionalInterface() {

		@Override
		public void method(int a) {
			final List<List<? extends Number>> numbers = new ArrayList<>();
			numbers.stream()
				.map(List::hashCode)
				.mapToInt(Integer::intValue)
				.sum();
		}
	};

	private final String declaredfterConstructor = "declaredAfterCtor";

	int a;

	AFunctionalInterface aFunctionalInterface = (int a) -> {
	};

	{
		final int a;
		final AFunctionalInterface aFunctionalInterface = (int a1) -> {
		};
	}

	public TestFunctionalInterfaceRule() {
		final AFunctionalInterface foo = new AFunctionalInterface() {

			@Override
			public void method(int a) {
				final String sthToLog = a + FINAL_STRING_FIELD;

			}
		};
		FINAL_STRING_FIELD = "irritating";

		final AFunctionalInterface foo2 = (int a) -> {
			final String sthToLog = a + FINAL_STRING_FIELD;

		};

		final AFunctionalInterface foo3 = (int a) -> {
			final String t = declaredfterConstructor;

		};

		if (foo3 != null) {
			final AFunctionalInterface foo4 = new AFunctionalInterface() {
				@Override
				public void method(int a) {
					final String sthToLog = a + NOT_INITIALIZED_FIELD;
				}
			};
		} else {
			final AFunctionalInterface foo5 = new AFunctionalInterface() {
				@Override
				public void method(int a) {
					final String sthToLog = a + NOT_INITIALIZED_FIELD;
				}
			};
		}

		if (foo != null) {
			NOT_INITIALIZED_FIELD = "";
			final AFunctionalInterface inNestedBlock = (int a) -> {
				final String sthToLog = a + NOT_INITIALIZED_FIELD;

			};
		} else {
			NOT_INITIALIZED_FIELD = "";
		}

	}

	public void usingUnassignedFieldInMethod() {
		final AFunctionalInterface foo2 = (int a) -> {
			final String sthToLog = a + FINAL_STRING_FIELD;

		};
	}

	@Test
	public void test1() {

		final Runnable runnable = () -> log.debug("xx");

		runnable.run();

		final MyClass mYClass = new MyClass(() -> log.debug("xy"));

		mYClass.test();

		final NonFunctionalInterface nonFunctionalInterface = new NonFunctionalInterface() {

			@Override
			public void method(int a) {
				log.debug("zy");
			}

			@Override
			public void method() {
				log.debug("xy");
			}
		};

		nonFunctionalInterface.method();

		final AFunctionalInterface aFunctionalInterface = (int a) -> {
		};

		final AFunctionalInterface aFunctionalInterface2 = (int a) -> {
		};

		aFunctionalInterface.method(0);
	}

	public void clashingLocalVariableNames(int l) {
		final int a;
		final int a1;
		a = 5;
		a1 = 6;
		final int a4 = 8;

		if (a4 > 0) {
			final int k = 0;
			for (int a2 = 0; a2 < 10; a2++) {
				final int c;

				if (a1 == 6) {
					final boolean b = true;
					final boolean d = false;
					final int m = 1;
				}

				final AFunctionalInterface foo = (int a3) -> {
					final int b = a3;
				};

				final AFunctionalInterface foo2 = (int m) -> {
					final int b = m;
				};

				final AFunctionalInterface foo3 = (int k1) -> {
					final int b = k1;
				};

				final AFunctionalInterface foo4 = (int c1) -> {
					final int b = c1;
				};

				final AFunctionalInterface foo5 = (int l1) -> {
					final int b = l1;
				};
			}

			final int b;
		}

		final int a3 = 7;

		final AFunctionalInterface aFunctionalInterface2 = (int b) -> {
		};

	}

	public void genericAnonymousClassCreation(String input) {

		sampleMethodAcceptingFunction(new GenericFoo<String>() {
			@Override
			public String foo(String s, List<String> fooList) {
				fooList.add(s);
				return s;
			}
		});
	}

	public void nestedLambdaExpressions(String input) {
		final int repeatedName = 0;
		final AFunctionalInterface foo = (int repeatedName1) -> {
			if (repeatedName1 > 0) {

				final AFunctionalInterface innerFoo = (int repeatedName2) -> {
					int c = repeatedName2;
					c++;
				};
			}

		};
	}

	public void cascadedLambdaExpressions(String input) {
		final AFunctionalInterface foo = (int a) -> {
			if (a > 0) {
				final int b = a;
			}

		};

		final AFunctionalInterface innerFoo = (int a) -> {
			int b = a;
			b++;
		};
	}

	public String redeclaringLocalVariableInAnEnclosingScope(String input) {
		final String local = input;
		final int a = 0;
		final int toString = a;

		final AFunctionalInterface foo = (int a1) -> {
			final String toString1 = "toString";
			final String local1 = Integer.toString(a1);
			final String input1 = local1;
		};

		return local;
	}

	public String nestedRedeclaringLocalVariableInAnEnclosingScope(String input) {
		final String local = input;
		final int a = 0;
		final int toString = a;

		final AFunctionalInterface foo = (int a1) -> {
			final String toString1 = "toString";
			final String local1 = Integer.toString(a1);
			final String input1 = local1;

			final AFunctionalInterface foo1 = (int a2) -> {
				final String toString2 = "toString";
				final String local2 = Integer.toString(a2);
				final String input2 = local2;
			};
		};

		return local;
	}

	public String commentFreeAnonymousClass(String input) {

		final String local = input;
		final AFunctionalInterface fooComments = new AFunctionalInterface() {

			@Override
			public void method(int fooComments) {
				final String toString = "toString";

			}
			/* } */
		};

		final AFunctionalInterface fooComments2 = new AFunctionalInterface() {

			/**
			 * what happens with javadoc?
			 */
			@Override
			public void method(int fooComments) {
				final String toString = "toString";
			}

			// some important comment. shall not be removed!
		};

		final AFunctionalInterface fooComments3 = (int fooComments1) -> {
			final String toString = "toString";

		};

		final AFunctionalInterface fooComments4 = new AFunctionalInterface() {
			/* block comment */
			@Override
			public void method(int fooComments) {
				final String toString = "toString";

			}

		};

		final AFunctionalInterface fooComments5 = new AFunctionalInterface() {
			// line comment
			@Override
			public void method(int fooComments) {
				final String toString = "toString";

			}

		};

		return input;
	}

	public void renamingVarInCatchClause(String e) {
		final AFunctionalInterface foo = new AFunctionalInterface() {
			@Override
			public void method(int param) {
				final String toString = "toString";
				try {

				} catch (Exception e) {
					final String sthToLog = new StringBuilder().append(e.getMessage())
						.append(toString())
						.append(param)
						.toString();
				}

			}

		};
	}

	public void usingFunctionsWithTypeParameters() {
		// SIM-1889
		final FunctionWithTypeParameters foo = new FunctionWithTypeParameters() {
			@Override
			public String foo(int a, String b) {
				return "";
			}
		};
	}

	private void sampleMethodAcceptingFunction(GenericFoo foo) {
		foo.hashCode();
		// do nothing
	}

	private interface AFunctionalInterface {
		void method(int a);
	}

	private interface NonFunctionalInterface {
		void method();

		void method(int a);
	}

	private class MyClass {
		Runnable runnable;

		public MyClass(Runnable runnable) {
			this.runnable = runnable;
		}

		public void test() {
			runnable.run();
		}
	}

	private interface GenericFoo<T> {
		T foo(String t, List<T> fooList);
	}

	/**
	 * SIM-1889
	 */
	public interface FunctionWithTypeParameters {
		<T> T foo(int a, String b);
	}
}
