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

		return input // don't break the semicolon
			.equals("input" // don't break the line
			);
	}

	public boolean swapExpressionWithStringLiteralIgnoreCase(String input) {
		StringUtils.equalsIgnoreCase(getClass().getName(), "StringLiteralEqualityCheckRule");

		return "input".equals(input);
	}

	public boolean swapMethodInvocationExpresion() {
		return "value".equals(getValue());
	}

	public boolean swapMethodInvocationExpresionIgnoreCase() {
		return StringUtils.equalsIgnoreCase(getValue(), "vAlue");
	}

	public boolean swapInReturnStatement() {
		return "value".equals(getValue());
	}

	public boolean literalAlreadyOnLHS() {
		final String value = "lhs-literal";
		return "lhs-literal".equals(value);
	}

	public boolean literalAlreadyOnLHSIgnoreCase() {
		final String value = "lhs-literal";
		return StringUtils.equalsIgnoreCase("lhs-literal", value);
	}

	public boolean nonStringEqualityCheck() {
		final Foo foo = new Foo("foo");
		return foo.equals("foo");
	}

	public boolean bothSidesAreStringLiterals() {
		return "left".equals("right");
	}

	public boolean someCommentsInBetween() {
		final String foo = "cornerCaseWithCommentsInBetween";
		return // please dont loose me
		foo // comparing equality with a copy of init value
			.equals("cornerCaseWithCommentsInBetween" // I may be useful
			);
	}

	public boolean someCommentsInBetween2() {
		final String foo = "cornerCaseWithCommentsInBetween";
		return // comparing equality with a copy of init value
		foo // please dont loose me
			.equals("cornerCaseWithCommentsInBetween");
	}

	public boolean compareStringExpression() {
		final String fooConcat = "fooconcatexpression";
		return fooConcat.equals(new StringBuilder().append("foo")
			.append("concat")
			.append("expression")
			.toString());
	}

	public boolean compareStringExpression2() {
		final String fooConcat = "fooconcatexpression";
		return "fooconcatexpression".equals((new StringBuilder().append("foo")
			.append("concat")
			.append("expression")
			.toString()));
	}

	public boolean checkingCustomComparable() {
		final ComparableString foo = new ComparableString("customComparable");

		return foo.equals("customComparable");
	}

	private String getValue() {
		return "value";
	}

	class Foo {
		private final String foo;

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

		private final String foo;

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
			final String result = "malicous-prefix" + foo;
			return result.compareTo(o);
		}

	}
}
