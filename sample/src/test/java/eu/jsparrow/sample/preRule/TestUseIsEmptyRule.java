package eu.jsparrow.sample.preRule;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Corner cases for StringLiteralEqualityCheckRule.
 * 
 * @author Hans-Jörg Schrödl
 * @since 2.0.3
 */
@SuppressWarnings({ "nls", "unused" })
public class TestUseIsEmptyRule {

	public void withDefaultInteger() {
		Map<String, String> m = new HashMap<>();
		if (m.size() == 0) {
		}

		Collection<String> l = new ArrayList<>();
		if (l.size() == 0) {
		}

		String s = "";
		if (s.length() == 0) {
		}
	}

	public void withSwitchedOperands() {
		Map<String, String> m = new HashMap<>();
		if (0 == m.size()) {
		}

		Collection<String> l = new ArrayList<>();
		if (0 == l.size()) {
		}

		String s = "";
		if (0 == s.length()) {
		}
	}

	public void withOddNumbers() {
		String s = "";
		if (s.length() == -0) {
		}
		if (s.length() == +0) {
		}
		if (s.length() == 0.0f) {
		}
		if (s.length() == -0.0f) {
		}
		if (s.length() == 0.0d) {
		}
		if (s.length() == -0.0d) {
		}
	}
	
	public void withOdderNumbers() {
		String s = "";
		if (s.length() == -0000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000) {
		}
		if (s.length() == +0000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000) {
		}
		if (s.length() == 0.0000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000f) {
		}
		if (s.length() == -0.0000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000f) {
		}
		if (s.length() == 0.0000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000d) {
		}
		if (s.length() == -0.0000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000d) {
		}
	}
	
	public void withOdderNumbersNoChange() {
		String s = "";
		if (s.length() == -0000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000001) {
		}
		if (s.length() == +0000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000001) {
		}
		if (s.length() == 0.000000000000000000000000000000000000000000001f) {
		}
		if (s.length() == -0.000000000000000000000000000000000000000000001f) {
		}
		if (s.length() == 0.0000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000001d) {
		}
		if (s.length() == -0.0000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000001d) {
		}
	}

	public void withOthersShouldNotChange() {
		String s = "";
		int i = 0;
		if (s.length() == i) {
		}
		if (s.length() == 0.1d) {
		}
		if (s.length() == -0.1f) {
		}
	}
}
