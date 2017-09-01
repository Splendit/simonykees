package at.splendit.simonykees.sample.postRule.stringBuildingLoop;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@SuppressWarnings("nls")
public class StringBuildingLoopRule {
	
	public String collectionOfStrings(String input) {
		List<String> collectionOfStrings = generateStringList(input);
		String result = "";
		result += collectionOfStrings.stream().collect(Collectors.joining());
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
