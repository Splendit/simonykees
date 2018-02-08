package eu.jsparrow.sample.postRule.allRules;

import org.apache.commons.lang3.StringUtils;

/**
 * Corner cases for StringLiteralEqualityCheckRule.
 * 
 * @author Ardit Ymeri
 * @since 1.2
 */
@SuppressWarnings({ "nls", "unused" })
public class StringLiteralEqualityCheckRule {

	public boolean swapExpressionWithStringLiteral(String input) {
		"StringLiteralEqualityCheckRule".equals(getClass().getName());

		boolean swap = input // don't break the semicolon
			.equals("input" // don't break the line
		);
		return swap;
	}

	public boolean swapExpressionWithStringLiteralIgnoreCase(String input) {
		StringUtils.equalsIgnoreCase(getClass().getName(), "StringLiteralEqualityCheckRule");

		boolean swap = "input".equals(input);
		return swap;
	}

	public boolean swapMethodInvocationExpresion() {
		boolean swap = "value".equals(getValue());
		return swap;
	}

	public boolean swapMethodInvocationExpresionIgnoreCase() {
		boolean swap = StringUtils.equalsIgnoreCase(getValue(), "vAlue");
		return swap;
	}

	public boolean swapInReturnStatement() {
		return "value".equals(getValue());
	}

	public boolean literalAlreadyOnLHS() {
		String value = "lhs-literal";
		return "lhs-literal".equals(value);
	}

	public boolean literalAlreadyOnLHSIgnoreCase() {
		String value = "lhs-literal";
		return StringUtils.equalsIgnoreCase("lhs-literal", value);
	}

	public boolean nonStringEqualityCheck() {
		Foo foo = new Foo("foo");
		return foo.equals("foo");
	}

	public boolean bothSidesAreStringLiterals() {
		return "left".equals("right");
	}

	public boolean someCommentsInBetween() {
		String foo = "cornerCaseWithCommentsInBetween";
		boolean swap =
				// please dont loose me
				foo // comparing equality with a copy of init value
					.equals("cornerCaseWithCommentsInBetween" // I may be useful
				);

		return swap;
	}

	public boolean someCommentsInBetween2() {
		String foo = "cornerCaseWithCommentsInBetween";
		boolean swap =
				// comparing equality with a copy of init value
				foo // please dont loose me
					.equals("cornerCaseWithCommentsInBetween");

		return swap;
	}

	public boolean compareStringExpression() {
		String fooConcat = "fooconcatexpression";
		return fooConcat.equals("foo" + "concat" + "expression");
	}

	public boolean compareStringExpression2() {
		String fooConcat = "fooconcatexpression";
		return "fooconcatexpression".equals(("foo" + "concat" + "expression"));
	}

	public boolean checkingCustomComparable() {
		String val = "customComparable";
		ComparableString foo = new ComparableString(val);

		return foo.equals("customComparable");
	}

	private String getValue() {
		return "value";
	}

	class Foo {
		private String foo;

		public Foo(String foo) {
			this.foo = foo;
		}

		@Override
		public String toString() {
			return foo;
		}

		public boolean equals(String foo) {
			return this.foo.equals(foo);
		}

		public boolean equals(Foo foo) {
			return foo.toString()
				.equals(this.foo);
		}

		public boolean equals(Foo foo, String otherFoo) {
			return toString().equals(foo);
		}
	}

	class ComparableString implements Comparable<String> {

		private String foo;

		public ComparableString(String foo) {
			this.foo = foo;
		}

		public boolean equals(String val) {
			return foo.equals(val);
		}

		@Override
		public String toString() {
			return foo;
		}

		@Override
		public int compareTo(String o) {
			String result = "malicous-prefix" + foo;
			return result.compareTo(o);
		}

	}
}
