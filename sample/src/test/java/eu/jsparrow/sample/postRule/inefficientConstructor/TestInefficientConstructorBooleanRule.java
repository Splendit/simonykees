package eu.jsparrow.sample.postRule.inefficientConstructor;

@SuppressWarnings("nls")
public class TestInefficientConstructorBooleanRule {

	public Boolean booleanTrueTest() {
		/* save me */
		return Boolean.valueOf(true);
	}

	public Boolean booleanTrueLiteralTest() {
		/* save me */
		return Boolean.valueOf(true);
	}

	public Boolean booleanFalseTest() {
		return Boolean.valueOf(false);
	}

	public Boolean booleanFalseLiteralTest() {
		return Boolean.valueOf(false);
	}

	public Boolean booleanFalseAnyLiteralTest() {
		return Boolean.valueOf(false);
	}

	public Boolean booleanVariableBooleanTest(boolean input) {
		return Boolean.valueOf(input);
	}

	public Boolean booleanVariableStringTest(String input) {
		return Boolean.valueOf(input);
	}

	public Boolean booleanTrueValueOfTest() {
		return Boolean.valueOf(true);
	}

	public Boolean booleanTrueLiteralValueOfTest() {
		return Boolean.valueOf(true);
	}

	public Boolean booleanFalseValueOfTest() {
		return Boolean.valueOf(false);
	}

	public Boolean booleanFalseLiteralValueOfTest() {
		return Boolean.valueOf(false);
	}

	public Boolean booleanAnyValueOfTest() {
		return Boolean.valueOf(false);
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

	public Boolean booleanWithMethodInvocationTest() {
		return Boolean.valueOf(true).booleanValue();
	}

	public Boolean booleanTrueValueOfMethodInvocationTest() {
		return Boolean.valueOf(true).booleanValue();
	}
}
