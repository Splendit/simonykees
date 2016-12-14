package at.splendit.simonykees.sample.preRule;

@SuppressWarnings("nls")
public class TestPrimitiveBoxedForStringWithObjectVariablesRule {

	public String testIntegerLiteralConcat(Integer input) {
		return "" + input;
	}

	public String testIntegerNewLiteralTostring(Integer input) {
		return new Integer(input).toString();
	}

	public String testIntegerValueOfToString(Integer input) {
		return Integer.valueOf(input).toString();
	}

	public String testLongLiteralConcat(Long input) {
		return "" + input;
	}

	public String testLongLongNewLiteralTostring(Long input) {
		return new Long(input).toString();
	}

	public String testLongIntegerNewLiteralTostring(Integer input) {
		return new Long(input).toString();
	}

	public String testLongLongValueOfToString(Long input) {
		return Long.valueOf(input).toString();
	}

	public String testLongIntegerValueOfToString(Integer input) {
		return Long.valueOf(input).toString();
	}

	public String testFloatLiteralConcat(Float input) {
		return "" + input;
	}

	public String testFloatNewLiteralTostring(Float input) {
		return new Float(input).toString();
	}

	public String testFloatDoubleNewLiteralTostring(Double input) {
		return new Float(input).toString();
	}

	public String testFloatLongNewLiteralTostring(Long input) {
		return new Float(input).toString();
	}

	public String testFloatIntegerNewLiteralTostring(Integer input) {
		return new Float(input).toString();
	}

	public String testFloatValueOfToString(Float input) {
		return Float.valueOf(input).toString();
	}

	public String testFloatLongValueOfToString(Long input) {
		return Float.valueOf(input).toString();
	}

	public String testFloatIntegerValueOfToString(Integer input) {
		return Float.valueOf(input).toString();
	}

	public String testDoubleLiteralConcat(Double input) {
		return "" + input;
	}

	public String testDoubleNewLiteralTostring(Double input) {
		return new Double(input).toString();
	}

	public String testDoubleFloatNewLiteralTostring(Float input) {
		return new Double(input).toString();
	}

	public String testDoubleLongNewLiteralTostring(Long input) {
		return new Double(input).toString();
	}

	public String testDoubleIntegerNewLiteralTostring(Integer input) {
		return new Double(input).toString();
	}

	public String testDoubleValueOfToString(Double input) {
		return Double.valueOf(input).toString();
	}

	public String testDoubleFloatValueOfToString(Float input) {
		return Double.valueOf(input).toString();
	}

	public String testDoubleLongValueOfToString(Long input) {
		return Double.valueOf(input).toString();
	}

	public String testDoubleIntegerValueOfToString(Integer input) {
		return Double.valueOf(input).toString();
	}
}
