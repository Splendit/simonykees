package eu.jsparrow.sample.preRule;

import java.util.Arrays;

@SuppressWarnings("nls")
public class TestBracketsToControlRule {
	public static void testforIf() {
		int i = 0;

		if (i == 0) {
			i = 0;
		} else {
			i = 0;
		}
	}
	
	public static void testforIfWithSystemOut() {
		int i = 0;

		if (i == 0) {
			System.out.println("0");
		} else {
			System.out.println("0");
		}
	}

	public static void testforLoop() {
		for (int i = 1; i < 2; i++)// 
			;
		for (Integer i : Arrays.asList(1, 2, 3))
			System.out.println(i);
	}

	public static void thestWhileLoop() {
		String st = null;
		while (st != null) //
			;
		do // 
			; while (st != null);
	}

	public static void testIf() {
		String s = "a";
		String t = "bb";
		if (s == "a") //
			if (t == "b") //
				do
					; while (t != null);
			else if (s == "aaa")
				System.out.print("aa");
			else
				System.out.print("bbb");
		else
			;
	}

	@SuppressWarnings("finally")
	public int testMultipleThings(int input) {
		if (input > 0) for (int i = 0; i < 10; i++) if (i > 2) input++; else input--; else if (input > -200) if (input != 0) do input++; while (input < 10); else input--; else try {input /= 0;} catch (ArithmeticException e) {input /= 2;} finally {return ++input;}
		return input;
	}
}
