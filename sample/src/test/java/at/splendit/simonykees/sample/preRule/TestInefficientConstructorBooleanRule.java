package at.splendit.simonykees.sample.preRule;

@SuppressWarnings("nls")
public class TestInefficientConstructorBooleanRule {

	public Boolean booleanTrueTest() {
		return new Boolean(true);
	}

	public Boolean booleanTrueLiteralTest() {
		return new Boolean("true");
	}
	
	public Boolean booleanFalseTest() {
		return new Boolean(false);
	}

	public Boolean booleanFalseLiteralTest() {
		return new Boolean("false");
	}

	public Boolean booleanFalseAnyLiteralTest() {
		return new Boolean("anyOtherStringThanTrue");
	}

	public Boolean booleanVariableBooleanTest(boolean input) {
		return new Boolean(input);
	}

	public Boolean booleanVariableStringTest(String input) {
		return new Boolean(input);
	}
	
	public Boolean booleanTrueValueOfTest() {
		return Boolean.valueOf(true);
	}

	public Boolean booleanTrueLiteralValueOfTest() {
		return Boolean.valueOf("true");
	}
	
	public Boolean booleanFalseValueOfTest() {
		return Boolean.valueOf(false);
	}

	public Boolean booleanFalseLiteralValueOfTest() {
		return Boolean.valueOf("false");
	}

	public Boolean booleanAnyValueOfTest() {
		return Boolean.valueOf("anyOtherStringThanTrue");
	}

	public Boolean booleanVariableBooleanTest(Boolean input) {
		return Boolean.valueOf(input);
	}
	
	public Boolean booleanVariableBooleanPrimTest(boolean input) {
		return Boolean.valueOf(input);
	}
	
	public Boolean booleanVariableStringValueOfTest(String input) {
		return Boolean.valueOf(input);
	}
}
