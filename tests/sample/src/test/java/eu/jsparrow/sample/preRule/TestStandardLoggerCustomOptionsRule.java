package eu.jsparrow.sample.preRule;

public class TestStandardLoggerCustomOptionsRule {
	
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
}
