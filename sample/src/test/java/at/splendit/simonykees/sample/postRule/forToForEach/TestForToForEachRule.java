package at.splendit.simonykees.sample.postRule.forToForEach;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

@SuppressWarnings("nls")
public class TestForToForEachRule {
	
	private List<String> generateList(String input) {
		return Arrays.asList(input.split(";"));
	}
	
	public String testForToForEach(String input) {
		List<String> foo = generateList(input);
		StringBuilder sb = new StringBuilder();

		for (String s:foo){
		    sb.append(s);
		}
		return sb.toString();
	}
}
