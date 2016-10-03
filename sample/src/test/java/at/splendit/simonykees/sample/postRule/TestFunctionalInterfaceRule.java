package at.splendit.simonykees.sample.postRule;

import org.junit.Test;

@SuppressWarnings("nls")
public class TestFunctionalInterfaceRule {

	@Test
	public void Test1() {

		Runnable runnable = ()->{
			System.out.println("xx");
		};

		runnable.run();

		MyClass mYClass = new MyClass(()->{
			System.out.println("xy");
		});

		mYClass.test();

		NonFunctionalInterface nonFunctionalInterface = new NonFunctionalInterface() {

			@Override
			public void method(int a) {
				System.out.println("zy");
			}

			@Override
			public void method() {
				System.out.println("xy");
			}
		};

		nonFunctionalInterface.method();
	}

	private interface NonFunctionalInterface {
		public void method();

		public void method(int a);
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
}
