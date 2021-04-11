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
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;

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
abstract class AbstractJUnit4MethodInvocationToJupiterASTVisitor extends AbstractAddImportASTVisitor {

	private static final String ORG_JUNIT_JUPITER_API_ASSERTIONS = "org.junit.jupiter.api.Assertions"; //$NON-NLS-1$
	private static final String ORG_JUNIT_JUPITER_API_FUNCTION_EXECUTABLE = "org.junit.jupiter.api.function.Executable"; //$NON-NLS-1$

	private static final String ORG_JUNIT_JUPITER_API_ASSERTIONS_PREFIX = ORG_JUNIT_JUPITER_API_ASSERTIONS + "."; //$NON-NLS-1$

	@Override
	public boolean visit(CompilationUnit compilationUnit) {

		super.visit(compilationUnit);

		verifyImport(compilationUnit, ORG_JUNIT_JUPITER_API_ASSERTIONS);
		verifyImport(compilationUnit, ORG_JUNIT_JUPITER_API_FUNCTION_EXECUTABLE);

		JUnit4AssertMethodInvocationAnalyzer invocationAnalyzer = new JUnit4AssertMethodInvocationAnalyzer(
				compilationUnit);

		List<JUnit4AssertMethodInvocationAnalysisResult> allJUnit4AssertInvocations = invocationAnalyzer
			.collectJUnit4AssertionAnalysisResults(
					compilationUnit);

		List<JUnit4AssertMethodInvocationAnalysisResult> jUnit4AssertInvocationsInJUnitJupiterTest = allJUnit4AssertInvocations
			.stream()
			.filter(JUnit4AssertMethodInvocationAnalysisResult::isTransformableInvocation)
			.collect(Collectors.toList());

		List<ImportDeclaration> staticAssertMethodImportsToRemove = collectStaticAssertMethodImportsToRemove(
				allJUnit4AssertInvocations, invocationAnalyzer);

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
			String newMethodName = data.getNewMethodName();
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

		List<Type> throwingRunnableTypesToReplace = allJUnit4AssertInvocations.stream()
			.filter(JUnit4AssertMethodInvocationAnalysisResult::isTransformableInvocation)
			.map(JUnit4AssertMethodInvocationAnalysisResult::getThrowingRunnableTypeToReplace)
			.filter(Optional::isPresent)
			.map(Optional::get)
			.collect(Collectors.toList());

		transform(staticAssertMethodImportsToRemove, newStaticAssertionMethodImports, throwingRunnableTypesToReplace,
				jUnit4AssertTransformationDataList);

		return false;
	}

	private String createAssertionsMethodQualifiedName(String methodName) {
		return ORG_JUNIT_JUPITER_API_ASSERTIONS_PREFIX + methodName;
	}

	private List<ImportDeclaration> collectStaticAssertMethodImportsToRemove(
			List<JUnit4AssertMethodInvocationAnalysisResult> jUnit4AssertInvocationDataList,
			JUnit4AssertMethodInvocationAnalyzer invocationAnalyzer) {
		Set<String> simpleNamesOfStaticAssertMethodImportsToKeep = jUnit4AssertInvocationDataList
			.stream()
			.filter(data -> !data.isTransformableInvocation())
			.filter(data -> data.getMethodInvocation()
				.getExpression() == null)
			.map(JUnit4AssertMethodInvocationAnalysisResult::getOriginalMethodName)
			.collect(Collectors.toSet());

		return ASTNodeUtil
			.convertToTypedList(getCompilationUnit().imports(), ImportDeclaration.class)
			.stream()
			.filter(importDeclaration -> canRemoveStaticImport(importDeclaration,
					simpleNamesOfStaticAssertMethodImportsToKeep, invocationAnalyzer))
			.collect(Collectors.toList());
	}

	private boolean canRemoveStaticImport(ImportDeclaration importDeclaration,
			Set<String> simpleNamesOfStaticAssertMethodImportsToKeep,
			JUnit4AssertMethodInvocationAnalyzer invocationAnalyzer) {
		if (!importDeclaration.isStatic()) {
			return false;
		}

		if (importDeclaration.isOnDemand()) {
			return false;
		}
		IBinding importBinding = importDeclaration.resolveBinding();
		if (importBinding.getKind() == IBinding.METHOD) {
			IMethodBinding methodBinding = ((IMethodBinding) importBinding);
			return invocationAnalyzer.isSupportedJUnit4AssertMethod(methodBinding)
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
		String originalMethodName = invocationData.getOriginalMethodName();
		String newMethodName = invocationData.getNewMethodName();

		boolean newInvocationWithoutQualifier = unqualifiedNamesOfNewAssertionMwethodImports.contains(newMethodName);

		List<Expression> originalArguments = ASTNodeUtil.convertToTypedList(methodInvocation.arguments(),
				Expression.class);

		List<Expression> newArguments;
		if (invocationData.isMessageMovingToLastPosition() && originalArguments.size() > 1) {
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
				&& newMethodName.equals(originalMethodName)) {
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

	@SuppressWarnings({ "unchecked" })
	private MethodInvocation createNewInvocationWithoutQualifier(String newMethodName,
			List<Expression> arguments) {
		AST ast = astRewrite.getAST();
		MethodInvocation newInvocation = ast.newMethodInvocation();
		newInvocation.setName(ast.newSimpleName(newMethodName));
		List<Expression> newInvocationArguments = newInvocation.arguments();
		arguments.stream()
			.map(arg -> (Expression) astRewrite.createCopyTarget(arg))
			.forEach(newInvocationArguments::add);
		return newInvocation;
	}

	private MethodInvocation createNewInvocationWithAssertionsQualifier(MethodInvocation contextForImport,
			String newMethodName, List<Expression> arguments) {
		MethodInvocation newInvocation = createNewInvocationWithoutQualifier(newMethodName, arguments);
		Name newQualifier = addImport(ORG_JUNIT_JUPITER_API_ASSERTIONS, contextForImport);
		newInvocation.setExpression(newQualifier);
		return newInvocation;
	}

	private void transform(List<ImportDeclaration> staticAssertMethodImportsToRemove,
			Set<String> newStaticAssertionMethodImports,
			List<Type> throwingRunnableTypesToReplace,
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

			throwingRunnableTypesToReplace.forEach(typeToReplace -> {
				Name executableTypeName = addImport(ORG_JUNIT_JUPITER_API_FUNCTION_EXECUTABLE, typeToReplace);
				SimpleType typeReplacement = ast.newSimpleType(executableTypeName);
				astRewrite.replace(typeToReplace, typeReplacement, null);
			});
			onRewrite();
		}
	}
}