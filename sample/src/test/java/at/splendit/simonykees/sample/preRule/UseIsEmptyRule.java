package at.splendit.simonykees.sample.preRule;

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
public class UseIsEmptyRule {

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
		if (s.length() == 0.0f) {
		}
		if (s.length() == 0.0d) {
		}
	}
}
