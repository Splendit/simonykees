package eu.jsparrow.sample.preRule;

@SuppressWarnings("nls")
public class TestPrimitiveBoxedForStringWithVariablesRule {

	public String testIntegerLiteralConcat(int input) {
		return "" + input;
	}

	public String testIntegerNewLiteralTostring(int input) {
		return new Integer(input).toString();
	}

	public String testIntegerValueOfToString(int input) {
		return Integer.valueOf(input).toString();
	}

	public String testLongLiteralConcat(long input) {
		return "" + input;
	}

	public String testLongLongNewLiteralTostring(long input) {
		return new Long(input).toString();
	}

	public String testLongIntegerNewLiteralTostring(int input) {
		return new Long(input).toString();
	}

	public String testLongLongValueOfToString(long input) {
		return Long.valueOf(input).toString();
	}

	public String testLongIntegerValueOfToString(int input) {
		return Long.valueOf(input).toString();
	}

	public String testFloatLiteralConcat(float input) {
		return "" + input;
	}

	public String testFloatNewLiteralTostring(float input) {
		return new Float(input).toString();
	}

	public String testFloatDoubleNewLiteralTostring(double input) {
		return new Float(input).toString();
	}

	public String testFloatLongNewLiteralTostring(long input) {
		return new Float(input).toString();
	}

	public String testFloatIntegerNewLiteralTostring(int input) {
		return new Float(input).toString();
	}

	public String testFloatValueOfToString(float input) {
		return Float.valueOf(input).toString();
	}

	public String testFloatLongValueOfToString(long input) {
		return Float.valueOf(input).toString();
	}

	public String testFloatIntegerValueOfToString(int input) {
		return Float.valueOf(input).toString();
	}

	public String testDoubleLiteralConcat(double input) {
		return "" + input;
	}

	public String testDoubleNewLiteralTostring(double input) {
		return new Double(input).toString();
	}

	public String testDoubleFloatNewLiteralTostring(float input) {
		return new Double(input).toString();
	}

	public String testDoubleLongNewLiteralTostring(long input) {
		return new Double(input).toString();
	}

	public String testDoubleIntegerNewLiteralTostring(int input) {
		return new Double(input).toString();
	}

	public String testDoubleValueOfToString(double input) {
		return Double.valueOf(input).toString();
	}

	public String testDoubleFloatValueOfToString(float input) {
		return Double.valueOf(input).toString();
	}

	public String testDoubleLongValueOfToString(long input) {
		return Double.valueOf(input).toString();
	}

	public String testDoubleIntegerValueOfToString(int input) {
		return Double.valueOf(input).toString();
	}
	
	public String testIntegertoStringWithParameters(int input) {
		return Integer.valueOf(input).toString(4);
	}

	public String test(Integer input) {
		String result = "";

		if(!("" + input).isEmpty()) {
			result = new Integer(intSampleMethod("5") + Integer.valueOf(3) + input).toString();
			Integer res = input + new Integer(1);
			result = result + res.toString();
		}

		return result;
	}
	
	private Integer intSampleMethod(String returnME){
		return Integer.valueOf(returnME);
	}
}
