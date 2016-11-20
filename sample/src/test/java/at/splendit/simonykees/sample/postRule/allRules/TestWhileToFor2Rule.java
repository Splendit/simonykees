package at.splendit.simonykees.sample.postRule.allRules;

import java.util.ArrayList;
import java.util.List;

public class TestWhileToFor2Rule {
	public void testNextOnlyIterator() {
		List<String> stringList = new ArrayList<>();

		for (String s : stringList) {
			System.out.println(s);
		}
	}
}
