package at.splendit.simonykees.sample.preRule;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

@SuppressWarnings({ "nls", "unused", "rawtypes" })
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
	
	int a;
	AFunctionalInterface aFunctionalInterface = new AFunctionalInterface() {
		@Override
		public void method(int a) {
		}
	};
	
	{
		int a;
		AFunctionalInterface aFunctionalInterface = new AFunctionalInterface() {
			@Override
			public void method(int a) {
			}
		};
	}
	
	public void clashingLocalVariableNames(int l) {
		int a, a1; 
		a = 5;
		a1 = 6;
		int a4 = 8;

		if(a4 > 0) {
			int k = 0;
			for(int a2 = 0; a2 < 10; a2++) {
				int c;
				
				if(a1 == 6) {
					boolean b = true;
					boolean d = false;
					int m = 1;
				}
				
				AFunctionalInterface foo = new AFunctionalInterface() {
					@Override 
					public void method(int a) {
						int b = a; 
					} 
				};
				
				AFunctionalInterface foo2 = new AFunctionalInterface() {
					@Override 
					public void method(int m) {
						int b = m; 
					} 
				};
				
				AFunctionalInterface foo3 = new AFunctionalInterface() {
					@Override 
					public void method(int k) {
						int b = k; 
					} 
				};
				
				AFunctionalInterface foo4 = new AFunctionalInterface() {
					@Override 
					public void method(int c) {
						int b = c; 
					} 
				};
				
				AFunctionalInterface foo5 = new AFunctionalInterface() {
					@Override 
					public void method(int l) {
						int b = l; 
					} 
				};
			}
			
			int b;
		}

		int a3 = 7;
		
		AFunctionalInterface aFunctionalInterface2 = (int b) -> {
		}; 

	} 
	
	public void genericAnonymousClassCreation(String input) {
		
		sampleMethodAcceptingFunction(			
				new GenericFoo<String>() {
				@Override
				public String foo(String s, List<String>fooList) {
					fooList.add(s);
					return s;
				}
			});
	}
	
	public void nestedLambdaExpressions(String input) {
		int repeatedName = 0;
		AFunctionalInterface foo = new AFunctionalInterface() {

			@Override
			public void method(int repeatedName) {
				if(repeatedName > 0) {

					AFunctionalInterface innerFoo = new AFunctionalInterface() {
						
						@Override
						public void method(int repeatedName) {
							int c = repeatedName;
							c++;
						}
					};
				}
				
			}
			
		};
	}
	
	public void cascadedLambdaExpressions(String input) {
		AFunctionalInterface foo = new AFunctionalInterface() {

			@Override
			public void method(int a) {
				if(a > 0) {
					int b = a;
				}
				
			}
			
		};

		AFunctionalInterface innerFoo = new AFunctionalInterface() {
			
			@Override
			public void method(int a) {
				int b = a;
				b++;
			}
		};
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
	
	private interface GenericFoo<T> {
		T foo(String t, List<T>fooList);
	}
	
	private void sampleMethodAcceptingFunction(GenericFoo foo) {
		// do nothing
	}
}
