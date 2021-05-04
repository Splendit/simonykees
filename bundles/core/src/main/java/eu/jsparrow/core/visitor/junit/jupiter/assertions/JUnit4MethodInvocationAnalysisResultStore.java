package eu.jsparrow.core.visitor.junit.jupiter.assertions;

import java.util.List;

import org.eclipse.jdt.core.dom.MethodInvocation;

class JUnit4MethodInvocationAnalysisResultStore {
	private final List<JUnit4MethodInvocationAnalysisResult> methodInvocationAnalysisResults;
	private final List<JUnit4AssertThrowsInvocationAnalysisResult> assertThrowsInvocationAnalysisResults;
	private final List<JUnit4AssumeNotNullInvocationAnalysisResult> assumeNotNullInvocationAnalysisResults;
	private final List<MethodInvocation> notTransformedMethodInvocations;

	public JUnit4MethodInvocationAnalysisResultStore(
			List<JUnit4MethodInvocationAnalysisResult> methodInvocationAnalysisResults,
			List<JUnit4AssertThrowsInvocationAnalysisResult> assertThrowsInvocationAnalysisResults,
			List<JUnit4AssumeNotNullInvocationAnalysisResult> assumeNotNullInvocationAnalysisResults,
			List<MethodInvocation> notTransformedMethodInvocations) {

		this.methodInvocationAnalysisResults = methodInvocationAnalysisResults;
		this.assertThrowsInvocationAnalysisResults = assertThrowsInvocationAnalysisResults;
		this.assumeNotNullInvocationAnalysisResults = assumeNotNullInvocationAnalysisResults;
		this.notTransformedMethodInvocations = notTransformedMethodInvocations;
	}

	List<JUnit4AssertThrowsInvocationAnalysisResult> getAssertThrowsInvocationAnalysisResults() {
		return assertThrowsInvocationAnalysisResults;
	}

	List<JUnit4AssumeNotNullInvocationAnalysisResult> getAssumeNotNullInvocationAnalysisResults() {
		return assumeNotNullInvocationAnalysisResults;
	}

	List<JUnit4MethodInvocationAnalysisResult> getMethodInvocationAnalysisResults() {
		return methodInvocationAnalysisResults;
	}

	List<MethodInvocation> getNotTransformedJUnt4MethodInvocations() {
		return notTransformedMethodInvocations;
	}
}