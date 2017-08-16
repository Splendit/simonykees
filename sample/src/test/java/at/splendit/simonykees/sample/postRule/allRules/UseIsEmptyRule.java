package at.splendit.simonykees.sample.postRule.allRules;

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
public class UseIsEmptyRule {

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
	}
}
