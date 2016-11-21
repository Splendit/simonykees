package at.splendit.simonykees.sample.postRule.allRules;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class TestWhileToForRule {

	public void testWhileToFor() {
		List<String> l = new ArrayList<>();

		for (String s : l) {
			System.out.println(s);
		}
	}

	public void testWhileToFor2() {
		List<String> l = new ArrayList<>();

		Iterator<String> iterator = l.iterator();
		while (iterator.hasNext()) {
			String s = iterator.next();
			s = iterator.next();
			System.out.println(s);
		}
	}

	public void testWhileToFor3() {
		List<String> l = new ArrayList<>();

		Iterator<String> iterator = l.iterator();
		while (iterator.hasNext()) {
			String s = iterator.next();
			s = iterator.next();
			s = iterator.next();
			System.out.println(s);
		}
	}

	public void testWhileToFor4() {
		List<String> l = new ArrayList<>();

		Iterator<String> iterator = l.iterator();
		while (iterator.hasNext()) {
			String s = iterator.next();
			System.out.println(s);
			iterator.remove();
			iterator.forEachRemaining(null);
		}
	}

	public void testWhileToFor5() {
		List<String> l = new ArrayList<>();

		for (String s : l) {
			Object k;
			System.out.println(s);
		}
	}

	public void testWhileToFor6() {
		List<String> l = new ArrayList<>();

		Iterator<String> iterator = l.iterator();
		String s;
		s = "lalelu";
		while (iterator.hasNext()) {
			Object k;
			s = iterator.next();
			System.out.println(s);
		}
	}

	public void testWhileToFor7() {
		List<String> l = new ArrayList<>();

		Iterator<String> iterator = l.iterator();
		String s;
		while (iterator.hasNext()) {
			Object k;
			s = iterator.next();
			System.out.println(s);
		}
		s = "lalelu";
	}

	public void testNextOnlyIterator() {
		List<String> stringList = new ArrayList<>();

		for (String s : stringList) {
			System.out.println(s);
		}
	}
}
