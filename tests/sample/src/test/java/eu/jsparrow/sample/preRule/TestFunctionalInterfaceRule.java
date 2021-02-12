package eu.jsparrow.sample.preRule;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings({ "nls", "unused", "rawtypes" })
public class TestFunctionalInterfaceRule {
	
	private static Logger log = LoggerFactory.getLogger(TestFunctionalInterfaceRule.class);
	
	private final String FINAL_STRING_FIELD;
	private final String NOT_INITIALIZED_FIELD;
	
	private AFunctionalInterface usingUnDeclaredField = new AFunctionalInterface() {
		@Override
		public void method(int a) {
			String s = FINAL_INITIALIZED_STRING_FIELD;
		}
	};

	private AFunctionalInterface reusingFieldDuringInitialization = new AFunctionalInterface() {
		@Override
		public void method(int a) {
			reusingFieldDuringInitialization.toString();
		}
	};

	private final String FINAL_INITIALIZED_STRING_FIELD = "initialized";
	
	private AFunctionalInterface usingUnInitializedField = new AFunctionalInterface() {
		@Override
		public void method(int a) {
			String s = FINAL_STRING_FIELD;
		}
	};
	
	private AFunctionalInterface usingInitializedField =  new AFunctionalInterface() {

		@Override
		public void method(int a) {
			/*
			 * Using initialized field
			 */
			String s = FINAL_INITIALIZED_STRING_FIELD;
		}
		
	};
	
	private AFunctionalInterface usingWildcardsInBody =  new AFunctionalInterface() {

		@Override
		public void method(int a) {
			List<List<? extends Number>> numbers = new ArrayList<>();
			numbers.stream().map(List::hashCode).mapToInt(Integer::intValue).sum();
		}
	};
	
	public TestFunctionalInterfaceRule() {
		AFunctionalInterface foo = new AFunctionalInterface() {
			
			@Override
			public void method(int a) {
				String sthToLog = a + FINAL_STRING_FIELD;
				
			}
		};
		FINAL_STRING_FIELD = "irritating";
		
		AFunctionalInterface foo2 = new AFunctionalInterface() {
			
			@Override
			public void method(int a) {
				String sthToLog = a + FINAL_STRING_FIELD;
				
			}
		};
		
		AFunctionalInterface foo3 = new AFunctionalInterface() {
			@Override
			public void method(int a) {
				String t = declaredfterConstructor;
				
			}
		};
		
		if(foo3 != null) {
			AFunctionalInterface foo4 = new AFunctionalInterface() {
				@Override
				public void method(int a) {
					String sthToLog = a + NOT_INITIALIZED_FIELD;
				}
			};
		} else {
			AFunctionalInterface foo5 = new AFunctionalInterface() {
				@Override
				public void method(int a) {
					String sthToLog = a + NOT_INITIALIZED_FIELD;
				}
			};
		}
		
		if(foo != null) {
			NOT_INITIALIZED_FIELD = "";
			AFunctionalInterface inNestedBlock = new AFunctionalInterface() {
				
				@Override
				public void method(int a) {
					String sthToLog = a + NOT_INITIALIZED_FIELD;
					
				}
			};
		} else {
			NOT_INITIALIZED_FIELD = "";
		}
		
	}
	
	private final String declaredfterConstructor = "declaredAfterCtor"; 
	
	public void usingUnassignedFieldInMethod() {
		AFunctionalInterface foo2 = new AFunctionalInterface() {
			
			@Override
			public void method(int a) {
				String sthToLog = a + FINAL_STRING_FIELD;
				
			}
		};
	}

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
	
	public String redeclaringLocalVariableInAnEnclosingScope(String input) {
		String local = input;
		int a = 0;
		int toString = a;
		
		AFunctionalInterface foo = new AFunctionalInterface() {
			
			@Override
			public void method(int a) {
				String toString = "toString";
				String local = Integer.toString(a);
				String input = local;
			}
		};
		
		return local;
	}
	
	public String nestedRedeclaringLocalVariableInAnEnclosingScope(String input) {
		String local = input;
		int a = 0;
		int toString = a;
		
		AFunctionalInterface foo = new AFunctionalInterface() {
			
			@Override
			public void method(int a) {
				String toString = "toString";
				String local = Integer.toString(a);
				String input = local;
				
				AFunctionalInterface foo = new AFunctionalInterface() {
					
					@Override
					public void method(int a) {
						String toString = "toString";
						String local = Integer.toString(a);
						String input = local;
					}
				};
			}
		};
		
		return local;
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
			   
			// some important comment. shall not be removed! 
		}; 
		
		AFunctionalInterface fooComments3 = new AFunctionalInterface() { 
			 
		@Override 
		public void method(int fooComments) { 
	        String toString = "toString"; 
	        
		  }
		
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
	
	public void renamingVarInCatchClause(String e) {
		AFunctionalInterface foo = new AFunctionalInterface() {
		@Override 
		public void method(int param) { 
	        String toString = "toString"; 
	        try {
	        	
	        } catch(Exception e) {
	        	String sthToLog = e.getMessage() + toString() + param;
	        }
	        
		  }
		
		}; 
	}

	public void usingFunctionsWithTypeParameters() {
		// SIM-1889
		FunctionWithTypeParameters foo = new FunctionWithTypeParameters() {
			@Override
			public String foo(int a, String b) {
				return "";
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
		foo.hashCode();
		// do nothing
	}

	/**
	 * SIM-1889
	 */
	public interface FunctionWithTypeParameters {
		<T> T foo (int a, String b);
	}
}
