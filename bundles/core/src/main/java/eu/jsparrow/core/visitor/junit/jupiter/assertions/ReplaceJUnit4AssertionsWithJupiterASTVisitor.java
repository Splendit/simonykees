package eu.jsparrow.core.visitor.junit.jupiter.assertions;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;

import eu.jsparrow.core.visitor.junit.jupiter.common.AbstractReplaceJUnit4AssertionsWithJupiterASTVisitor;
import eu.jsparrow.rules.common.util.ASTNodeUtil;

/**
 * Replaces invocations of methods of the JUnit-4-class {@code org.junit.Assert}
 * by invocations of the corresponding methods of the JUnit-Jupiter-class
 * {@code org.junit.jupiter.api.Assertions}.
 * 
 * @since 3.28.0
 * 
 */
public class ReplaceJUnit4AssertionsWithJupiterASTVisitor extends AbstractReplaceJUnit4AssertionsWithJupiterASTVisitor {

	@Override
	public boolean visit(CompilationUnit compilationUnit) {

		boolean continueVisiting = super.visit(compilationUnit);
		if (!continueVisiting) {
			return false;
		}

		JUnit4AssertToJupiterAnalyzerVisitor analyzerVisitor = new JUnit4AssertToJupiterAnalyzerVisitor();
		compilationUnit.accept(analyzerVisitor);
		if (!analyzerVisitor.isTransformationPossible()) {
			return false;
		}

		verifyImport(compilationUnit, ORG_JUNIT_JUPITER_API_ASSERTIONS);

		JUnit4AssertMethodInvocationAnalyzer invocationAnalyzer = new JUnit4AssertMethodInvocationAnalyzer();
		List<JUnit4AssertMethodInvocationAnalysisResult> allJUnit4AssertInvocations = invocationAnalyzer
			.collectJUnit4AssertionAnalysisResults(
					compilationUnit);

		List<JUnit4AssertMethodInvocationAnalysisResult> jUnit4AssertInvocationsInJUnitJupiterTest = allJUnit4AssertInvocations
			.stream()
			.filter(JUnit4AssertMethodInvocationAnalysisResult::isTransformableInvocation)
			.collect(Collectors.toList());

		List<ImportDeclaration> staticAssertMethodImportsToRemove = collectStaticAssertMethodImportsToRemove(
				allJUnit4AssertInvocations);

		Set<String> unqualifiedNamesOfAssertMethodImportsToRemove = staticAssertMethodImportsToRemove
			.stream()
			.map(ImportDeclaration::getName)
			.filter(Name::isQualifiedName)
			.map(QualifiedName.class::cast)
			.map(QualifiedName::getName)
			.map(SimpleName::getIdentifier)
			.collect(Collectors.toSet());

		Set<String> newStaticAssertionMethodImports = new HashSet<>();
		Set<String> unqualifiedNamesOfNewAssertionMwethodImports = new HashSet<>();
		jUnit4AssertInvocationsInJUnitJupiterTest.forEach(data -> {
			String newMethodName = data.getDeprecatedMethodNameReplacement()
				.orElse(data.getMethodName());
			String newMethodFullyQualifiedName = createAssertionsMethodQualifiedName(newMethodName);
			if (unqualifiedNamesOfAssertMethodImportsToRemove.contains(newMethodName)
					|| canAddStaticAssertionsMethodImport(newMethodFullyQualifiedName)) {
				newStaticAssertionMethodImports.add(newMethodFullyQualifiedName);
				unqualifiedNamesOfNewAssertionMwethodImports.add(newMethodName);
			}
		});

		List<JUnit4AssertInvocationReplacementData> jUnit4AssertTransformationDataList = allJUnit4AssertInvocations
			.stream()
			.map(data -> this.findTransformationData(data, unqualifiedNamesOfNewAssertionMwethodImports))
			.filter(Optional::isPresent)
			.map(Optional::get)
			.collect(Collectors.toList());

		transform(staticAssertMethodImportsToRemove, newStaticAssertionMethodImports,
				jUnit4AssertTransformationDataList);

		return false;
	}

	private String createAssertionsMethodQualifiedName(String methodName) {
		return ORG_JUNIT_JUPITER_API_ASSERTIONS_PREFIX + methodName;
	}

