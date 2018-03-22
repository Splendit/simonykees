package eu.jsparrow.sample.preRule;

@SuppressWarnings("nls")
public class TestPrimitiveBoxedForStringWithConstantsRule {

	public String testIntegerLiteralConcat() {
		return "" /* save me */ +  4;
	}

	public String testIntegerNewLiteralTostring() {
		return new Integer(4).toString();
	}

	public String testIntegerValueOfToString() {
		return Integer.valueOf(4).toString();
	}

	public String testLongLiteralConcat() {
		return "" + 4L;
	}

	public String testLongLongNewLiteralTostring() {
		return new Long(4L)/* save me */.toString();
	}

	public String testLongIntegerNewLiteralTostring() {
		return new Long(4).toString();
	}

	public String testLongLongValueOfToString() {
		return Long.valueOf(4L).toString();
	}

	public String testLongIntegerValueOfToString() {
		return Long.valueOf(4).toString();
	}

	public String testFloatLiteralConcat() {
		return "" + 4F;
	}

	public String testFloatNewLiteralTostring() {
		return new Float(4F).toString();
	}

	public String testFloatDoubleNewLiteralTostring() {
		return new Float(4D).toString();
	}

	public String testFloatLongNewLiteralTostring() {
		return new Float(4L).toString();
	}

	public String testFloatIntegerNewLiteralTostring() {
		return new Float(4).toString();
	}

	public String testFloatValueOfToString() {
		return Float.valueOf(4F).toString();
	}

	public String testFloatLongValueOfToString() {
		return Float.valueOf(4L).toString();
	}

	public String testFloatIntegerValueOfToString() {
		return Float.valueOf(4).toString();
	}

	public String testDoubleLiteralConcat() {
		return "" + 4D;
	}

	public String testDoubleNewLiteralTostring() {
		return new Double(4D).toString();
	}

	public String testDoubleFloatNewLiteralTostring() {
		return new Double(4F).toString();
	}

	public String testDoubleLongNewLiteralTostring() {
		return new Double(4L).toString();
	}

	public String testDoubleIntegerNewLiteralTostring() {
		return new Double(4).toString();
	}

	public String testDoubleValueOfToString() {
		return Double.valueOf(4D).toString();
	}

	public String testDoubleFloatValueOfToString() {
		return Double.valueOf(4F).toString();
	}

	public String testDoubleLongValueOfToString() {
		return Double.valueOf(4L).toString();
	}

	public String testDoubleIntegerValueOfToString() {
		return Double.valueOf(4).toString();
	}
}
