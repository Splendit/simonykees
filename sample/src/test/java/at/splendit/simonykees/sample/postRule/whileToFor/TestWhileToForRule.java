package at.splendit.simonykees.sample.postRule.whileToFor;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class TestWhileToForRule {

	@Test
	public void testWhileToFor() {
		List<String> l = new ArrayList<>();

		for (String s : l) {
		    System.out.println(s);
		}
	}
}
