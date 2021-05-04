package eu.jsparrow.core.visitor.junit.jupiter.assertions;

import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IMethodBinding;

import eu.jsparrow.rules.common.visitor.AbstractAddImportASTVisitor;

abstract class AbstractReplaceJUnit4MethodInvocationsASTVisitor extends AbstractAddImportASTVisitor{
	
	protected JUnit4MethodInvocationAnalysisResultStore createTransformationDataStore(CompilationUnit compilationUnit) {
		JUnit4MethodInvocationAnalyzer analyzer = new JUnit4MethodInvocationAnalyzer(compilationUnit,
				this::isSupportedJUnit4Method);
		return analyzer.collectAnalysisResults();
	}

	protected abstract boolean isSupportedJUnit4Method(IMethodBinding methodBinding);

}
