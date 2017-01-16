package at.splendit.simonykees.sample.postRule.allRules;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

@SuppressWarnings({ "nls", "unused" })
public class TestWhileToForRule {

	private List<String> generateList(String input) {
		return Arrays.asList(input.split(";")); //$NON-NLS-1$
	}

	public String testWhileToFor(String input) {
		List<String> l = generateList(input);
		StringBuilder sb = new StringBuilder();

		for (String s : l) {
			sb.append(s);
		}
		return sb.toString();
	}

	public String testWhileToFor2(String input) {
		List<String> l = generateList(input);
		StringBuilder sb = new StringBuilder();

		Iterator<String> iterator = l.iterator();
		while (iterator.hasNext()) {
			String s = iterator.next();
			s = iterator.next();
			sb.append(s);
		}
		return sb.toString();
	}

	public String testWhileToFor3(String input) {
		List<String> l = generateList(input);
		StringBuilder sb = new StringBuilder();

		Iterator<String> iterator = l.iterator();
		while (iterator.hasNext()) {
			String s = iterator.next();
			s = iterator.next();
			s = iterator.next();
			sb.append(s);
		}
		return sb.toString();
	}

	public String testWhileToFor4(String input) {
		List<String> l = generateList(input);
		StringBuilder sb = new StringBuilder();

		Iterator<String> iterator = l.iterator();
		while (iterator.hasNext()) {
			String s = iterator.next();
			sb.append(s);
			iterator.remove();
			iterator.forEachRemaining(null);
		}
		return sb.toString();
	}

	public String testWhileToFor5(String input) {
		List<String> l = generateList(input);
		StringBuilder sb = new StringBuilder();

		for (String s : l) {
			Object k;
			sb.append(s);
		}
		return sb.toString();
	}

	public String testWhileToFor6(String input) {
		List<String> l = generateList(input);
		StringBuilder sb = new StringBuilder();

		Iterator<String> iterator = l.iterator();
		String s;
		s = "lalelu";
		while (iterator.hasNext()) {
			Object k;
			s = iterator.next();
			sb.append(s);
		}
		return sb.toString();
	}

	public String testWhileToFor7(String input) {
		List<String> l = generateList(input);
		StringBuilder sb = new StringBuilder();

		Iterator<String> iterator = l.iterator();
		String s;
		while (iterator.hasNext()) {
			Object k;
			s = iterator.next();
			sb.append(s);
		}
		s = "lalelu";
		return sb.toString();
	}

	public String testWhileToFor8(String input) {
		List<String> l = generateList(input);
		StringBuilder sb = new StringBuilder();

		Iterator<String> iterator = l.iterator();
		String s = "";
		while (iterator.hasNext()) {
			Object k;
			s = iterator.next();
			sb.append(s);
		}
		sb.append(s);
		return sb.toString();
	}

	public String testNextOnlyIterator(String input) {
		List<String> l = generateList(input);
		StringBuilder sb = new StringBuilder();

		Iterator<String> stringIterator = l.iterator();
		String s = null;
		while ((s = stringIterator.next()) != null) {
			sb.append(s);
		}
		return sb.toString();
	}
}
