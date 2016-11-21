package at.splendit.simonykees.sample.postRule.allRules;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("nls")
public class TestForToForEachRule {

	public void testForToForEach() {
		List<String> foo = new ArrayList<>();

		for (String s : foo) {
			System.out.println(s);
		}
	}
}
