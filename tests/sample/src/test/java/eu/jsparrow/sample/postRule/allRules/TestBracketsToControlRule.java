package eu.jsparrow.sample.postRule.allRules;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("nls")
public class TestBracketsToControlRule {
	private static final Logger logger = LoggerFactory.getLogger(TestBracketsToControlRule.class);

	public static void testforIf() {
		int i = 0;

		if (i == 0) {
			i = 0;
		} else {
			i = 0;
		}
	}

	public static void testforLoop() {
		for (int i = 1; i < 2; i++) {
		}
		Arrays.asList(1, 2, 3)
			.forEach(i -> logger.info(String.valueOf(i)));
	}

	public static void thestWhileLoop() {
		final String st = null;
		while (st != null) {
		}
		do {
		} while (st != null);
	}

	public static void testIf() {
		final String s = "a";
		final String t = "bb";
		if (s.equals("a")) {
			if (t.equals("b")) {
				do {
				} while (t != null);
			} else if (s.equals("aaa")) {
				logger.info("aa");
			} else {
				logger.info("bbb");
			}
		} else {
		}
	}

	@SuppressWarnings("finally")
	public int testMultipleThings(int input) {
		if (input > 0) {
			for (int i = 0; i < 10; i++) {
				if (i > 2) {
					input++;
				} else {
					input--;
				}
			}
		} else if (input > -200) {
			if (input != 0) {
				do {
					input++;
				} while (input < 10);
			} else {
				input--;
			}
		} else {
			try {
				input /= 0;
			} catch (ArithmeticException e) {
				logger.error(e.getMessage(), e);
				input /= 2;
			} finally {
				return ++input;
			}
		}
		return input;
	}
}
