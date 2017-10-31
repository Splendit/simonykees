package eu.jsparrow.sample.preRule;

import java.io.IOException;
import java.util.Locale;
import java.util.function.Consumer;

@SuppressWarnings({"unused", "nls"})
public class TestStandardLoggerRule {

	private static String logger;
	
	private final String e = "I am not an exception";
	
	public void printingExceptionsAndPrimitives(String input) {
		Exception e = new Exception("Made up exception...");
		System.out.println(new Exception());
		System.out.println();
		System.out.println(6);
		System.out.println(new char[] {'c', 'd', 'e'});
		System.out.println(new Exception("adsfads"));
		System.out.println("adsfads");
		System.out.println(e);
		
		System.out.printf("");
		System.out.printf("%d - %d", 5, 6);
		System.out.printf(Locale.GERMANY, "%d - %d", 5, 6);
		System.out.printf(Locale.GERMANY, "%d - %d");
	}
	
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
	
	public void replaceSystemOutPrintFormatException(String input) {
		try {
			input.substring(5);
		} catch (Exception e) {
			System.out.printf("%d : " + e.getMessage(), 1);
			System.out.printf("%d : val %d : ", 1, 2);
			System.out.printf(Locale.FRANCE, "%d : val %d : ", 1, 2);
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
	
	public void replaceSystemOutPrintf(String input) {
		System.out.printf("%d : " + input , 1);
		System.out.printf(Locale.GERMANY, "%d : " + input, 1);
	}
	
	public void replaceSystemErrPrint(String input) {
		System.err.print(input);
	}
	
	public void replaceSystemErrPrintln(String input) {
		System.err.println(input);
	}
	
	public void replaceSystemErrPrintf(String input) {
		System.err.printf("%d : " + input , 1);
		System.err.printf(Locale.GERMANY, "%d : " + input, 1);
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
