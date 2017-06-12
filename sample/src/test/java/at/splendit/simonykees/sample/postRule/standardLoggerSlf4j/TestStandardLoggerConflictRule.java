package at.splendit.simonykees.sample.postRule.standardLoggerSlf4j;

@SuppressWarnings("nls")
public class TestStandardLoggerConflictRule {

	public void dontReplace(String input) {
		System.out.print(input);
	}
	
	class Logger {
		
		public void debut(String input) {
			System.out.println("input");
		}
		
		public void warn(String input) {
			System.err.println("input");
		}
		
		public void error(String input) {
			System.err.println("input");
		}
		
	}
}
