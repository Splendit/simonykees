package eu.jsparrow.core.visitor.junit.jupiter.assertions;

import java.util.List;

import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodReference;

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
		JUnit4AssertMethodAnalyzer methodAnalyzer = new JUnit4AssertMethodAnalyzer();
		List<AssertTransformationData> assertTransformationDataList = methodAnalyzer
			.createAssertInvocationTransformationDataList(compilationUnit);

		MethodReferenceCollectorVisitor methodReferenceCollectorVisitor = new MethodReferenceCollectorVisitor();
		compilationUnit.accept(methodReferenceCollectorVisitor);
		List<MethodReference> methodReferences = methodReferenceCollectorVisitor.getMethodReferences();

		if (!assertTransformationDataList.isEmpty() || !methodReferences.isEmpty()) {
			transform(assertTransformationDataList, methodReferences);
		}
		return false;
	}

	private void transform(List<AssertTransformationData> assertTransformationDataList,
			List<MethodReference> methodReferences) {
		// ...
	}
}
