package eu.jsparrow.sample.preRule;

@SuppressWarnings("nls")
public class TestInefficientConstructorBooleanRule {

	public Boolean booleanTrueTest() {
		return /* save me */ new Boolean(true);
	}

	public Boolean booleanTrueLiteralTest() {
		return /* leading comment */ new /* internal comment */ Boolean( /* argument comment */"true") /* trailing comment */;
	}

	public Boolean booleanFalseTest() {
		return /* leading comment */ new /* internal comment */ Boolean( /* argument comment */ false) /* trailing comment */ ;
	}

	public Boolean booleanFalseLiteralTest() {
		return new Boolean("false");
	}

	public Boolean booleanFalseAnyLiteralTest() {
		return new Boolean("anyOtherStringThanTrue");
	}

	public Boolean booleanVariableBooleanTest(boolean input) {
		return new Boolean(/* save me */ input);
	}

	public Boolean booleanVariableStringTest(String input) {
		return new Boolean(/* save me */ input);
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

	public Boolean booleanWithMethodInvocationTest() {
		return new Boolean(true).booleanValue();
	}

	public Boolean booleanTrueValueOfMethodInvocationTest() {
		return Boolean.valueOf(true).booleanValue();
	}
}
