package at.splendit.simonykees.sample.postRule;

import java.util.Arrays;

public class TestBracketsToControlRule {
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
			;
		}
		for (Integer i : Arrays.asList(1, 2, 3)) {
			System.out.println(i);
		}
	}

	public static void thestWhileLoop() {
		String st = null;
		while (st != null) {
			;
		}
		do {
			;
		} while (st != null);
	}

	public static void testIf() {
		String s = "a";
		String t = "bb";
		if (s == "a") {
			if (t == "b") {
				do {
					;
				} while (t != null);
			} else if (s == "aaa") {
				System.out.print("aa");
			} else {
				System.out.print("bbb");
			}
		} else {
			;
		}
	}
}
