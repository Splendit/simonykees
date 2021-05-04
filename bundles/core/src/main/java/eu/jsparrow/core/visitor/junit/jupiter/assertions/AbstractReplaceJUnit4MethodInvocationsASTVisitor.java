package eu.jsparrow.core.visitor.junit.jupiter.assertions;

import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ImportDeclaration;

import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.visitor.AbstractAddImportASTVisitor;

abstract class AbstractReplaceJUnit4MethodInvocationsASTVisitor extends AbstractAddImportASTVisitor {

	protected JUnit4MethodInvocationAnalysisResultStore createTransformationDataStore(CompilationUnit compilationUnit) {
		JUnit4MethodInvocationAnalyzer analyzer = new JUnit4MethodInvocationAnalyzer(compilationUnit,
				this::isSupportedJUnit4Method);
		return analyzer.collectAnalysisResults();
	}

	protected List<ImportDeclaration> collectStaticMethodImportsToRemove(CompilationUnit compilationUnit,
			Predicate<IMethodBinding> supportedJUnit4MethodPredicate,
			List<JUnit4MethodInvocationAnalysisResult> jUnit4AssertInvocationDataList) {
		Set<String> simpleNamesOfStaticAssertMethodImportsToKeep = jUnit4AssertInvocationDataList
			.stream()
			.filter(data -> !data.isTransformable())
			.filter(data -> data.getMethodInvocation()
				.getExpression() == null)
			.map(JUnit4MethodInvocationAnalysisResult::getMethodBinding)
			.map(IMethodBinding::getName)
			.collect(Collectors.toSet());

		return ASTNodeUtil
			.convertToTypedList(compilationUnit.imports(), ImportDeclaration.class)
			.stream()
			.filter(importDeclaration -> canRemoveStaticImport(importDeclaration,
					supportedJUnit4MethodPredicate,
					simpleNamesOfStaticAssertMethodImportsToKeep))
			.collect(Collectors.toList());
	}

	private static boolean canRemoveStaticImport(ImportDeclaration importDeclaration,
			Predicate<IMethodBinding> supportedJUnit4MethodPredicate,
			Set<String> simpleNamesOfStaticAssertMethodImportsToKeep) {
		if (!importDeclaration.isStatic()) {
			return false;
		}

		if (importDeclaration.isOnDemand()) {
			return false;
		}
		IBinding importBinding = importDeclaration.resolveBinding();
		if (importBinding.getKind() == IBinding.METHOD) {
			IMethodBinding methodBinding = ((IMethodBinding) importBinding);
			return supportedJUnit4MethodPredicate.test(methodBinding)
					&& !simpleNamesOfStaticAssertMethodImportsToKeep.contains(methodBinding.getName());
		}
		return false;
	}

	protected abstract boolean isSupportedJUnit4Method(IMethodBinding methodBinding);
}
