package at.splendit.simonykees.sample.preRule;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

@SuppressWarnings("nls")
public class TestFunctionalInterfaceRule {
	
	private static Logger log = LogManager.getLogger(TestFunctionalInterfaceRule.class);

	@Test
	public void test1() {

		Runnable runnable = new Runnable() {

			@Override
			public void run() {
				log.debug("xx");
			}
		};

		runnable.run();

		MyClass mYClass = new MyClass(new Runnable() {

			@Override
			public void run() {
				log.debug("xy");
			}
		});

		mYClass.test();

		NonFunctionalInterface nonFunctionalInterface = new NonFunctionalInterface() {

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

		AFunctionalInterface aFunctionalInterface = new AFunctionalInterface() {
			@Override
			public void method(int a) {
			}
		};

		AFunctionalInterface aFunctionalInterface2 = (int a) -> {
		};

		aFunctionalInterface.method(0);
	}

	private interface AFunctionalInterface {
		public void method(int a);
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
