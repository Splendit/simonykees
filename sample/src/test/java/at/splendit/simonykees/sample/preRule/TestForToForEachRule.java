package at.splendit.simonykees.sample.preRule;

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

		for (Iterator<String> iterator = foo.iterator(); iterator.hasNext(); ) {
		    String s = iterator.next();
		    sb.append(s);
		}
		return sb.toString();
	}
	
	public void testForToForEach2(String input) {
		List<String> foo = generateList(input);
		StringBuilder sb = new StringBuilder();

		for (int i = 0; i < foo.size(); i++) {
			String s = foo.get(i);
		    sb.append(s);
		    sb.append(foo.get(i));
		}
	}
}
