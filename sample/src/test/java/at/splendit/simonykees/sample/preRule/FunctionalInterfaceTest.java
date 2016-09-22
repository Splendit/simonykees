package at.splendit.simonykees.sample.preRule;


import org.junit.Test;

@SuppressWarnings("nls")
public class FunctionalInterfaceTest {

	@Test
	public void Test1() {

		Runnable runnable = new Runnable() {
			
			@Override
			public void run() {
				System.out.println("xx");
			}
		};

		MyClass mYClass = new MyClass(new Runnable() {
			
			@Override
			public void run() {
				System.out.println("xy");
			}
		});
		
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
