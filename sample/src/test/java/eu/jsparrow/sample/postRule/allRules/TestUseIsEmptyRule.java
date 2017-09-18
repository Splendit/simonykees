package eu.jsparrow.sample.postRule.allRules;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
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
		if (m.isEmpty()) {
		}

		Collection<String> l = new ArrayList<>();
		if (l.isEmpty()) {
		}

		String s = "";
		if (s.isEmpty()) {
		}
	}

	public void withSwitchedOperands() {
		Map<String, String> m = new HashMap<>();
		if (m.isEmpty()) {
		}

		Collection<String> l = new ArrayList<>();
		if (l.isEmpty()) {
		}

		String s = "";
		if (s.isEmpty()) {
		}
	}

	public void withOddNumbers() {
		String s = "";
		if (s.isEmpty()) {
		}
		if (s.isEmpty()) {
		}
		if (s.isEmpty()) {
		}
		if (s.isEmpty()) {
		}
		if (s.isEmpty()) {
		}
		if (s.isEmpty()) {
		}
	}

	public void withOdderNumbers() {
		String s = "";
		if (s.isEmpty()) {
		}
		if (s.isEmpty()) {
		}
		if (s.isEmpty()) {
		}
		if (s.isEmpty()) {
		}
		if (s.isEmpty()) {
		}
		if (s.isEmpty()) {
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
