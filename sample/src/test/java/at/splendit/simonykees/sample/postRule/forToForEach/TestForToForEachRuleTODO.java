package at.splendit.simonykees.sample.postRule.forToForEach;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@SuppressWarnings("nls")
public class TestForToForEachRuleTODO {

	public void testForToForEach2() {
		List<String> foo = new ArrayList<>();

		for(String s:foo){
			System.out.println(s);
		}
	}
	
	public void testForToForEach3() {
		String[] foo = { "f", "oo" };

		for(String s:foo){
			System.out.println(s);
		}
	}
}
