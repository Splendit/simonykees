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
 * This visitor replaces invocations of methods which are declared in a JUnit 4
 * class like {@code org.junit.Assert} or {@code org.junit.Assume} by
 * invocations of the corresponding JUnit Jupiter methods.
 * 
 * @since 3.30.0
 * 
 */
abstract class AbstractJUnit4MethodInvocationToJupiterASTVisitor extends AbstractAddImportASTVisitor {
	private static final String ORG_JUNIT_JUPITER_API_FUNCTION_EXECUTABLE = "org.junit.jupiter.api.function.Executable"; //$NON-NLS-1$
	private final String classDeclaringJUnitJupiterMethod;

	AbstractJUnit4MethodInvocationToJupiterASTVisitor(String classDeclaringJUnitJupiterMethod) {
		this.classDeclaringJUnitJupiterMethod = classDeclaringJUnitJupiterMethod;
	}

	@Override
	public boolean visit(CompilationUnit compilationUnit) {

		super.visit(compilationUnit);

		verifyImport(compilationUnit, classDeclaringJUnitJupiterMethod);
		verifyImport(compilationUnit, ORG_JUNIT_JUPITER_API_FUNCTION_EXECUTABLE);

		JUnit4MethodInvocationAnalyzer invocationAnalyzer = new JUnit4MethodInvocationAnalyzer(
				compilationUnit, this::isSupportedJUnit4Method);

		List<JUnit4MethodInvocationAnalysisResult> allJUnit4AssertInvocations = invocationAnalyzer
			.collectJUnit4AssertionAnalysisResults(
					compilationUnit);

		List<JUnit4MethodInvocationAnalysisResult> jUnit4AssertInvocationsInJUnitJupiterTest = allJUnit4AssertInvocations
			.stream()
			.filter(JUnit4MethodInvocationAnalysisResult::isTransformableInvocation)
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
		String newMethodFullyQualifiedNamePrefix = classDeclaringJUnitJupiterMethod + "."; //$NON-NLS-1$
		jUnit4AssertInvocationsInJUnitJupiterTest.forEach(data -> {
			String newMethodName = data.getNewMethodName();
			String newMethodFullyQualifiedName = newMethodFullyQualifiedNamePrefix + newMethodName;
			if (unqualifiedNamesOfAssertMethodImportsToRemove.contains(newMethodName)
					|| canAddStaticAssertionsMethodImport(newMethodFullyQualifiedName)) {
				newStaticAssertionMethodImports.add(newMethodFullyQualifiedName);
				unqualifiedNamesOfNewAssertionMwethodImports.add(newMethodName);
			}
		});

		List<JUnit4MethodInvocationReplacementData> jUnit4AssertTransformationDataList = allJUnit4AssertInvocations
			.stream()
			.map(data -> this.findTransformationData(data, unqualifiedNamesOfNewAssertionMwethodImports))
			.filter(Optional::isPresent)
			.map(Optional::get)
			.collect(Collectors.toList());

		List<Type> throwingRunnableTypesToReplace = allJUnit4AssertInvocations.stream()
			.filter(JUnit4MethodInvocationAnalysisResult::isTransformableInvocation)
			.map(JUnit4MethodInvocationAnalysisResult::getThrowingRunnableTypeToReplace)
			.filter(Optional::isPresent)
			.map(Optional::get)
			.collect(Collectors.toList());

		transform(staticAssertMethodImportsToRemove, newStaticAssertionMethodImports, throwingRunnableTypesToReplace,
				jUnit4AssertTransformationDataList);

		return false;
	}

	private List<ImportDeclaration> collectStaticAssertMethodImportsToRemove(
			List<JUnit4MethodInvocationAnalysisResult> jUnit4AssertInvocationDataList) {
		Set<String> simpleNamesOfStaticAssertMethodImportsToKeep = jUnit4AssertInvocationDataList
			.stream()
			.filter(data -> !data.isTransformableInvocation())
			.filter(data -> data.getMethodInvocation()
				.getExpression() == null)
			.map(JUnit4MethodInvocationAnalysisResult::getOriginalMethodName)
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
			return isSupportedJUnit4Method(methodBinding)
					&& !simpleNamesOfStaticAssertMethodImportsToKeep.contains(methodBinding.getName());
		}
		return false;
	}

	private boolean canAddStaticAssertionsMethodImport(String fullyQualifiedAssertionsMethodName) {
		verifyStaticMethodImport(getCompilationUnit(), fullyQualifiedAssertionsMethodName);
		return canAddStaticMethodImport(fullyQualifiedAssertionsMethodName);
	}

	private Optional<JUnit4MethodInvocationReplacementData> findTransformationData(
			JUnit4MethodInvocationAnalysisResult invocationData,
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

		return Optional.of(new JUnit4MethodInvocationReplacementData(methodInvocation, newMethodInvocationSupplier));
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
		Name newQualifier = addImport(classDeclaringJUnitJupiterMethod, contextForImport);
		newInvocation.setExpression(newQualifier);
		return newInvocation;
	}

	private void transform(List<ImportDeclaration> staticAssertMethodImportsToRemove,
			Set<String> newStaticAssertionMethodImports,
			List<Type> throwingRunnableTypesToReplace,
			List<JUnit4MethodInvocationReplacementData> jUnit4AssertTransformationDataList) {
		if (!staticAssertMethodImportsToRemove.isEmpty() || !newStaticAssertionMethodImports.isEmpty()
				|| !jUnit4AssertTransformationDataList.isEmpty()) {

			staticAssertMethodImportsToRemove.forEach(importDeclaration -> {
				astRewrite.remove(importDeclaration, null);
				onRewrite();
			});

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
				onRewrite();
			});

			throwingRunnableTypesToReplace.forEach(typeToReplace -> {
				Name executableTypeName = addImport(ORG_JUNIT_JUPITER_API_FUNCTION_EXECUTABLE, typeToReplace);
				SimpleType typeReplacement = ast.newSimpleType(executableTypeName);
				astRewrite.replace(typeToReplace, typeReplacement, null);
			});

		}
	}

	protected abstract boolean isSupportedJUnit4Method(IMethodBinding methodBinding);
}