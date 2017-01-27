package at.splendit.simonykees.sample.preRule;

public class TestPrimitiveBoxedForStringWithExpressionsRule {
	
	private int intSampleMethod() {
		return 1;
	}
	
	private int intSampleMethod(String intVal) {
		return Integer.valueOf(intVal);
	}
	
	public String testIntegerValueOfExpressionToString(int input) {
		return Integer.valueOf(input + 3).toString();
	}
	
	public String testIntegerValueOfExpressionWithMethodInvoke(int input) {
		return Integer.valueOf(intSampleMethod() + input).toString(); 
	}
	
	public String testIntegerValueOfMixedTypeExpression(int input) {
		return Integer.valueOf(input + new Integer(1) + intSampleMethod()).toString();
	}
	
	public String testIntegerValueOfCascadeExpressions(int input) {
		return Integer.valueOf(input + intSampleMethod()).toString() 
				+ Integer.valueOf(input + "abc".length()).toString();
	}
	
	public String testIntegerValueOfInIfCondition(int input) {
		String result = "";
		if(!Integer.valueOf(input).toString().isEmpty()
				|| Integer.valueOf(5).toString().length() == 1) {
			result = Integer.toString(input);
		}
		return result;
	}
	
	public String testIntegerBoxingOnExpression(int input) {
		String result =  new Integer(5 + input + intSampleMethod("2")).toString()
				+ new Integer(input + intSampleMethod()).toString();
		return result;
	}
	
	public String testNestedIntegerBoxing(int input) {
		String val = Integer.valueOf(Integer.valueOf(input) + 1).toString();
		return val;
	}
	
	
	public String testMethodInvocationIntegerBoxing(int input) {
		String val = Integer
				.valueOf(intSampleMethod(Integer.valueOf(3).toString()) + 4)
				.toString();
		return val;
	}
	
	public String testLiteralConcatWithExpression(int input) {
		return "" + (input + 1 + intSampleMethod("4"));
	}
}
