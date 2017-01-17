package at.splendit.simonykees.sample.postRule.primitiveBoxed;

@SuppressWarnings("nls")
public class TestPrimitiveBoxedForStringWithConstantsRule {

	public String testIntegerLiteralConcat() {
		return Integer.toString(4);
	}

	public String testIntegerNewLiteralTostring() {
		return Integer.toString(4);
	}

	public String testIntegerValueOfToString() {
		return Integer.toString(4);
	}

	public String testLongLiteralConcat() {
		return Long.toString(4L);
	}

	public String testLongLongNewLiteralTostring() {
		return Long.toString(4L);
	}

	public String testLongIntegerNewLiteralTostring() {
		return Long.toString(4);
	}

	public String testLongLongValueOfToString() {
		return Long.toString(4L);
	}

	public String testLongIntegerValueOfToString() {
		return Long.toString(4);
	}

	public String testFloatLiteralConcat() {
		return Float.toString(4F);
	}

	public String testFloatNewLiteralTostring() {
		return Float.toString(4F);
	}

	public String testFloatDoubleNewLiteralTostring() {
		return new Float(4D).toString();
	}

	public String testFloatLongNewLiteralTostring() {
		return Float.toString(4L);
	}

	public String testFloatIntegerNewLiteralTostring() {
		return Float.toString(4);
	}

	public String testFloatValueOfToString() {
		return Float.toString(4F);
	}

	public String testFloatLongValueOfToString() {
		return Float.toString(4L);
	}

	public String testFloatIntegerValueOfToString() {
		return Float.toString(4);
	}

	public String testDoubleLiteralConcat() {
		return Double.toString(4D);
	}

	public String testDoubleNewLiteralTostring() {
		return Double.toString(4D);
	}

	public String testDoubleFloatNewLiteralTostring() {
		return Double.toString(4F);
	}

	public String testDoubleLongNewLiteralTostring() {
		return Double.toString(4L);
	}

	public String testDoubleIntegerNewLiteralTostring() {
		return Double.toString(4);
	}

	public String testDoubleValueOfToString() {
		return Double.toString(4D);
	}

	public String testDoubleFloatValueOfToString() {
		return Double.toString(4F);
	}

	public String testDoubleLongValueOfToString() {
		return Double.toString(4L);
	}

	public String testDoubleIntegerValueOfToString() {
		return Double.toString(4);
	}
}
