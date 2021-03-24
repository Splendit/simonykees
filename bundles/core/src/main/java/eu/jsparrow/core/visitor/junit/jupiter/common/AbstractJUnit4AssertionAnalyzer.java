package eu.jsparrow.core.visitor.junit.jupiter.common;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodInvocation;

public abstract class AbstractJUnit4AssertionAnalyzer<T> {

	public List<T> collectJUnit4AssertionAnalysisResults(
			CompilationUnit compilationUnit) {

		MethodInvocationsCollectorVisitor invocationCollectorVisitor = new MethodInvocationsCollectorVisitor();
		compilationUnit.accept(invocationCollectorVisitor);
		List<MethodInvocation> allMethodInvocations = invocationCollectorVisitor.getMethodInvocations();

		return allMethodInvocations
			.stream()
			.map(this::findAnalysisResult)
			.filter(Optional::isPresent)
			.map(Optional::get)
			.collect(Collectors.toList());
	}

	protected abstract Optional<T> findAnalysisResult(
			MethodInvocation methodInvocation);

}