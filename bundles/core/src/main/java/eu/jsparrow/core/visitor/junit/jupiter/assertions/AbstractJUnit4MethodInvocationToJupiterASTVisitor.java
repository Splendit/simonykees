package eu.jsparrow.core.visitor.junit.jupiter.assertions;

import static eu.jsparrow.rules.common.util.ClassRelationUtil.isContentOfType;

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
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Name;
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
	private final String classDeclaringJUnit4Method;
	private final String classDeclaringJUnitJupiterMethod;

	AbstractJUnit4MethodInvocationToJupiterASTVisitor(String classDeclaringJUnit4Method,
			String classDeclaringJUnitJupiterMethod) {
		this.classDeclaringJUnit4Method = classDeclaringJUnit4Method;
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
			.collectJUnit4AssertionAnalysisResults(compilationUnit);

		StaticMethodImportsToRemoveHelper staticMethodImportsToRemoveHelper = new StaticMethodImportsToRemoveHelper(
				compilationUnit, this::isSupportedJUnit4Method, allJUnit4AssertInvocations);

		List<JUnit4MethodInvocationAnalysisResult> transformableJUnit4InvocationAnalysisResults = allJUnit4AssertInvocations
			.stream()
			.filter(JUnit4MethodInvocationAnalysisResult::isTransformableInvocation)
			.collect(Collectors.toList());

		List<ImportDeclaration> staticAssertMethodImportsToRemove = staticMethodImportsToRemoveHelper
			.getStaticMethodImportsToRemove();

		Set<String> newStaticAssertionMethodImports = new HashSet<>();
		Set<String> unqualifiedNamesOfNewAssertionMethodImports = new HashSet<>();
		String newMethodFullyQualifiedNamePrefix = classDeclaringJUnitJupiterMethod + "."; //$NON-NLS-1$
		transformableJUnit4InvocationAnalysisResults.forEach(data -> {
			String newMethodName = data.getNewMethodName();
			String newMethodFullyQualifiedName = newMethodFullyQualifiedNamePrefix + newMethodName;
			if (staticMethodImportsToRemoveHelper.isSimpleNameOfStaticMethodImportToRemove(newMethodName)
					|| canAddStaticAssertionsMethodImport(newMethodFullyQualifiedName)) {
				newStaticAssertionMethodImports.add(newMethodFullyQualifiedName);
				unqualifiedNamesOfNewAssertionMethodImports.add(newMethodName);
			}
		});

		List<JUnit4MethodInvocationReplacementData> jUnit4AssertTransformationDataList = allJUnit4AssertInvocations
			.stream()
			.map(data -> this.findTransformationData(data, unqualifiedNamesOfNewAssertionMethodImports))
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

	private Set<String> findSupportedStaticImports(
			StaticMethodImportsToRemoveHelper staticMethodImportsToRemoveHelper,
			List<JUnit4MethodInvocationAnalysisResult> transformableJUnit4InvocationAnalysisResults) {

		Set<String> supportedNewStaticMethodImports = new HashSet<>();
		String newMethodFullyQualifiedNamePrefix = classDeclaringJUnitJupiterMethod + "."; //$NON-NLS-1$
		transformableJUnit4InvocationAnalysisResults.forEach(data -> {
			String supportedNewMethodName = data.getMethodBinding().getName();
			String supportedNewMethodFullyQualifiedName = newMethodFullyQualifiedNamePrefix + supportedNewMethodName;
			if (staticMethodImportsToRemoveHelper.isSimpleNameOfStaticMethodImportToRemove(supportedNewMethodName)
					|| canAddStaticAssertionsMethodImport(supportedNewMethodFullyQualifiedName)) {
				supportedNewStaticMethodImports.add(supportedNewMethodFullyQualifiedName);
			}
		});
		if(classDeclaringJUnit4Method.equals("org.junit.Assert")) { //$NON-NLS-1$
			supportedNewStaticMethodImports.add("org.junit.Assert.assertArrayEquals"); //$NON-NLS-1$
		}
		return supportedNewStaticMethodImports;
	}

	private boolean canAddStaticAssertionsMethodImport(String fullyQualifiedAssertionsMethodName) {
		verifyStaticMethodImport(getCompilationUnit(), fullyQualifiedAssertionsMethodName);
		return canAddStaticMethodImport(fullyQualifiedAssertionsMethodName);
	}

	private Optional<JUnit4MethodInvocationReplacementData> findTransformationData(
			JUnit4MethodInvocationAnalysisResult invocationData,
			Set<String> unqualifiedNamesOfNewAssertionMethodImports) {

		if (!invocationData.isTransformableInvocation()) {
			return Optional.empty();
		}
		MethodInvocation methodInvocation = invocationData.getMethodInvocation();
		String originalMethodName = invocationData.getOriginalMethodName();
		String newMethodName = invocationData.getNewMethodName();

		boolean newInvocationWithoutQualifier = unqualifiedNamesOfNewAssertionMethodImports.contains(newMethodName);

		List<Expression> originalArguments = ASTNodeUtil.convertToTypedList(methodInvocation.arguments(),
				Expression.class);

		ITypeBinding[] declaredParameterTypes = invocationData.getMethodBinding()
			.getMethodDeclaration()
			.getParameterTypes();

		boolean messageMovingToLastPosition = declaredParameterTypes.length > 0
				&& isParameterTypeString(declaredParameterTypes[0]);

		List<Expression> newArguments;
		if (messageMovingToLastPosition && originalArguments.size() > 1) {
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

	private boolean isParameterTypeString(ITypeBinding parameterType) {
		return isContentOfType(parameterType, "java.lang.String"); //$NON-NLS-1$
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