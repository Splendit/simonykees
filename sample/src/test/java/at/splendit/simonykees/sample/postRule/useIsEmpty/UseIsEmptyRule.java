package at.splendit.simonykees.sample.postRule.useIsEmpty;

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
		if (m.isEmpty()) {
		}

		Collection<String> l = new ArrayList<>();
		if (l.isEmpty()) {
		}

		String s = "";
		if (s.isEmpty()) {
		}

	}

	public void test2() {

	}

	public void test3() {

	}
}
