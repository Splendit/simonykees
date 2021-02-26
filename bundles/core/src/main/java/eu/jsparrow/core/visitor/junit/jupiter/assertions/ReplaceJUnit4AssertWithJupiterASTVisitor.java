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
import org.eclipse.jdt.core.dom.MethodReference;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;

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

	private static final String ASSERT_ARRAY_EQUALS = "assertArrayEquals"; //$NON-NLS-1$
	private static final String ORG_JUNIT_JUPITER_API_ASSERTIONS = "org.junit.jupiter.api.Assertions"; //$NON-NLS-1$

	@Override
	public boolean visit(CompilationUnit compilationUnit) {

		boolean continueVisiting = super.visit(compilationUnit);
		if (!continueVisiting) {
			return false;
		}

		verifyImport(compilationUnit, ORG_JUNIT_JUPITER_API_ASSERTIONS);

		List<ImportDeclaration> assertMethodStaticImportsToRemove = ASTNodeUtil
			.convertToTypedList(compilationUnit.imports(), ImportDeclaration.class)
			.stream()
			.filter(this::isStaticImportOfAssertMethodToRemove)
			.collect(Collectors.toList());
		Set<String> assertMethodStaticImportsSimpleNames = assertMethodStaticImportsToRemove.stream()
			.map(ImportDeclaration::getName)
			.filter(Name::isQualifiedName)
			.map(QualifiedName.class::cast)
			.map(QualifiedName::getName)
			.map(SimpleName::getIdentifier)
			.collect(Collectors.toSet());

		MethodInvocationsCollectorVisitor invocationCollectorVisitor = new MethodInvocationsCollectorVisitor();
		compilationUnit.accept(invocationCollectorVisitor);

		List<AssertInvocationAnalysisResult> assertInvocationAnalysisResults = invocationCollectorVisitor
			.getMethodInvocations()
			.stream()
			.map(invocation -> analyzeAssertMethodInvocation(invocation, assertMethodStaticImportsSimpleNames))
			.filter(Optional::isPresent)
			.map(Optional::get)
			.collect(Collectors.toList());

		Set<String> assertionMethodNamesForNewStaticImports = assertInvocationAnalysisResults
			.stream()
			.filter(AssertInvocationAnalysisResult::isAlsoNewInvocationWithoutQualifier)
			.map(result -> result.isNameChangedToAssertArrayEquals() ? ASSERT_ARRAY_EQUALS
					: result.getOriginalInvocation()
						.getName()
						.getIdentifier())
			.collect(Collectors.toSet());

		List<AssertTransformationData> invocationReplacementData = assertInvocationAnalysisResults
			.stream()
			.map(this::findInvocationReplacementData)
			.filter(Optional::isPresent)
			.map(Optional::get)
			.collect(Collectors.toList());

		MethodReferenceCollectorVisitor methodReferenceCollectorVisitor = new MethodReferenceCollectorVisitor();
		compilationUnit.accept(methodReferenceCollectorVisitor);
		List<MethodReference> methodReferences = methodReferenceCollectorVisitor.getMethodReferences();

		if (!assertMethodStaticImportsToRemove.isEmpty() || !invocationReplacementData.isEmpty()
				|| !methodReferences.isEmpty()) {
			transform(assertMethodStaticImportsToRemove, assertionMethodNamesForNewStaticImports,
					invocationReplacementData, methodReferences);
		}
		return false;
	}

	private Optional<AssertInvocationAnalysisResult> analyzeAssertMethodInvocation(MethodInvocation methodInvocation,
			Set<String> assertMethodStaticImportsSimpleNames) {
		IMethodBinding methodBinding = methodInvocation.resolveMethodBinding();
		if (!isSupportedJUnit4Method(methodBinding)) {
			return Optional.empty();
		}
		IMethodBinding methodDeclaration = methodBinding
			.getMethodDeclaration();
		ITypeBinding[] declaredParameterTypes = methodDeclaration.getParameterTypes();

		boolean isNameChangedToAssertArrayEquals = isAssertEqualsComparingObjectArrays(methodBinding.getName(),
				declaredParameterTypes);
		boolean keepAlsoNewInvocationWithoutExpression;
		if (methodInvocation.getExpression() == null) {
			if (isNameChangedToAssertArrayEquals) {
				keepAlsoNewInvocationWithoutExpression = assertMethodStaticImportsSimpleNames
					.contains(ASSERT_ARRAY_EQUALS);
			} else {
				keepAlsoNewInvocationWithoutExpression = assertMethodStaticImportsSimpleNames
					.contains(methodDeclaration.getName());
			}
		} else {
			keepAlsoNewInvocationWithoutExpression = false;
		}

		List<Expression> invocationArguments = ASTNodeUtil.convertToTypedList(methodInvocation.arguments(),
				Expression.class);

		if (!invocationArguments.isEmpty() && isParameterTypeString(declaredParameterTypes[0])) {
			Expression assertionMessageAsFirstArgument = invocationArguments.get(0);
			return Optional
				.of(new AssertInvocationAnalysisResult(methodInvocation, keepAlsoNewInvocationWithoutExpression,
						isNameChangedToAssertArrayEquals, assertionMessageAsFirstArgument));
		}
		
		return Optional
			.of(new AssertInvocationAnalysisResult(methodInvocation, keepAlsoNewInvocationWithoutExpression,
					isNameChangedToAssertArrayEquals));

	}

	private Optional<AssertTransformationData> findInvocationReplacementData(
			AssertInvocationAnalysisResult analysisResult) {

		if (analysisResult.isAlsoNewInvocationWithoutQualifier()
				&& !analysisResult.isNameChangedToAssertArrayEquals()
				&& !analysisResult.getMessageAsFirstArgument()
					.isPresent()) {
			return Optional.empty();
		}
		MethodInvocation originalInvocation = analysisResult.getOriginalInvocation();

		String newMethodName = analysisResult.isNameChangedToAssertArrayEquals() ? ASSERT_ARRAY_EQUALS
				: originalInvocation.getName()
					.getIdentifier();

		List<Expression> invocationArguments = ASTNodeUtil.convertToTypedList(originalInvocation.arguments(),
				Expression.class);

		Expression assertionMessageAsFirstArgument = analysisResult.getMessageAsFirstArgument()
			.orElse(null);

		Supplier<MethodInvocation> newMethodInvocationSupplier;
		if (analysisResult.isAlsoNewInvocationWithoutQualifier()) {
			newMethodInvocationSupplier = () -> this.createNewInvocationWithoutQualifier(newMethodName);
		} else {
			newMethodInvocationSupplier = () -> this.createNewInvocationWithAssertionsAsQualifier(originalInvocation,
					newMethodName);
		}

		Supplier<List<Expression>> newArgumentsSupplier;
		if (assertionMessageAsFirstArgument != null) {
			invocationArguments.remove(assertionMessageAsFirstArgument);
			newArgumentsSupplier = () -> this.createAssertionMethodArguments(invocationArguments,
					assertionMessageAsFirstArgument);
		} else {
			newArgumentsSupplier = () -> this.createAssertionMethodArguments(invocationArguments);
		}
		return Optional
			.of(new AssertTransformationData(originalInvocation, newMethodInvocationSupplier, newArgumentsSupplier));

	}

	private boolean isSupportedJUnit4Method(IMethodBinding methodBinding) {
		if (isOrgJUnitAssertClass(methodBinding.getDeclaringClass())) {
			String methodName = methodBinding.getName();
			return !methodName.equals("assertThat") //$NON-NLS-1$
					&& !methodName.equals("assertThrows"); //$NON-NLS-1$
		}
		return false;
	}

	private boolean isOrgJUnitAssertClass(ITypeBinding declaringClass) {
		return declaringClass.getQualifiedName()
			.equals("org.junit.Assert");//$NON-NLS-1$
	}

	/**
	 * This applies to the following signatures:<br>
	 * {@code assertEquals(Object[], Object[])}
	 * {@code assertEquals(String, Object[], Object[])} where a corresponding
	 * method with the name "assertArrayEquals" is available
	 * 
	 * @param methodBinding
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
		return parameterType.getComponentType()
			.getQualifiedName()
			.equals("java.lang.Object") && parameterType.getDimensions() == 1; //$NON-NLS-1$
	}

	private boolean isParameterTypeString(ITypeBinding parameterType) {
		return parameterType.getQualifiedName()
			.equals("java.lang.String"); //$NON-NLS-1$
	}

	private boolean isStaticImportOfAssertMethodToRemove(ImportDeclaration importDeclaration) {
		if (!importDeclaration.isStatic()) {
			return false;
		}

		if (!importDeclaration.isOnDemand()) {
			return false;
		}
		IBinding importBinding = importDeclaration.resolveBinding();
		if (importBinding.getKind() == IBinding.METHOD) {
			IMethodBinding methodBinding = ((IMethodBinding) importBinding);
			return isSupportedJUnit4Method(methodBinding);
		}
		return false;
	}

	private MethodInvocation createNewInvocationWithoutQualifier(String newMethodName) {
		AST ast = astRewrite.getAST();
		MethodInvocation newInvocation = ast.newMethodInvocation();
		newInvocation.setName(ast.newSimpleName(newMethodName));
		return newInvocation;
	}

	private MethodInvocation createNewInvocationWithAssertionsAsQualifier(MethodInvocation contextForImport,
			String newMethodName) {
		MethodInvocation newInvocation = createNewInvocationWithoutQualifier(newMethodName);
		Name assertionTypeName = addImport(ORG_JUNIT_JUPITER_API_ASSERTIONS, contextForImport);
		newInvocation.setExpression(assertionTypeName);
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
		messageSupplierLambdaExpression.setBody((Expression) astRewrite.createCopyTarget(assertionMessage));
		assertionMethodArguments.add(messageSupplierLambdaExpression);
		return assertionMethodArguments;
	}

	private void transform(List<ImportDeclaration> assertMethodStaticImportsToRemove,
			Set<String> assertionMethodNamesForNewStaticImports,
			List<AssertTransformationData> assertTransformationDataList,
			List<MethodReference> methodReferences) {
		// ...
	}
}
