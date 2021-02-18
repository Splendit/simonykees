package eu.jsparrow.core.visitor.junit.jupiter.assertions;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;

import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.visitor.AbstractAddImportASTVisitor;

/**
 * Replaces invocations of methods of the JUnit-4-class {@code org.junit.Assert}
 * by invocations of the corresponding methods of the JUnit-Jupiter-class
 * {@code org.junit.jupiter.api.Assertions}.
 * 
 * @since 3.28.0
 * 
 */
public class ReplaceJUnit4AssertWithJupiterASTVisitor extends AbstractAddImportASTVisitor {

	@Override
	public boolean visit(CompilationUnit compilationUnit) {

		boolean continueVisiting = super.visit(compilationUnit);
		if (!continueVisiting) {
			return false;
		}
		MethodInvocationsCollectorVisitor invocationCollectorVisitor = new MethodInvocationsCollectorVisitor();
		compilationUnit.accept(invocationCollectorVisitor);
		JUnit4AssertMethodAnalyzer methodAnalyzer = new JUnit4AssertMethodAnalyzer();
		List<AssertTransformationData> assertTransformationDataList = invocationCollectorVisitor.getMethodInvocations()
			.stream()
			.map(methodAnalyzer::findAssertTransformationData)
			.filter(Optional::isPresent)
			.map(Optional::get)
			.collect(Collectors.toList());

		if (!assertTransformationDataList.isEmpty()) {
			transform(assertTransformationDataList);
		}

		return false;
	}

	private void transform(List<AssertTransformationData> assertTransformationDataList) {
		// ...
	}

}
