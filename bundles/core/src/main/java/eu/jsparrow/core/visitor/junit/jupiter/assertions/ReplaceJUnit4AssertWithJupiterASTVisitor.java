package eu.jsparrow.core.visitor.junit.jupiter.assertions;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IAnnotationBinding;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.LambdaExpression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.TypeDeclaration;
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

	private static final String ASSERT_ARRAY_EQUALS = "assertArrayEquals"; //$NON-NLS-1$
	private static final String ORG_JUNIT_JUPITER_API_ASSERTIONS = "org.junit.jupiter.api.Assertions"; //$NON-NLS-1$
	private static final String ORG_JUNIT_JUPITER_API_ASSERTIONS_PREFIX = ORG_JUNIT_JUPITER_API_ASSERTIONS + "."; //$NON-NLS-1$
	private static final String ORG_JUNIT_JUPITER_API_TEST = "org.junit.jupiter.api.Test"; //$NON-NLS-1$

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

		List<ImportDeclaration> staticAssertMethodImportsToRemove = collectStaticAssertMethodImportsToRemove(
				allMethodInvocations);

		Set<String> methodNamesOfAssertMethodImportsToRemove = staticAssertMethodImportsToRemove
			.stream()
			.map(ImportDeclaration::getName)
			.filter(Name::isQualifiedName)
			.map(QualifiedName.class::cast)
			.map(QualifiedName::getName)
			.map(SimpleName::getIdentifier)
			.collect(Collectors.toSet());

		List<AssertInvocationAnalysisResult> assertInvocationAnalysisResults = allMethodInvocations
			.stream()
			.map(invocation -> analyzeAssertMethodInvocation(invocation, methodNamesOfAssertMethodImportsToRemove))
			.filter(Optional::isPresent)
			.map(Optional::get)
			.collect(Collectors.toList());

		Set<String> assertionMethodSimpleNamesForNewStaticImports = assertInvocationAnalysisResults
			.stream()
			.filter(AssertInvocationAnalysisResult::isNewInvocationWithoutQualifier)
			.map(this::getNewMethodName)
			.collect(Collectors.toSet());

		List<AssertTransformationData> invocationReplacementData = assertInvocationAnalysisResults
			.stream()
			.filter(this::isAnyChangeOnMethodInvocation)
			.map(this::findInvocationReplacementData)
			.collect(Collectors.toList());

		if (!staticAssertMethodImportsToRemove.isEmpty()
				|| !assertionMethodSimpleNamesForNewStaticImports.isEmpty()
				|| !invocationReplacementData.isEmpty()) {
			transform(staticAssertMethodImportsToRemove, assertionMethodSimpleNamesForNewStaticImports,
					invocationReplacementData);
		}
		return false;
	}

	private List<ImportDeclaration> collectStaticAssertMethodImportsToRemove(
			List<MethodInvocation> allMethodInvocations) {
		Set<String> simpleNamesOfStaticAssertMethodImportsToKeep = allMethodInvocations
			.stream()
			.filter(invocation -> !isInvocationWithinJUnitJupiterTest(invocation))
			.filter(invocation -> invocation.getExpression() == null)
			.map(MethodInvocation::resolveMethodBinding)
			.filter(this::isSupportedJUnit4Method)
			.map(IMethodBinding::getName)
			.collect(Collectors.toSet());

		List<ImportDeclaration> originalStaticAssertMethodImportsToRemove = ASTNodeUtil
			.convertToTypedList(getCompilationUnit().imports(), ImportDeclaration.class)
			.stream()
			.filter(importDeclaration -> canRemoveStaticImport(importDeclaration,
					simpleNamesOfStaticAssertMethodImportsToKeep))
			.collect(Collectors.toList());
		return originalStaticAssertMethodImportsToRemove;
	}

	private Optional<AssertInvocationAnalysisResult> analyzeAssertMethodInvocation(MethodInvocation methodInvocation,
			Set<String> methodNamesOfAssertMethodImportsToRemove) {
		IMethodBinding methodBinding = methodInvocation.resolveMethodBinding();
		if (!isSupportedJUnit4Method(methodBinding)) {
			return Optional.empty();
		}
		if (!isInvocationWithinJUnitJupiterTest(methodInvocation)) {
			return Optional.empty();
		}
		IMethodBinding methodDeclaration = methodBinding
			.getMethodDeclaration();
		ITypeBinding[] declaredParameterTypes = methodDeclaration.getParameterTypes();

		boolean isNameChangedToAssertArrayEquals = isAssertEqualsComparingObjectArrays(methodBinding.getName(),
				declaredParameterTypes);
		String newMethodName = isNameChangedToAssertArrayEquals ? ASSERT_ARRAY_EQUALS : methodDeclaration.getName();
		boolean newInvocationWithoutQualifier;

		if (methodNamesOfAssertMethodImportsToRemove.contains(newMethodName)) {
			newInvocationWithoutQualifier = true;
		} else {
			String fullyQualifiedAssertionsMethodName = ORG_JUNIT_JUPITER_API_ASSERTIONS_PREFIX
					+ newMethodName;
			verifyStaticMethodImport(getCompilationUnit(), fullyQualifiedAssertionsMethodName);
			newInvocationWithoutQualifier = canAddStaticMethodImport(fullyQualifiedAssertionsMethodName);
		}

		List<Expression> invocationArguments = ASTNodeUtil.convertToTypedList(methodInvocation.arguments(),
				Expression.class);

		if (!invocationArguments.isEmpty() && isParameterTypeString(declaredParameterTypes[0])) {
			Expression assertionMessageAsFirstArgument = invocationArguments.get(0);
			return Optional
				.of(new AssertInvocationAnalysisResult(methodInvocation, newInvocationWithoutQualifier,
						isNameChangedToAssertArrayEquals, assertionMessageAsFirstArgument));
		}

		return Optional
			.of(new AssertInvocationAnalysisResult(methodInvocation, newInvocationWithoutQualifier,
					isNameChangedToAssertArrayEquals));

	}

	String getNewMethodName(AssertInvocationAnalysisResult analysisResult) {
		if (analysisResult.isNameChangedToAssertArrayEquals()) {
			return ASSERT_ARRAY_EQUALS;
		}
		return analysisResult.getOriginalInvocation()
			.getName()
			.getIdentifier();
	}

	private boolean isAnyChangeOnMethodInvocation(AssertInvocationAnalysisResult analysisResult) {

		if (analysisResult.getOriginalInvocation()
			.getExpression() != null) {
			return true;
		}
		boolean isNewInvocationQualified = !analysisResult.isNewInvocationWithoutQualifier();
		if (isNewInvocationQualified) {
			return true;
		}
		if (analysisResult.isNameChangedToAssertArrayEquals()) {
			return true;
		}
		return analysisResult.getMessageAsFirstArgument()
			.isPresent();
	}

	private AssertTransformationData findInvocationReplacementData(
			AssertInvocationAnalysisResult analysisResult) {

		MethodInvocation originalInvocation = analysisResult.getOriginalInvocation();
		String newMethodName = getNewMethodName(analysisResult);
		Supplier<MethodInvocation> newMethodInvocationSupplier;
		if (analysisResult.isNewInvocationWithoutQualifier()) {
			newMethodInvocationSupplier = () -> this.createNewInvocationWithoutQualifier(newMethodName);
		} else {
			newMethodInvocationSupplier = () -> this.createNewInvocationWithAssertionsQualifier(originalInvocation,
					newMethodName);
		}

		List<Expression> invocationArguments = ASTNodeUtil.convertToTypedList(originalInvocation.arguments(),
				Expression.class);
		Expression assertionMessageAsFirstArgument = analysisResult.getMessageAsFirstArgument()
			.orElse(null);
		Supplier<List<Expression>> newArgumentsSupplier;
		if (assertionMessageAsFirstArgument != null) {
			invocationArguments.remove(assertionMessageAsFirstArgument);
			newArgumentsSupplier = () -> this.createAssertionMethodArguments(invocationArguments,
					assertionMessageAsFirstArgument);
		} else {
			newArgumentsSupplier = () -> this.createAssertionMethodArguments(invocationArguments);
		}
		return new AssertTransformationData(originalInvocation, newMethodInvocationSupplier, newArgumentsSupplier);
	}

	private boolean isSupportedJUnit4Method(IMethodBinding methodBinding) {
		if (methodBinding.getDeclaringClass()
			.getQualifiedName()
			.equals("org.junit.Assert")) { //$NON-NLS-1$
			String methodName = methodBinding.getName();
			return !methodName.equals("assertThat") //$NON-NLS-1$
					&& !methodName.equals("assertThrows"); //$NON-NLS-1$
		}
		return false;
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
			return isSupportedJUnit4Method(methodBinding)
					&& !simpleNamesOfStaticAssertMethodImportsToKeep.contains(methodBinding.getName());
		}
		return false;
	}

	private boolean isInvocationWithinJUnitJupiterTest(MethodInvocation methodInvocation) {
		ASTNode parent = methodInvocation.getParent();
		while (parent != null) {
			if (parent.getNodeType() == ASTNode.METHOD_DECLARATION) {
				MethodDeclaration methodDeclaration = (MethodDeclaration) parent;
				if (methodDeclaration.getLocationInParent() != TypeDeclaration.BODY_DECLARATIONS_PROPERTY) {
					return false;
				}
				TypeDeclaration typeDeclaration = (TypeDeclaration) methodDeclaration.getParent();
				if (typeDeclaration.isLocalTypeDeclaration()) {
					return false;
				}
				return ASTNodeUtil.convertToTypedList(methodDeclaration.modifiers(), Annotation.class)
					.stream()
					.map(Annotation::resolveAnnotationBinding)
					.map(IAnnotationBinding::getAnnotationType)
					.map(ITypeBinding::getQualifiedName)
					.anyMatch(ORG_JUNIT_JUPITER_API_TEST::equals);
			}
			if (parent.getNodeType() == ASTNode.LAMBDA_EXPRESSION) {
				return false;
			}
			parent = parent.getParent();
		}
		return false;
	}

	private MethodInvocation createNewInvocationWithoutQualifier(String newMethodName) {
		AST ast = astRewrite.getAST();
		MethodInvocation newInvocation = ast.newMethodInvocation();
		newInvocation.setName(ast.newSimpleName(newMethodName));
		return newInvocation;
	}

	private MethodInvocation createNewInvocationWithAssertionsQualifier(MethodInvocation contextForImport,
			String newMethodName) {
		MethodInvocation newInvocation = createNewInvocationWithoutQualifier(newMethodName);
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
		messageSupplierLambdaExpression.setBody((Expression) astRewrite.createCopyTarget(assertionMessage));
		assertionMethodArguments.add(messageSupplierLambdaExpression);
		return assertionMethodArguments;
	}

	private void transform(List<ImportDeclaration> assertMethodStaticImportsToRemove,
			Set<String> assertionMethodSimpleNamesForNewStaticImports,
			List<AssertTransformationData> assertTransformationDataList) {

		assertMethodStaticImportsToRemove.forEach(importDeclaration -> astRewrite.remove(importDeclaration, null));
		AST ast = astRewrite.getAST();
		ListRewrite newImportsListRewrite = astRewrite.getListRewrite(getCompilationUnit(),
				CompilationUnit.IMPORTS_PROPERTY);
		assertionMethodSimpleNamesForNewStaticImports
			.forEach(methodName -> {
				ImportDeclaration newImportDeclaration = ast.newImportDeclaration();
				String qualifiedName = ORG_JUNIT_JUPITER_API_ASSERTIONS_PREFIX;
				newImportDeclaration.setName(ast.newName(qualifiedName + methodName));
				newImportDeclaration.setStatic(true);
				newImportsListRewrite.insertLast(newImportDeclaration, null);
			});
		assertTransformationDataList.forEach(this::replaceAssertMethodInvocation);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void replaceAssertMethodInvocation(AssertTransformationData invocationReplacementData) {
		MethodInvocation methodInvocationToReplace = invocationReplacementData.getMethodInvocationToReplace();
		MethodInvocation methodInvocationReplacement = invocationReplacementData.createAssertionMethodInvocation();
		List newArguments = methodInvocationReplacement.arguments();
		invocationReplacementData.createNewArgumentList()
			.forEach(newArguments::add);
		astRewrite.replace(methodInvocationToReplace, methodInvocationReplacement, null);
	}
}
