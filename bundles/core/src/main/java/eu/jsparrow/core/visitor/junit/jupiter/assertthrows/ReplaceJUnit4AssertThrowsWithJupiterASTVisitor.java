package eu.jsparrow.core.visitor.junit.jupiter.assertthrows;

import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jdt.core.dom.CompilationUnit;

import eu.jsparrow.core.visitor.junit.jupiter.common.JUnit4AssertMethodInvocationAnalysisResult;

import eu.jsparrow.core.visitor.junit.jupiter.common.AbstractReplaceJUnit4AssertionsWithJupiterASTVisitor;
/**
 * Replaces invocations of methods of the JUnit-4-class
 * {@code org.junit.Assert#assertThrows} by invocations of the corresponding
 * methods of the JUnit-Jupiter-class {@code org.junit.jupiter.api.Assertions}.
 * 
 * @since 3.29.0
 *
 */
public class ReplaceJUnit4AssertThrowsWithJupiterASTVisitor extends AbstractReplaceJUnit4AssertionsWithJupiterASTVisitor {

	@Override
	public boolean visit(CompilationUnit compilationUnit) {

		super.visit(compilationUnit);
		verifyImport(compilationUnit, ORG_JUNIT_JUPITER_API_ASSERTIONS);

		JUnit4AssertThrowsInvocationAnalyzer assertionAnalyzer = new JUnit4AssertThrowsInvocationAnalyzer(
				compilationUnit);
		List<JUnit4AssertMethodInvocationAnalysisResult> allJUnit4AssertThrowsAnalysisResults = assertionAnalyzer
			.collectJUnit4AssertionAnalysisResults(compilationUnit);

		List<JUnit4AssertMethodInvocationAnalysisResult> transformableJUnit4AssertThrowsAnalysisResults = allJUnit4AssertThrowsAnalysisResults
			.stream()
			.filter(JUnit4AssertMethodInvocationAnalysisResult::isTransformableInvocation)
			.collect(Collectors.toList());

		return false;
	}
}
