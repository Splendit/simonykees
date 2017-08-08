package at.splendit.simonykees.sample.preRule;

import java.util.Arrays;
import java.util.List;

public class EnhancedForLoopToStreamFindFirstRule {

	public String convertToFindFirstBreak(String input) {
		StringBuilder sb = new StringBuilder();
		String key = "";
		List<String> values = generateList(input);
		for(String value : values) {
		    if(value.length() > 4) {
		    	key = value;
		        break;
		    }
		}
		sb.append(key);
		
		return sb.toString();
	}
	
	public String convertToFindFirstReturn(String input) {
		List<String> values = generateList(input);
		System.out.println("I dont care what happens next!");
		for(String value : values) {
		    if(value.length() > 4) {
		    	return value;
		    }
		}
		
		return "";
	}

	private List<String> generateList(String input) {
		return Arrays.asList(input.split(","));
	}
}
