package at.splendit.simonykees.sample.preRule;

/**
 * @author Martin Huter
 *
 */

@SuppressWarnings("nls")
public class StringUtilsRefactorRule {

	public boolean testEmpty(String testString) {
		return testString.isEmpty();
	}

	public String testTrim(String testString) {
		return testString.trim();
	}

	public boolean testEquals(String testString) {
		String sometimesExpectedString = testString.replaceAll("a", "b");
		
		return testString.equals(sometimesExpectedString);
	}

	public boolean testEqualsIgnoreCase(String testString) {
		String sometimesExpectedString = testString.replaceAll("a", "b");
		
		return testString.equalsIgnoreCase(sometimesExpectedString);
	}

	public boolean testEndsWith(String testString) {
		String sometimesExpectedString = "With";

		return testString.endsWith(sometimesExpectedString);
	}

	public boolean testStartWith(String testString) {
		String sometimesExpectedString = "start";

		return testString.startsWith(sometimesExpectedString);
	}

	public int testIndexOf(String testString) {
		return testString.indexOf("e");
	}

	public boolean testContains(String testString) {
		String sometimesExpectedString = "tain";

		return testString.contains(sometimesExpectedString);
	}

	public String testReplace(String testString) {
		return testString.replace("M", "m");
	}

	public String testLowerCase(String testString) {
		return testString.toLowerCase();
	}

	public String testUpperCase(String testString) {
		return testString.toUpperCase();
	}

	public String[] testSplit(String testString) {
		return testString.split(",");
	}
	
}
