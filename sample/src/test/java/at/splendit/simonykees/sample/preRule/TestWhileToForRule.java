package at.splendit.simonykees.sample.preRule;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class TestWhileToForRule {

	public void testWhileToFor() {
		List<String> l = new ArrayList<>();

		Iterator<String> iterator = l.iterator();
		while (iterator.hasNext()) {
			String s = iterator.next();
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

		Iterator<String> iterator = l.iterator();
		String s;
		while ((s = iterator.next()) != null) {
			System.out.println(s);
		}
	}
}
