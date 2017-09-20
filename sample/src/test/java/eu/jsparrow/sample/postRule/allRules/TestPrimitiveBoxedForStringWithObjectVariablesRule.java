package eu.jsparrow.sample.postRule.allRules;

import java.util.Date;

@SuppressWarnings("nls")
public class TestPrimitiveBoxedForStringWithObjectVariablesRule {

	public String testIntegerLiteralConcat(Integer input) {
		return Integer.toString(input);
	}

	public String testIntegerNewLiteralTostring(Integer input) {
		return Integer.toString(input);
	}

	public String testIntegerValueOfToString(Integer input) {
		return Integer.toString(input);
	}

	public String testLongLiteralConcat(Long input) {
		return Long.toString(input);
	}

	public String testLongLongNewLiteralTostring(Long input) {
		return Long.toString(input);
	}

	public String testLongIntegerNewLiteralTostring(Integer input) {
		return Long.toString(input);
	}

	public String testLongLongValueOfToString(Long input) {
		return Long.toString(input);
	}

	public String testLongIntegerValueOfToString(Integer input) {
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

	public String testFloatIntegerNewLiteralTostring(Integer input) {
		return Float.toString(input);
	}

	public String testFloatValueOfToString(Float input) {
		return Float.toString(input);
	}

	public String testFloatLongValueOfToString(Long input) {
		return Float.toString(input);
	}

	public String testFloatIntegerValueOfToString(Integer input) {
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

	public String testDoubleIntegerNewLiteralTostring(Integer input) {
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

	public String testDoubleIntegerValueOfToString(Integer input) {
		return Double.toString(input);
	}

	// SIM-529
	public String testDateLongValueOfToString(Long input) {
		return new Date(input).toString();
	}
}
