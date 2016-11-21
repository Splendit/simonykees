package at.splendit.simonykees.sample.preRule;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@SuppressWarnings("nls")
public class TestForToForEachRuleTODO {

	public void testForToForEach2() {
		List<String> foo = new ArrayList<>();

		for (int i = 0; i < foo.size(); i++) {
			String s = foo.get(i);
		    System.out.println(s);
		}
	}
	
	public void testForToForEach3() {
		String[] ms = { "f", "oo" };
		
		for (int i = 0; i < ms.length; i++) {
			String s = ms[i];
			System.out.println(s);
		}
	}
}
