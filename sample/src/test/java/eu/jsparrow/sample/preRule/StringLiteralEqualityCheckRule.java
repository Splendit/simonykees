package eu.jsparrow.sample.preRule;

/**
 * Corner cases for StringLiteralEqualityCheckRule.
 * 
 * @author Ardit Ymeri
 * @since 1.2
 */
@SuppressWarnings({"nls", "unused"}) 
public class StringLiteralEqualityCheckRule {
	
	public boolean swapExpressionWithStringLiteral(String input) {
		getClass().getName().equals("StringLiteralEqualityCheckRule");
		
		boolean swap = input.equals("input");
		return swap;
	}
	
	public boolean swapExpressionWithStringLiteralIgnoreCase(String input) {
		getClass().getName().equalsIgnoreCase("StringLiteralEqualityCheckRule");
		
		boolean swap = input.equals("input");
		return swap;
	}
	
	public boolean swapMethodInvocationExpresion() {
		boolean swap = getValue().equals("value");
		return swap;
	}
	
	public boolean swapMethodInvocationExpresionIgnoreCase() {
		boolean swap = getValue().equalsIgnoreCase("vAlue");
		return swap;
	}
	
	public boolean swapInReturnStatement() {
		return getValue().equals("value");
	}
	
	public boolean literalAlreadyOnLHS() {
		String value = "lhs-literal";
		return "lhs-literal".equals(value);
	}
	
	public boolean literalAlreadyOnLHSIgnoreCase() {
		String value = "lhs-literal";
		return "lhs-literal".equalsIgnoreCase(value);
	}
	
	public boolean nonStringEqualityCheck(){
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
		return ("foo" + "concat" + "expression").equals("fooconcatexpression");
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
		
		public String toString() {
			return foo;
		}
		
		public boolean equals(String foo) {
			return this.foo.equals(foo);
		}
		
		public boolean equals(Foo foo) {
			return foo.toString().equals(this.foo);
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
		
		public String toString() {
			return foo;
		}
		
		@Override
		public int compareTo(String o) {
			String result = "malicous-prefix" + foo.toString();
			return result.compareTo(o);
		}
		
	}
}
