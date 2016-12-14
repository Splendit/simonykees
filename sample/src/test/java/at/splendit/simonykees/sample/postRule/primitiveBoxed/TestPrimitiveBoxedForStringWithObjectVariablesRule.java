package at.splendit.simonykees.sample.postRule.primitiveBoxed;

@SuppressWarnings("nls")
public class TestPrimitiveBoxedForStringWithObjectVariablesRule {

	public String testIntegeregerLiteralConcat(Integer input) {
		return Integer.toString(input);
	}

	public String testIntegeregerNewLiteralTostring(Integer input) {
		return Integer.toString(input);
	}

	public String testIntegeregerValueOfToString(Integer input) {
		return Integer.toString(input);
	}

	public String testLongLiteralConcat(Long input) {
		return Long.toString(input);
	}

	public String testLongLongNewLiteralTostring(Long input) {
		return Long.toString(input);
	}

	public String testLongIntegeregerNewLiteralTostring(Integer input) {
		return Long.toString(input);
	}

	public String testLongLongValueOfToString(Long input) {
		return Long.toString(input);
	}

	public String testLongIntegeregerValueOfToString(Integer input) {
		return Long.toString(input);
	}

	public String testFloatLiteralConcat(Float input) {
		return Float.toString(input);
	}

	public String testFloatNewLiteralTostring(Float input) {
		return Float.toString(input);
	}

	public String testFloatDoubleNewLiteralTostring(Double input) {
		return new Float(input).toString();
	}

	public String testFloatLongNewLiteralTostring(Long input) {
		return Float.toString(input);
	}

	public String testFloatIntegeregerNewLiteralTostring(Integer input) {
		return Float.toString(input);
	}

	public String testFloatValueOfToString(Float input) {
		return Float.toString(input);
	}

	public String testFloatLongValueOfToString(Long input) {
		return Float.toString(input);
	}

	public String testFloatIntegeregerValueOfToString(Integer input) {
		return Float.toString(input);
	}

	public String testDoubleLiteralConcat(Double input) {
		return Double.toString(input);
	}

	public String testDoubleNewLiteralTostring(Double input) {
		return Double.toString(input);
	}

	public String testDoubleFloatNewLiteralTostring(Float input) {
		return Double.toString(input);
	}

	public String testDoubleLongNewLiteralTostring(Long input) {
		return Double.toString(input);
	}

	public String testDoubleIntegeregerNewLiteralTostring(Integer input) {
		return Double.toString(input);
	}

	public String testDoubleValueOfToString(Double input) {
		return Double.toString(input);
	}

	public String testDoubleFloatValueOfToString(Float input) {
		return Double.toString(input);
	}

	public String testDoubleLongValueOfToString(Long input) {
		return Double.toString(input);
	}

	public String testDoubleIntegeregerValueOfToString(Integer input) {
		return Double.toString(input);
	}
}
