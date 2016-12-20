package at.splendit.simonykees.sample.postRule.inefficientConstructor;

@SuppressWarnings("nls")
public class TestInefficientConstructorBooleanRule {

	public Boolean booleanTrueTest() {
		return true;
	}

	public Boolean booleanTrueLiteralTest() {
		return true;
	}
	
	public Boolean booleanTrueValueOfTest() {
		return true;
	}

	public Boolean booleanTrueLiteralValueOfTest() {
		return true;
	}

	public Boolean booleanFalseTest() {
		return false;
	}

	public Boolean booleanFalseLiteralTest() {
		return false;
	}

	public Boolean booleanFalseAnyLiteralTest() {
		return Boolean.valueOf("anyOtherStringThanTrue");
	}

	public Boolean booleanFalseValueOfTest() {
		return false;
	}
	
	public Boolean booleanFalseLiteralValueOfTest() {
		return false;
	}

	public Boolean booleanAnyValueOfTest() {
		return Boolean.valueOf("anyOtherStringThanTrue");
	}

	public Boolean booleanVariableBooleanTest(Boolean input) {
		return input;
	}

	public Boolean booleanVariableBooleanTest(boolean input) {
		return input;
	}

	public Boolean booleanVariableStringTest(String input) {
		return Boolean.valueOf(input);
	}
	
	public Boolean booleanVariableStringValueOfTest(String input) {
		return Boolean.valueOf(input);
	}
}
