package eu.jsparrow.core.visitor.junit.jupiter.assertions;

import java.util.ArrayList;
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
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.LambdaExpression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
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
public class ReplaceJUnit4AssertWithJupiterASTVisitor extends AbstractAddImportASTVisitor {

	private static final String ORG_JUNIT_JUPITER_API_ASSERTIONS = "org.junit.jupiter.api.Assertions"; //$NON-NLS-1$
	private static final String ORG_JUNIT_JUPITER_API_ASSERTIONS_PREFIX = ORG_JUNIT_JUPITER_API_ASSERTIONS + "."; //$NON-NLS-1$

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

		MethodInvocationsCollectorVisitor invocationCollectorVisitor = new MethodInvocationsCollectorVisitor();
		compilationUnit.accept(invocationCollectorVisitor);

		List<MethodInvocation> allMethodInvocations = invocationCollectorVisitor.getMethodInvocations();

		List<JUnit4AssertMethodInvocationData> jUnit4AssertInvocationDataList = allMethodInvocations
			.stream()
			.map(JUnit4AssertMethodInvocationData::findJUnit4MethodInvocationData)
			.filter(Optional::isPresent)
			.map(Optional::get)
			.collect(Collectors.toList());

		List<ImportDeclaration> staticAssertMethodImportsToRemove = collectStaticAssertMethodImportsToRemove(
				jUnit4AssertInvocationDataList);

		Set<String> methodNamesOfAssertMethodImportsToRemove = staticAssertMethodImportsToRemove
			.stream()
			.map(ImportDeclaration::getName)
			.filter(Name::isQualifiedName)
			.map(QualifiedName.class::cast)
			.map(QualifiedName::getName)
			.map(SimpleName::getIdentifier)
			.collect(Collectors.toSet());

		List<JUnit4AssertTransformationData> jUnit4AssertTransformationDataList = jUnit4AssertInvocationDataList
			.stream()
			.map(data -> this.findTransformationData(data, methodNamesOfAssertMethodImportsToRemove))
			.filter(Optional::isPresent)
			.map(Optional::get)
			.collect(Collectors.toList());

		transform(staticAssertMethodImportsToRemove, jUnit4AssertTransformationDataList);

