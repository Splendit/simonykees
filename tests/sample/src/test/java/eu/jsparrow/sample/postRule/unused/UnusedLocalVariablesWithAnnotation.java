package eu.jsparrow.sample.postRule.unused;

public class UnusedLocalVariablesWithAnnotation {

	void localVariableWithSuppressWarningsAnnotation() {
	}

	void localVariableWithDeprecatedAnnotation() {
	}

	void localVariableWithKeepMeAnnotation() {
		@KeepMe
		int x;
	}

	@interface KeepMe {

	}
}
