package at.splendit.simonykees.sample.preRule;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.net.ssl.ExtendedSSLSession;

@SuppressWarnings({ "nls", "unused" })
public class TestForToForEachRuleTODO {
	
	//TODO add tests to integration

	public void testForToForEach22() {
		List<String> foo = new ArrayList<>();

		int i;
		for (i = 0; i < foo.size(); i++) {
			String s = foo.get(i);
			System.out.println(s);
		}
	}

	public void testForToForEach222() {
		List<String> foo = new ArrayList<>();

		int i;
		for (i = 0; i < foo.size(); i = i + 2) {
			String s = foo.get(i);
			System.out.println(s);
		}
	}

	public void testForToForEach2222() {
		List<? extends Number> foo = new ArrayList<>();

		int i;
		for (i = 0; i < foo.size(); i = i + 2) {
			Number s = foo.get(i);
			System.out.println(s);
		}
	}

	public void testForToForEach23() {
		List<String> foo = new ArrayList<>();

		int i, a;
		for (i = 0, a = 0; i < foo.size(); i++) {
			String s = foo.get(i);
			System.out.println(s);
		}
	}

	public void testForToForEach24() {
		List<String> foo = new ArrayList<>();

		int i, a;
		for (i = 0, a = 0; i < foo.size(); i++, a++) {
			String s = foo.get(i);
			System.out.println(s);
		}
	}

	public void testForToForEach25() {
		List<String> foo = new ArrayList<>();

		for (int i = 0, a = 0; i < foo.size(); i++, a++) {
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
