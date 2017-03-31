package at.splendit.simonykees.sample.postRule.functinalInterface;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

@SuppressWarnings({ "nls", "unused", "rawtypes" })
public class TestFunctionalInterfaceRule {
	
	private static Logger log = LogManager.getLogger(TestFunctionalInterfaceRule.class);

	@Test
	public void test1() {

		Runnable runnable = ()->{
			log.debug("xx");
		};

		runnable.run();

		MyClass mYClass = new MyClass(()->{
			log.debug("xy");
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

		AFunctionalInterface aFunctionalInterface = (int a)->{
		};

		AFunctionalInterface aFunctionalInterface2 = (int a) -> {
		};

		aFunctionalInterface.method(0);
	}
	
	int a;
	AFunctionalInterface aFunctionalInterface = (int a)->{
	};
	
	{
		int a;
		AFunctionalInterface aFunctionalInterface = (int a1)->{
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
				
				AFunctionalInterface foo = (int a3)->{
					int b = a3; 
				};
				
				AFunctionalInterface foo2 = (int m)->{
					int b = m; 
				};
				
				AFunctionalInterface foo3 = (int k1)->{
					int b = k1; 
				};
				
				AFunctionalInterface foo4 = (int c1)->{
					int b = c1; 
				};
				
				AFunctionalInterface foo5 = (int l1)->{
					int b = l1; 
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
		AFunctionalInterface foo = (int repeatedName1)->{
			if(repeatedName1 > 0) {

				AFunctionalInterface innerFoo = (int repeatedName2)->{
					int c = repeatedName2;
					c++;
				};
			}
			
		};
	}
	
	public void cascadedLambdaExpressions(String input) {
		AFunctionalInterface foo = (int a)->{
			if(a > 0) {
				int b = a;
			}
			
		};

		AFunctionalInterface innerFoo = (int a)->{
			int b = a;
			b++;
		};
	}
	
	public String commentFreeAnonymousClass(String input) { 
		  
		String local = input; 
		AFunctionalInterface fooComments = new AFunctionalInterface() { 
	 
		@Override 
		public void method(int fooComments) { 
	        String toString = "toString"; 
	        
		  }
		/* } */ 
		}; 
		     
		AFunctionalInterface fooComments2 = new AFunctionalInterface() {
		 
			/** 
			* what happens with javadoc? 
			*/ 
			@Override 
			public void method(int fooComments) {
			    String toString = "toString"; 
			} 
			   
			//TODO: some important comment. shall not be removed! 
		}; 
		
		AFunctionalInterface fooComments3 = (int fooComments1)->{ 
	        String toString = "toString"; 
	        
		  };
		
		AFunctionalInterface fooComments4 = new AFunctionalInterface() {
		/* block comment */ 
		@Override 
		public void method(int fooComments) { 
	        String toString = "toString"; 
	        
		  }
		
		}; 
		
		AFunctionalInterface fooComments5 = new AFunctionalInterface() {
		// line comment
		@Override 
		public void method(int fooComments) { 
	        String toString = "toString"; 
	        
		  }
		
		}; 
	
	return input; 
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
