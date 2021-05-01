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


		List<JUnit4MethodInvocationAnalysisResult> allJUnit4InvocationAnalysisResults;
		List<Type> throwingRunnableTypesToReplace = new ArrayList<>();
		if (classDeclaringJUnit4Method.equals("org.junit.Assert")) { //$NON-NLS-1$
			JUnit4AssertionAnalyzer assertionAnalyzer =  new JUnit4AssertionAnalyzer(
					compilationUnit, this::isSupportedJUnit4Method);
			allJUnit4InvocationAnalysisResults = new ArrayList<>();
			assertionAnalyzer.collectJUnit4AssertionAnalysisResults(compilationUnit).forEach(result -> {
				allJUnit4InvocationAnalysisResults.add(result);
				Type throwingRunnableTypeToReplace = result.getThrowingRunnableTypeToReplace().orElse(null);
				if(result.isTransformableInvocation() &&  throwingRunnableTypeToReplace != null) {
					throwingRunnableTypesToReplace.add(throwingRunnableTypeToReplace);
				}
			});
		} else {
			JUnit4MethodInvocationAnalyzer invocationAnalyzer = new JUnit4MethodInvocationAnalyzer(
					compilationUnit, this::isSupportedJUnit4Method);
			allJUnit4InvocationAnalysisResults = invocationAnalyzer
					.collectJUnit4AssertionAnalysisResults(compilationUnit);
			
		}

		StaticMethodImportsToRemoveHelper staticMethodImportsToRemoveHelper = new StaticMethodImportsToRemoveHelper(
				compilationUnit, this::isSupportedJUnit4Method, allJUnit4InvocationAnalysisResults);

		List<JUnit4MethodInvocationAnalysisResult> transformableJUnit4InvocationAnalysisResults = allJUnit4InvocationAnalysisResults
			.stream()
			.filter(JUnit4MethodInvocationAnalysisResult::isTransformableInvocation)
			.collect(Collectors.toList());

		List<ImportDeclaration> staticAssertMethodImportsToRemove = staticMethodImportsToRemoveHelper
			.getStaticMethodImportsToRemove();

		Set<String> supportedNewStaticMethodImports = findSupportedStaticImports(staticMethodImportsToRemoveHelper,
				transformableJUnit4InvocationAnalysisResults);

		List<JUnit4MethodInvocationReplacementData> jUnit4AssertTransformationDataList = allJUnit4InvocationAnalysisResults
			.stream()
			.map(data -> this.findTransformationData(data, supportedNewStaticMethodImports))
			.filter(Optional::isPresent)
			.map(Optional::get)
			.collect(Collectors.toList());

		Set<String> newStaticAssertionMethodImports = jUnit4AssertTransformationDataList.stream()
			.map(JUnit4MethodInvocationReplacementData::getStaticMethodImport)
			.filter(Optional::isPresent)
			.map(Optional::get)
			.collect(Collectors.toSet());

		transform(staticAssertMethodImportsToRemove, newStaticAssertionMethodImports, throwingRunnableTypesToReplace,
				jUnit4AssertTransformationDataList);

		return false;
	}

	private Set<String> findSupportedStaticImports(
			StaticMethodImportsToRemoveHelper staticMethodImportsToRemoveHelper,
			List<JUnit4MethodInvocationAnalysisResult> transformableJUnit4InvocationAnalysisResults) {

		Set<String> supportedNewMethodNames = new HashSet<>();
		transformableJUnit4InvocationAnalysisResults.forEach(data -> {
			supportedNewMethodNames.add(data.getMethodBinding()
				.getName());
		});

		if (classDeclaringJUnit4Method.equals("org.junit.Assert")) { //$NON-NLS-1$
			supportedNewMethodNames.add("assertArrayEquals"); //$NON-NLS-1$
			// always supported because "assertEquals" may change to
			// "assertArrayEquals"
		}

		Set<String> supportedNewStaticMethodImports = new HashSet<>();
		String newMethodFullyQualifiedNamePrefix = classDeclaringJUnitJupiterMethod + "."; //$NON-NLS-1$
		supportedNewMethodNames.forEach(supportedNewMethodName -> {
			String supportedNewMethodFullyQualifiedName = newMethodFullyQualifiedNamePrefix + supportedNewMethodName;
			if (staticMethodImportsToRemoveHelper.isSimpleNameOfStaticMethodImportToRemove(supportedNewMethodName)
					|| canAddStaticAssertionsMethodImport(supportedNewMethodFullyQualifiedName)) {
				supportedNewStaticMethodImports.add(supportedNewMethodFullyQualifiedName);
			}
		});

		return supportedNewStaticMethodImports;
	}

	private boolean canAddStaticAssertionsMethodImport(String fullyQualifiedAssertionsMethodName) {
		verifyStaticMethodImport(getCompilationUnit(), fullyQualifiedAssertionsMethodName);
		return canAddStaticMethodImport(fullyQualifiedAssertionsMethodName);
	}

	private Optional<JUnit4MethodInvocationReplacementData> findTransformationData(
			JUnit4MethodInvocationAnalysisResult invocationData,
			Set<String> supportedNewStaticMethodImports) {

		if (!invocationData.isTransformableInvocation()) {
			return Optional.empty();
		}
		MethodInvocation methodInvocation = invocationData.getMethodInvocation();
		IMethodBinding originalMethodBinding = invocationData.getMethodBinding();
		String originalMethodName = originalMethodBinding.getName();

		ITypeBinding[] declaredParameterTypes = originalMethodBinding
			.getMethodDeclaration()
			.getParameterTypes();
		String newMethodName;
		if (isDeprecatedAssertEqualsComparingObjectArrays(originalMethodName, declaredParameterTypes)) {
			newMethodName = "assertArrayEquals"; //$NON-NLS-1$
		} else {
			newMethodName = originalMethodName;
		}

		List<Expression> originalArguments = ASTNodeUtil.convertToTypedList(methodInvocation.arguments(),
				Expression.class);

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

		String newMethodStaticImport = classDeclaringJUnitJupiterMethod + "." + newMethodName; //$NON-NLS-1$
		if (supportedNewStaticMethodImports.contains(newMethodStaticImport)) {
			if (methodInvocation.getExpression() == null
					&& newArguments == originalArguments
					&& newMethodName.equals(originalMethodName)) {
				return Optional.of(new JUnit4MethodInvocationReplacementData(methodInvocation, newMethodStaticImport));
			}
			return Optional.of(new JUnit4MethodInvocationReplacementData(methodInvocation,
					() -> createNewInvocationWithoutQualifier(newMethodName, newArguments), newMethodStaticImport));
		}

		Supplier<MethodInvocation> newMethodInvocationSupplier = () -> createNewInvocationWithAssertionsQualifier(
				methodInvocation,
				newMethodName, newArguments);

		return Optional.of(new JUnit4MethodInvocationReplacementData(methodInvocation, newMethodInvocationSupplier));
	}

	private boolean isDeprecatedAssertEqualsComparingObjectArrays(String methodName,
			ITypeBinding[] declaredParameterTypes) {
		if (!methodName.equals("assertEquals")) { //$NON-NLS-1$
			return false;
		}

		if (declaredParameterTypes.length == 2) {
			return isParameterTypeObjectArray(declaredParameterTypes[0])
					&& isParameterTypeObjectArray(declaredParameterTypes[1]);
		}

		if (declaredParameterTypes.length == 3) {
			return isParameterTypeString(declaredParameterTypes[0])
					&& isParameterTypeObjectArray(declaredParameterTypes[1])
					&& isParameterTypeObjectArray(declaredParameterTypes[2]);
		}
		return false;
	}

	private boolean isParameterTypeString(ITypeBinding parameterType) {
		return isContentOfType(parameterType, "java.lang.String"); //$NON-NLS-1$
	}

	private boolean isParameterTypeObjectArray(ITypeBinding parameterType) {
		if (parameterType.isArray() && parameterType.getDimensions() == 1) {
			return isContentOfType(parameterType.getComponentType(), "java.lang.Object"); //$NON-NLS-1$
		}
		return false;
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
				MethodInvocation methodInvocationReplacement = data.createMethodInvocationReplacement()
					.orElse(null);
				if (methodInvocationReplacement != null) {
					MethodInvocation methodInvocationToReplace = data.getOriginalMethodInvocation();
					astRewrite.replace(methodInvocationToReplace, methodInvocationReplacement, null);
					onRewrite();
				}
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