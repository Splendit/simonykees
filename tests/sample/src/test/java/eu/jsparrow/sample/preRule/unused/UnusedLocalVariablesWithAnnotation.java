package eu.jsparrow.sample.preRule.unused;

public class UnusedLocalVariablesWithAnnotation {

	void localVariableWithSuppressWarningsAnnotation() {
		@SuppressWarnings({ "unused" })
		int x;
	}

	void localVariableWithDeprecatedAnnotation() {
		@Deprecated
		int x;
	}

	void localVariableWithKeepMeAnnotation() {
		@KeepMe
		int x;
	}

	@interface KeepMe {

	}
}
