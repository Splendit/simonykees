package at.splendit.simonykees.sample.postRule.useIsEmpty;

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
