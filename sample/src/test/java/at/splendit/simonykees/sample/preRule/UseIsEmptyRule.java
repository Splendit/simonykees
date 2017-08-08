package at.splendit.simonykees.sample.preRule;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Corner cases for StringLiteralEqualityCheckRule.
 * 
 * @author Martin Huter
 * @since 2.0.3
 */
@SuppressWarnings({ "nls", "unused" })
public class UseIsEmptyRule {

	public void test1() {
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

	public void test2() {

	}

	public void test3() {

	}
}
