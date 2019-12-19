package eu.jsparrow.sample.postRule.allRules;

public class LocalVariableTypeInferenceRule {

	public void visit_initializedWithSubtypeAndUsedInOverloadedMethod_shouldNotTransform() {
		final Number number = Integer.valueOf(4);
		overloadedMethod(number);
	}

	public void overloadedMethod() {

	}

	public void overloadedMethod(String value) {

	}

	public void overloadedMethod(Integer integer) {

	}

	public void overloadedMethod(Number number) {

	}

}
