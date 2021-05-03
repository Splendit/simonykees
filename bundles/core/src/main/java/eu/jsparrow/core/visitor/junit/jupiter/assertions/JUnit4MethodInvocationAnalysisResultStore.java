package eu.jsparrow.core.visitor.junit.jupiter.assertions;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;

import eu.jsparrow.core.visitor.junit.jupiter.common.MethodInvocationsCollectorVisitor;

class JUnit4MethodInvocationAnalysisResultStore {
	private final List<JUnit4MethodInvocationAnalysisResult> methodInvocationAnalysisResults = new ArrayList<>();
	private final List<JUnit4AssertThrowsInvocationAnalysisResult> assertThrowsInvocationAnalysisResults = new ArrayList<>();

	JUnit4MethodInvocationAnalysisResultStore(CompilationUnit compilationUnit,
			Predicate<IMethodBinding> supportedJUnit4MethodPredicate) {

		MethodInvocationsCollectorVisitor invocationCollectorVisitor = new MethodInvocationsCollectorVisitor();
		compilationUnit.accept(invocationCollectorVisitor);

		JUnit4MethodInvocationAnalyzer analyzer = new JUnit4MethodInvocationAnalyzer(compilationUnit,
				supportedJUnit4MethodPredicate);

		for (MethodInvocation methodInvocation : invocationCollectorVisitor.getMethodInvocations()) {

			JUnit4AssertThrowsInvocationAnalysisResult assertThrowsAnalysisResult = analyzer
				.findAssertThrowsAnalysisResult(methodInvocation)
				.orElse(null);
			if (assertThrowsAnalysisResult != null) {
				assertThrowsInvocationAnalysisResults.add(assertThrowsAnalysisResult);
			} else {
				analyzer.findAnalysisResult(methodInvocation)
					.ifPresent(methodInvocationAnalysisResults::add);
			}
		}
	}

	public List<JUnit4AssertThrowsInvocationAnalysisResult> getAssertThrowsInvocationAnalysisResults() {
		return assertThrowsInvocationAnalysisResults;
	}

	List<JUnit4MethodInvocationAnalysisResult> getMethodInvocationAnalysisResults() {
		return methodInvocationAnalysisResults;
	}

}
