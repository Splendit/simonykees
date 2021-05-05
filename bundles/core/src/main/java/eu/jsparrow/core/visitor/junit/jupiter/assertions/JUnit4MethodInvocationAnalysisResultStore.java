package eu.jsparrow.core.visitor.junit.jupiter.assertions;

import java.util.List;

import org.eclipse.jdt.core.dom.MethodInvocation;

class JUnit4MethodInvocationAnalysisResultStore {
	private final List<JUnit4MethodInvocationAnalysisResult> methodInvocationAnalysisResults;
	private final List<MethodInvocation> notTransformedMethodInvocations;

	public JUnit4MethodInvocationAnalysisResultStore(
			List<JUnit4MethodInvocationAnalysisResult> methodInvocationAnalysisResults,
			List<MethodInvocation> notTransformedMethodInvocations) {

		this.methodInvocationAnalysisResults = methodInvocationAnalysisResults;
		this.notTransformedMethodInvocations = notTransformedMethodInvocations;
	}

	List<JUnit4MethodInvocationAnalysisResult> getMethodInvocationAnalysisResults() {
		return methodInvocationAnalysisResults;
	}

	List<MethodInvocation> getNotTransformedJUnt4MethodInvocations() {
		return notTransformedMethodInvocations;
	}
}