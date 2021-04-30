package eu.jsparrow.core.visitor.junit.jupiter.assertions;

import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;

import eu.jsparrow.rules.common.util.ASTNodeUtil;

class StaticMethodImportsToRemoveHelper {

	private final List<ImportDeclaration> staticMethodImportsToRemove;
	private final Set<String> simpleNamesOfStaticMethodImportsToRemove;

	StaticMethodImportsToRemoveHelper(CompilationUnit compilationUnit,
			Predicate<IMethodBinding> supportedJUnit4MethodPredicate,
			List<JUnit4MethodInvocationAnalysisResult> jUnit4AssertInvocationDataList) {

		staticMethodImportsToRemove = collectStaticAssertMethodImportsToRemove(compilationUnit,
				supportedJUnit4MethodPredicate, jUnit4AssertInvocationDataList);

		simpleNamesOfStaticMethodImportsToRemove = staticMethodImportsToRemove
			.stream()
			.map(ImportDeclaration::getName)
			.filter(Name::isQualifiedName)
			.map(QualifiedName.class::cast)
			.map(QualifiedName::getName)
			.map(SimpleName::getIdentifier)
			.collect(Collectors.toSet());
	}

	private List<ImportDeclaration> collectStaticAssertMethodImportsToRemove(CompilationUnit compilationUnit,
			Predicate<IMethodBinding> supportedJUnit4MethodPredicate,
			List<JUnit4MethodInvocationAnalysisResult> jUnit4AssertInvocationDataList) {
		Set<String> simpleNamesOfStaticAssertMethodImportsToKeep = jUnit4AssertInvocationDataList
			.stream()
			.filter(data -> !data.isTransformableInvocation())
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

	public List<ImportDeclaration> getStaticMethodImportsToRemove() {
		return staticMethodImportsToRemove;
	}

	boolean isSimpleNameOfStaticMethodImportToRemove(String newMethodName) {
		return simpleNamesOfStaticMethodImportsToRemove.contains(newMethodName);
	}
}
