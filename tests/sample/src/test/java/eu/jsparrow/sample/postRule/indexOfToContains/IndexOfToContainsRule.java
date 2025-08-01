package eu.jsparrow.sample.postRule.indexOfToContains;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author Matthias Webhofer
 * @since 2.1.1
 *
 */
@SuppressWarnings("nls")
public class IndexOfToContainsRule {

	public void testList() {
		List<String> l = new ArrayList<>();
		String s = "searchString";

		// not contains
		if (!l.contains(s)) {
			l.add(s);
		}

		if (!l.contains(s)) {
			l.add(s);
		}

		if (!l.contains(s)) {
			l.add(s);
		}

		if (!l.contains(s)) {
			l.add(s);
		}

		if (!l.contains(s)) {
			l.add(s);
		}

		if (!l.contains(s)) {
			l.add(s);
		}

		if (!l.contains(s)) {
			l.add(s);
		}

		// contains
		if (l.contains(s)) {
			l.remove(s);
		}

		if (l.contains(s)) {
			l.remove(s);
		}

		if (l.contains(s)) {
			l.remove(s);
		}

		if (l.contains(s)) {
			l.remove(s);
		}

		if (l.contains(s)) {
			l.remove(s);
		}

		if (l.contains(s)) {
			l.remove(s);
		}

		if (l.contains(s)) {
			l.remove(s);
		}

		// other cases without transformation
		if (l.indexOf(s) == 0) {
			l.remove(s);
		}

		if (l.indexOf(s) == 1) {
			l.remove(s);
		}

		if (l.indexOf(s) > 0) {
			l.remove(s);
		}

		if (0 == l.indexOf(s)) {
			l.remove(s);
		}

		if (1 == l.indexOf(s)) {
			l.remove(s);
		}

		if (0 < l.indexOf(s)) {
			l.remove(s);
		}

		int i = 0;
		while ((i = l.indexOf(s)) != -1) {
			System.out.println(s);
		}

		while ((i = l.indexOf(s)) >= 0) {
			System.out.println(s);
		}

		int index = l.indexOf(s);
		System.out.println("object found at index: " + index);
		
		/*
		 * Some cases with comments
		 */
		
		/* saving comments */
		if (!l.contains(s)) {
			l.add(s);
		}
		
		// index of
		if (!l // index of
.contains(s)) {
			l.add(s);
		}
		
		// I don't want to break anything
		if (!l // I don't want to break anything
.contains(s)) {
			l.add(s);
		}
	}

	public void testString() {
		String s = "Hello World";

		// not contains
		if (!s.contains("ello")) {
			System.out.println(s);
		}

		if (!s.contains("ello")) {
			System.out.println(s);
		}

		if (!s.contains("ello")) {
			System.out.println(s);
		}

		if (!s.contains("ello")) {
			System.out.println(s);
		}

		if (!s.contains("ello")) {
			System.out.println(s);
		}

		if (!s.contains("ello")) {
			System.out.println(s);
		}

		if (!s.contains("ello")) {
			System.out.println(s);
		}

		// contains
		if (s.contains("ello")) {
			System.out.println(s);
		}

		if (s.contains("ello")) {
			System.out.println(s);
		}

		if (s.contains("ello")) {
			System.out.println(s);
		}

		if (s.contains("ello")) {
			System.out.println(s);
		}

		if (s.contains("ello")) {
			System.out.println(s);
		}

		if (s.contains("ello")) {
			System.out.println(s);
		}

		if (s.contains("ello")) {
			System.out.println(s);
		}
		
		/* save me */
		if (s.contains("ello")) {
			System.out.println(s);
		}

		// char as argument of indexOf
		if (s.indexOf('l') != -1) {
			System.out.println(s);
		}

		if (s.indexOf('l') >= 0) {
			System.out.println(s);
		}

		// other cases without transformation
		if (s.indexOf("ello") == 0) {
			System.out.println(s);
		}

		if (s.indexOf("ello") == 1) {
			System.out.println(s);
		}

		if (s.indexOf("ello") > 0) {
			System.out.println(s);
		}

		if (0 == s.indexOf("ello")) {
			System.out.println(s);
		}

		if (1 == s.indexOf("ello")) {
			System.out.println(s);
		}

		if (0 < s.indexOf("ello")) {
			System.out.println(s);
		}

		int i = 0;
		while ((i = s.indexOf("ello")) != -1) {
			System.out.println(s);
		}

		while ((i = s.indexOf("ello")) >= 0) {
			System.out.println(s);
		}

		int index = s.indexOf("ello");
		System.out.println("substring found at index: " + index);
	}
}
