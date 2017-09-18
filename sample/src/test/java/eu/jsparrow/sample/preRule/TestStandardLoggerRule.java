package eu.jsparrow.sample.preRule;

import java.util.function.Consumer;

@SuppressWarnings({"unused", "nls"})
public class TestStandardLoggerRule {

	private static String logger;
	
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
