package at.splendit.simonykees.sample.preRule;

import java.util.Arrays;
import java.util.List;

@SuppressWarnings("nls")
public class StringBuildingLoopRule {
	
	public String collectionOfStrings(String input) {
		List<String> collectionOfStrings = generateStringList(input);
		String result = "";
		for(String val : collectionOfStrings) {
			result = result + val;
		}
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
