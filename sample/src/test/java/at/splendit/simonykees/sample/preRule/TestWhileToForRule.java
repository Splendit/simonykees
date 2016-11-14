package at.splendit.simonykees.sample.preRule;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.junit.Test;

public class TestWhileToForRule {

	@Test
	public void testWhileToFor() {
		List<String> l = new ArrayList<>();

		Iterator<String> iterator = l.iterator();
		while (iterator.hasNext()) {
			String s = iterator.next();
			System.out.println(s);
		}
	}

	@Test
	public void testWhileToFor2() {
		List<String> l = new ArrayList<>();

		Iterator<String> iterator = l.iterator();
		String s;
		while ((s = iterator.next()) != null) {
			System.out.println(s);
		}
		while ((s = iterator.next()) != null) {
			System.out.println(s);
		}
		

	}
}