	private List<ImportDeclaration> collectStaticAssertMethodImportsToRemove(
			List<JUnit4AssertMethodInvocationAnalysisResult> jUnit4AssertInvocationDataList) {
		Set<String> simpleNamesOfStaticAssertMethodImportsToKeep = jUnit4AssertInvocationDataList
			.stream()
			.filter(data -> !data.isTransformableInvocation())
			.filter(data -> data.getMethodInvocation()
				.getExpression() == null)
			.map(JUnit4AssertMethodInvocationAnalysisResult::getMethodName)
			.collect(Collectors.toSet());

		return ASTNodeUtil
			.convertToTypedList(getCompilationUnit().imports(), ImportDeclaration.class)
			.stream()
			.filter(importDeclaration -> canRemoveStaticImport(importDeclaration,
					simpleNamesOfStaticAssertMethodImportsToKeep))
			.collect(Collectors.toList());
	}

	private boolean canRemoveStaticImport(ImportDeclaration importDeclaration,
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
			return JUnit4AssertMethodInvocationAnalyzer.isSupportedJUnit4AssertMethod(methodBinding)
					&& !simpleNamesOfStaticAssertMethodImportsToKeep.contains(methodBinding.getName());
		}
		return false;
	}

	private boolean canAddStaticAssertionsMethodImport(String fullyQualifiedAssertionsMethodName) {
		verifyStaticMethodImport(getCompilationUnit(), fullyQualifiedAssertionsMethodName);
		return canAddStaticMethodImport(fullyQualifiedAssertionsMethodName);
	}

	private Optional<JUnit4AssertInvocationReplacementData> findTransformationData(
			JUnit4AssertMethodInvocationAnalysisResult invocationData,
			Set<String> unqualifiedNamesOfNewAssertionMwethodImports) {

		if (!invocationData.isTransformableInvocation()) {
			return Optional.empty();
		}
		MethodInvocation methodInvocation = invocationData.getMethodInvocation();
		String deprecatedethodNameReplacement = invocationData.getDeprecatedMethodNameReplacement()
			.orElse(null);
		String newMethodName = deprecatedethodNameReplacement != null ? deprecatedethodNameReplacement
				: invocationData.getMethodName();

		boolean newInvocationWithoutQualifier = unqualifiedNamesOfNewAssertionMwethodImports.contains(newMethodName);

		List<Expression> originalArguments = ASTNodeUtil.convertToTypedList(methodInvocation.arguments(),
				Expression.class);

		List<Expression> newArguments;
		if (invocationData.isMessageAsFirstParameter() && originalArguments.size() > 1) {
			newArguments = new ArrayList<>();
			Expression messageArgument = originalArguments.remove(0);
			newArguments.addAll(originalArguments);
			newArguments.add(messageArgument);
		} else {
			newArguments = originalArguments;
		}

		if (methodInvocation.getExpression() == null
				&& newInvocationWithoutQualifier
				&& newArguments == originalArguments
				&& deprecatedethodNameReplacement == null) {
			return Optional.empty();
		}

		Supplier<MethodInvocation> newMethodInvocationSupplier;
		if (newInvocationWithoutQualifier) {
			newMethodInvocationSupplier = () -> this.createNewInvocationWithoutQualifier(newMethodName, newArguments);
		} else {
			newMethodInvocationSupplier = () -> this.createNewInvocationWithAssertionsQualifier(methodInvocation,
					newMethodName, newArguments);
		}

		return Optional.of(new JUnit4AssertInvocationReplacementData(methodInvocation, newMethodInvocationSupplier));
	}

	private void transform(List<ImportDeclaration> staticAssertMethodImportsToRemove,
			Set<String> newStaticAssertionMethodImports,
			List<JUnit4AssertInvocationReplacementData> jUnit4AssertTransformationDataList) {
		if (!staticAssertMethodImportsToRemove.isEmpty() || !newStaticAssertionMethodImports.isEmpty()
				|| !jUnit4AssertTransformationDataList.isEmpty()) {

			staticAssertMethodImportsToRemove.forEach(importDeclaration -> astRewrite.remove(importDeclaration, null));

			AST ast = astRewrite.getAST();
			ListRewrite newImportsListRewrite = astRewrite.getListRewrite(getCompilationUnit(),
					CompilationUnit.IMPORTS_PROPERTY);
			newStaticAssertionMethodImports
				.forEach(fullyQualifiedMethodName -> {
					ImportDeclaration newImportDeclaration = ast.newImportDeclaration();
					newImportDeclaration.setName(ast.newName(fullyQualifiedMethodName));
					newImportDeclaration.setStatic(true);
					newImportsListRewrite.insertLast(newImportDeclaration, null);
				});

			jUnit4AssertTransformationDataList.forEach(data -> {
				MethodInvocation methodInvocationToReplace = data.getOriginalMethodInvocation();
				MethodInvocation methodInvocationReplacement = data.createMethodInvocationReplacement();
				astRewrite.replace(methodInvocationToReplace, methodInvocationReplacement, null);
			});
			onRewrite();
		}
	}
}