package at.splendit.simonykees.sample.postRule.stringBuildingLoopJ5;

import java.util.Arrays;
import java.util.List;

@SuppressWarnings("nls")
public class StringBuildingLoopRule {
	
	public String collectionOfStrings(String input) {
		List<String> collectionOfStrings = generateStringList(input);
		String result = "";
		StringBuilder resultSb = new StringBuilder();
		for(String val : collectionOfStrings) {
			resultSb.append(val);
		}
		result = resultSb.toString();
		return result;
	}
	
	public String joinCharacter(String input) {
		List<String> collectionOfStrings = generateStringList(input);
		String result = "";
		for(String val : collectionOfStrings) {
			result = result + "," + val;
		}
		return result;
	}

	private List<String> generateStringList(String input) {
		return Arrays.asList(input.split(","));
	}
}