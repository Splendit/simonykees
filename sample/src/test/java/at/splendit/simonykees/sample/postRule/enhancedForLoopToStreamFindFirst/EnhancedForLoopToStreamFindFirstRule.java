package at.splendit.simonykees.sample.postRule.enhancedForLoopToStreamFindFirst;

import java.util.Arrays;
import java.util.List;

public class EnhancedForLoopToStreamFindFirstRule {

	public String convertToFindFirstBreak(String input) {
		StringBuilder sb = new StringBuilder();
		List<String> values = generateList(input);
		String key = values.stream().filter(value -> value.length() > 4).findFirst().orElse("");
		sb.append(key);
		
		return sb.toString();
	}
	
	public String convertToFindFirstReturn(String input) {
		List<String> values = generateList(input);
		System.out.println("I dont care what happens next!");
		return values.stream().filter(value -> value.length() > 4).findFirst().orElse("");
	}

	private List<String> generateList(String input) {
		return Arrays.asList(input.split(","));
	}
}
