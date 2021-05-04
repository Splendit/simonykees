package eu.jsparrow.core.visitor.junit.jupiter.assertions;

import java.util.List;

class JUnit4MethodInvocationAnalysisResultStore {
	private final List<JUnit4MethodInvocationAnalysisResult> methodInvocationAnalysisResults;
	private final List<JUnit4AssertThrowsInvocationAnalysisResult> assertThrowsInvocationAnalysisResults;
	private final List<JUnit4AssumeNotNullInvocationAnalysisResult> assumeNotNullInvocationAnalysisResults;


	public JUnit4MethodInvocationAnalysisResultStore(
			List<JUnit4MethodInvocationAnalysisResult> methodInvocationAnalysisResults,
			List<JUnit4AssertThrowsInvocationAnalysisResult> assertThrowsInvocationAnalysisResults,
			List<JUnit4AssumeNotNullInvocationAnalysisResult> assumeNotNullInvocationAnalysisResults) {

		this.methodInvocationAnalysisResults = methodInvocationAnalysisResults;
		this.assertThrowsInvocationAnalysisResults = assertThrowsInvocationAnalysisResults;
		this.assumeNotNullInvocationAnalysisResults = assumeNotNullInvocationAnalysisResults;
	}

	List<JUnit4AssertThrowsInvocationAnalysisResult> getAssertThrowsInvocationAnalysisResults() {
		return assertThrowsInvocationAnalysisResults;
	}

	public List<JUnit4AssumeNotNullInvocationAnalysisResult> getAssumeNotNullInvocationAnalysisResults() {
		return assumeNotNullInvocationAnalysisResults;
	}

	List<JUnit4MethodInvocationAnalysisResult> getMethodInvocationAnalysisResults() {
		return methodInvocationAnalysisResults;
	}

}