		return false;
	}

	private List<ImportDeclaration> collectStaticAssertMethodImportsToRemove(
			List<JUnit4AssertMethodInvocationData> jUnit4AssertInvocationDataList) {
		Set<String> simpleNamesOfStaticAssertMethodImportsToKeep = jUnit4AssertInvocationDataList
			.stream()
			.filter(data -> !data.isInvocationWithinJUnitJupiterTest())
			.filter(data -> data.getMethodInvocation()
				.getExpression() == null)
			.map(JUnit4AssertMethodInvocationData::getMethodName)
			.collect(Collectors.toSet());

		return ASTNodeUtil
			.convertToTypedList(getCompilationUnit().imports(), ImportDeclaration.class)
			.stream()
			.filter(importDeclaration -> canRemoveStaticImport(importDeclaration,
					simpleNamesOfStaticAssertMethodImportsToKeep))
			.collect(Collectors.toList());
	}

	private Optional<JUnit4AssertTransformationData> findTransformationData(
			JUnit4AssertMethodInvocationData invocationData,
			Set<String> methodNamesOfAssertMethodImportsToRemove) {
		if (!invocationData.isInvocationWithinJUnitJupiterTest()) {
			return Optional.empty();
		}
		MethodInvocation methodInvocation = invocationData.getMethodInvocation();
		String methodName = invocationData.getMethodName();
		String methodNameReplacement;
		if (isAssertEqualsComparingObjectArrays(methodName, invocationData.getDeclaredParameterTypes())) {
			methodNameReplacement = "assertArrayEquals"; //$NON-NLS-1$
		} else {
			methodNameReplacement = null;
		}
		String newMethodName = methodNameReplacement != null ? methodNameReplacement : methodName;
		String staticImportForNewInvocation = findStaticImportForNewInvocation(
				methodNamesOfAssertMethodImportsToRemove, newMethodName).orElse(null);

		List<Expression> invocationArguments = ASTNodeUtil.convertToTypedList(methodInvocation.arguments(),
				Expression.class);
		Expression assertionMessageAsFirstArgument;

		if (!invocationArguments.isEmpty() && isParameterTypeString(invocationData.getDeclaredParameterTypes()[0])) {
			assertionMessageAsFirstArgument = invocationArguments.get(0);
		} else {
			assertionMessageAsFirstArgument = null;
		}

		if (staticImportForNewInvocation != null
				&& methodInvocation.getExpression() == null
				&& assertionMessageAsFirstArgument == null
				&& methodNameReplacement == null) {
			return Optional
				.of(new JUnit4AssertTransformationData(methodInvocation, staticImportForNewInvocation));
		}

		Supplier<List<Expression>> newArgumentsSupplier;
		if (assertionMessageAsFirstArgument != null) {
			invocationArguments.remove(assertionMessageAsFirstArgument);
			newArgumentsSupplier = () -> this.createAssertionMethodArguments(invocationArguments,
					assertionMessageAsFirstArgument);
		} else {
			newArgumentsSupplier = () -> this.createAssertionMethodArguments(invocationArguments);
		}

		Supplier<MethodInvocation> newMethodInvocationSupplier;
		if (staticImportForNewInvocation != null) {
			newMethodInvocationSupplier = () -> this.createNewInvocationWithoutQualifier(newMethodName,
					newArgumentsSupplier);
		} else {
			newMethodInvocationSupplier = () -> this.createNewInvocationWithAssertionsQualifier(methodInvocation,
					newMethodName, newArgumentsSupplier);
		}

		if (staticImportForNewInvocation != null) {
			return Optional.of(new JUnit4AssertTransformationData(methodInvocation, staticImportForNewInvocation,
					newMethodInvocationSupplier));
		}
		return Optional.of(new JUnit4AssertTransformationData(methodInvocation, newMethodInvocationSupplier));
	}

	private Optional<String> findStaticImportForNewInvocation(
			Set<String> methodNamesOfAssertMethodImportsToRemove, String newMethodName) {
		String fullyQualifiedAssertionsMethodName = ORG_JUNIT_JUPITER_API_ASSERTIONS_PREFIX + newMethodName;
		if (methodNamesOfAssertMethodImportsToRemove.contains(newMethodName)) {
			return Optional.of(fullyQualifiedAssertionsMethodName);
		}
		verifyStaticMethodImport(getCompilationUnit(), fullyQualifiedAssertionsMethodName);
		if (canAddStaticMethodImport(fullyQualifiedAssertionsMethodName)) {
			return Optional.of(fullyQualifiedAssertionsMethodName);
		}
		return Optional.empty();
	}

	/**
	 * This applies to the following signatures:<br>
	 * {@code assertEquals(Object[], Object[])}
	 * {@code assertEquals(String, Object[], Object[])} where a corresponding
	 * method with the name "assertArrayEquals" is available
	 * 
	 * @return
	 */
	private boolean isAssertEqualsComparingObjectArrays(String methodName, ITypeBinding[] declaredParameterTypes) {
		if (!methodName.equals("assertEquals")) { //$NON-NLS-1$
			return false;
		}
		/*
		 * applies to {@code assertEquals(Object[], Object[])}
		 */
		if (declaredParameterTypes.length == 2) {
			return isParameterTypeObjectArray(declaredParameterTypes[0])
					&& isParameterTypeObjectArray(declaredParameterTypes[1]);
		}
		/*
		 * applies to {@code assertEquals(String, Object[], Object[])}
		 */
		if (declaredParameterTypes.length == 3) {
			return isParameterTypeString(declaredParameterTypes[0])
					&& isParameterTypeObjectArray(declaredParameterTypes[1])
					&& isParameterTypeObjectArray(declaredParameterTypes[2]);
		}
		return false;
	}

	private boolean isParameterTypeObjectArray(ITypeBinding parameterType) {
		if (parameterType.isArray()) {
			return parameterType.getComponentType()
				.getQualifiedName()
				.equals("java.lang.Object") && parameterType.getDimensions() == 1; //$NON-NLS-1$
		}
		return false;
	}

	private boolean isParameterTypeString(ITypeBinding parameterType) {
		return parameterType.getQualifiedName()
			.equals("java.lang.String"); //$NON-NLS-1$
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
			return JUnit4AssertMethodInvocationData.isSupportedJUnit4AssertMethod(methodBinding)
					&& !simpleNamesOfStaticAssertMethodImportsToKeep.contains(methodBinding.getName());
		}
		return false;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private MethodInvocation createNewInvocationWithoutQualifier(String newMethodName,
			Supplier<List<Expression>> newArgumentsSupplier) {
		AST ast = astRewrite.getAST();
		MethodInvocation newInvocation = ast.newMethodInvocation();
		newInvocation.setName(ast.newSimpleName(newMethodName));
		List newArguments = newInvocation.arguments();
		newArgumentsSupplier.get()
			.forEach(newArguments::add);
		return newInvocation;
	}

	private MethodInvocation createNewInvocationWithAssertionsQualifier(MethodInvocation contextForImport,
			String newMethodName, Supplier<List<Expression>> newArgumentsSupplier) {
		MethodInvocation newInvocation = createNewInvocationWithoutQualifier(newMethodName, newArgumentsSupplier);
		Name newQualifier = addImport(ORG_JUNIT_JUPITER_API_ASSERTIONS, contextForImport);
		newInvocation.setExpression(newQualifier);
		return newInvocation;
	}

	private List<Expression> createAssertionMethodArguments(List<Expression> arguments) {
		return arguments.stream()
			.map(arg -> (Expression) astRewrite.createCopyTarget(arg))
			.collect(Collectors.toList());
	}

	private List<Expression> createAssertionMethodArguments(List<Expression> arguments, Expression assertionMessage) {
		List<Expression> assertionMethodArguments = new ArrayList<>();
		arguments.stream()
			.map(arg -> (Expression) astRewrite.createCopyTarget(arg))
			.forEach(assertionMethodArguments::add);
		LambdaExpression messageSupplierLambdaExpression = astRewrite.getAST()
			.newLambdaExpression();
		messageSupplierLambdaExpression.setBody(astRewrite.createCopyTarget(assertionMessage));
		assertionMethodArguments.add(messageSupplierLambdaExpression);
		return assertionMethodArguments;
	}

	private void transform(List<ImportDeclaration> staticAssertMethodImportsToRemove,
			List<JUnit4AssertTransformationData> jUnit4AssertTransformationDataList) {
		if (!staticAssertMethodImportsToRemove.isEmpty() || !jUnit4AssertTransformationDataList.isEmpty()) {

			staticAssertMethodImportsToRemove.forEach(importDeclaration -> astRewrite.remove(importDeclaration, null));

			Set<String> qualifiedNamesOfNewStaticImports = jUnit4AssertTransformationDataList
				.stream()
				.map(JUnit4AssertTransformationData::getNewStaticMethodImport)
				.filter(Optional::isPresent)
				.map(Optional::get)
				.collect(Collectors.toSet());

			AST ast = astRewrite.getAST();
			ListRewrite newImportsListRewrite = astRewrite.getListRewrite(getCompilationUnit(),
					CompilationUnit.IMPORTS_PROPERTY);
			qualifiedNamesOfNewStaticImports
				.forEach(fullyQualifiedMethodName -> {
					ImportDeclaration newImportDeclaration = ast.newImportDeclaration();
					newImportDeclaration.setName(ast.newName(fullyQualifiedMethodName));
					newImportDeclaration.setStatic(true);
					newImportsListRewrite.insertLast(newImportDeclaration, null);
				});

			jUnit4AssertTransformationDataList.forEach(this::replaceAssertMethodInvocation);
		}
	}

	private void replaceAssertMethodInvocation(JUnit4AssertTransformationData transformationData) {
		transformationData.createMethodInvocationReplacement()
			.ifPresent(methodInvocationReplacement -> {
				MethodInvocation methodInvocationToReplace = transformationData.getOriginalMethodInvocation();
				astRewrite.replace(methodInvocationToReplace, methodInvocationReplacement, null);
			});
	}
}
