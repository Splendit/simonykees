package eu.jsparrow.sample.preRule;

import java.util.function.Consumer;

@SuppressWarnings({"unused", "nls"})
public class TestStandardLoggerRule {

	private static String logger;
	
	private final String e = "I am not an exception";
	
	public void replaceSystemOutPrintingException(String input) {
		try {
			input.substring(5);
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}
	
	public void replaceSystemErrPrintingException(String input) {
		try {
			input.substring(5);
		} catch (Exception e) {
			System.err.println(e.getMessage());
		}
	}
	
	public void insertLoggingStatementInEmptycatchClasuse(String input) {
		try {
			input.substring(5);
		} catch (Exception e) {
			
		}
	}
	
	public void insertMissingLoggingStatementInCatchClasuse(String input) {
		try {
			input.substring(5);
		} catch (Exception e) {
			/*
			 * The catch clause is not empty, but the exception is not logged.
			 */
			
			System.out.println("Nothing to show");
		}
	}
	
	public void distinguishLoggedExceptionFromField(String input) {
		try {
			input.substring(5);
		} catch (Exception e) {
			/*
			 * The catch clause is not empty, but the exception is not logged.
			 */
			
			System.out.println(this.e);
		}
	}
	
	public void nestedCatchClauses(String input) {
		try {
			input.substring(5);
		} catch (Exception e) {
			try {
				input.substring(5);
			} catch (Exception e1) {
				System.out.println(e.getMessage());
			}
		}
	}
	
	public void replaceSystemOutPrint(String input) {
		System.out.print(input);
	}
	
	public void replaceSystemOutPrintln(String input) {
		System.out.println(input);
	}
	
	public void replaceSystemErrPrint(String input) {
		System.err.print(input);
	}
	
	public void replaceSystemErrPrintln(String input) {
		System.err.println(input);
	}
	
	public void replacePrintStackTrace(String input) {
		try {
			input.substring(5);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	class InnerClass {
		
		{{
			System.out.println("a log message");
		}}
		
		Consumer<String> p = (String input) -> {
			System.out.println(input);
		};
		
		public void dontUseOuterClassLogger(String input) {
			System.out.println("a log message");
		}
		
		class DoubleNestedInnerClass {
			
			public void loggerInDoubleNestedClass(String input) {
				System.out.println(input);
			}
			
			public void useDeepNestedLogger(String input) {
				System.out.println(input);
			}
		}
		
		public void useCorrectLogger(String input) {
			System.out.println(input);
		}
	}
	
	public void replaceAfterInnerClass(String input) {
		System.out.println(input);
	}
}

class TopLevelClass {
	
	public void replaceSystemOutPrint(String input) {
		System.out.print(input);
	}
	
	public void replaceSystemErrPrintln(String input) {
		System.err.println(input);
	}
	
	public void replacePrintStackTrace(String input) {
		try {
			input.substring(5);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

@SuppressWarnings({"unused", "nls"})
enum Days {
	Mon, Tue, Thu, Frii, Saaat, Sun;

	private static final String logger = "";
	public void loggerInEnumType(String input) {
		System.out.println(input);
	}
}


interface OneHavingAnImplementedMethod {
	
	default void makeUseOfSystemOut(String input) {
		System.out.println(input);
	}
}
