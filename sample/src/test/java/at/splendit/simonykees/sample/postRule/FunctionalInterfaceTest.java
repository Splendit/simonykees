package at.splendit.simonykees.sample.postRule;

import org.junit.Test;

import at.splendit.test.runtimeEclipsePlayground.functionalInterface.AnFunctionalInterface;
import at.splendit.test.runtimeEclipsePlayground.functionalInterface.NonFunctinalInterface;

public class FunctionalInterfaceTest {

	@Test
	public void Test1() {

		Runnable runnable = () -> {
			System.out.println("xx");
			System.out.println("xy");
			System.out.println("xz");
		};

		MyClass mYClass = new MyClass(() -> {
			System.out.println("xx");
			System.out.println("xy");
			System.out.println("xz");

		});

		NonFunctionalInterface anFunctinalInterface = new NonFunctionalInterface() {

			@Override
			public void method(int a) {
				System.out.println("zy");				
			}
			
			@Override
			public void method() {
				System.out.println("xy");				
			}
		};
	}
	
	private interface NonFunctionalInterface {
		public void method();
		public void method(int a);
	}

	private class MyClass {
		public MyClass(Runnable runnable) {

		}
	}
}
