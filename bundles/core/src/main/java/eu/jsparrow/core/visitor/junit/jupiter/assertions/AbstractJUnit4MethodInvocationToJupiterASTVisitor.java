package eu.jsparrow.core.visitor.junit.jupiter.assertions;

import static eu.jsparrow.core.visitor.junit.jupiter.assertions.JUnit4MethodInvocationAnalyzer.isDeprecatedAssertEqualsComparingObjectArrays;
import static eu.jsparrow.core.visitor.junit.jupiter.assertions.JUnit4MethodInvocationAnalyzer.isParameterTypeString;

import java.util.ArrayList;
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

import eu.jsparrow.rules.common.util.ASTNodeUtil;

/**
 * This visitor replaces invocations of methods which are declared in a JUnit 4
 * class like {@code org.junit.Assert} or {@code org.junit.Assume} by
 * invocations of the corresponding JUnit Jupiter methods.
 * 
 * @since 3.30.0
 * 
 */
abstract class AbstractJUnit4MethodInvocationToJupiterASTVisitor
		extends AbstractReplaceJUnit4MethodInvocationsASTVisitor {
	private static final String ORG_JUNIT_JUPITER_API_FUNCTION_EXECUTABLE = "org.junit.jupiter.api.function.Executable"; //$NON-NLS-1$

	AbstractJUnit4MethodInvocationToJupiterASTVisitor(String classDeclaringJUnit4MethodReplacement) {
		super(classDeclaringJUnit4MethodReplacement);
	}

	@Override
	public boolean visit(CompilationUnit compilationUnit) {

		super.visit(compilationUnit);

		verifyImport(compilationUnit, classDeclaringJUnit4MethodReplacement);
		verifyImport(compilationUnit, ORG_JUNIT_JUPITER_API_FUNCTION_EXECUTABLE);

		JUnit4MethodInvocationAnalysisResultStore transformationDataStore = createTransformationDataStore(
				compilationUnit);
		List<JUnit4MethodInvocationAnalysisResult> allSupportedJUnit4InvocationDataList = new ArrayList<>();

		transformationDataStore.getMethodInvocationAnalysisResults()
			.forEach(allSupportedJUnit4InvocationDataList::add);

		List<JUnit4AssertThrowsInvocationAnalysisResult> assertThrowsInvocationAnalysisResults = transformationDataStore
			.getAssertThrowsInvocationAnalysisResults();
		assertThrowsInvocationAnalysisResults.stream()
			.map(JUnit4AssertThrowsInvocationAnalysisResult::getJUnit4InvocationData)
			.forEach(allSupportedJUnit4InvocationDataList::add);

		List<Type> throwingRunnableTypesToReplace = assertThrowsInvocationAnalysisResults.stream()
			.map(JUnit4AssertThrowsInvocationAnalysisResult::getTypeOfThrowingRunnableToReplace)
			.filter(Optional::isPresent)
			.map(Optional::get)
			.collect(Collectors.toList());

		List<ImportDeclaration> staticMethodImportsToRemove = collectStaticMethodImportsToRemove(compilationUnit,
				transformationDataStore);

		Set<String> supportedNewStaticMethodImports = findSupportedStaticImports(staticMethodImportsToRemove,
				transformationDataStore);

		List<JUnit4MethodInvocationReplacementData> jUnit4AssertTransformationDataList = allSupportedJUnit4InvocationDataList
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

		transform(staticMethodImportsToRemove, newStaticAssertionMethodImports, jUnit4AssertTransformationDataList);

		AST ast = astRewrite.getAST();
		throwingRunnableTypesToReplace.forEach(typeToReplace -> {
			Name executableTypeName = addImport(ORG_JUNIT_JUPITER_API_FUNCTION_EXECUTABLE, typeToReplace);
			SimpleType typeReplacement = ast.newSimpleType(executableTypeName);
			astRewrite.replace(typeToReplace, typeReplacement, null);
		});

		return false;
	}

	private Optional<JUnit4MethodInvocationReplacementData> findTransformationData(
			JUnit4MethodInvocationAnalysisResult invocationData,
			Set<String> supportedNewStaticMethodImports) {

		if (!invocationData.isTransformable()) {
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

		String newMethodStaticImport = classDeclaringJUnit4MethodReplacement + "." + newMethodName; //$NON-NLS-1$
		if (supportedNewStaticMethodImports.contains(newMethodStaticImport)) {
			if (methodInvocation.getExpression() == null
					&& newArguments == originalArguments
					&& newMethodName.equals(originalMethodName)) {
				return Optional.of(new JUnit4MethodInvocationReplacementData(methodInvocation, newMethodStaticImport));
			}
			Supplier<List<Expression>> newArgumentsSupplier = () -> createNewMethodArguments(newArguments);
			return Optional.of(new JUnit4MethodInvocationReplacementData(methodInvocation,
					() -> createNewInvocationWithoutQualifier(newMethodName, newArgumentsSupplier),
					newMethodStaticImport));
		}
		Supplier<List<Expression>> newArgumentsSupplier = () -> createNewMethodArguments(newArguments);
		Supplier<MethodInvocation> newMethodInvocationSupplier = () -> createNewInvocationWithQualifier(
				methodInvocation,
				newMethodName, newArgumentsSupplier);

		return Optional.of(new JUnit4MethodInvocationReplacementData(methodInvocation, newMethodInvocationSupplier));
	}
}