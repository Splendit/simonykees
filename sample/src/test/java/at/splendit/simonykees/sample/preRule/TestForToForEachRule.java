package at.splendit.simonykees.sample.preRule;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@SuppressWarnings("nls")
public class TestForToForEachRule {
	
	public void testForToForEach() {
		List<String> foo = new ArrayList<>();

		for (Iterator<String> iterator = foo.iterator(); iterator.hasNext(); ) {
		    String s = iterator.next();
			System.out.println(s);
		}
	}
}
