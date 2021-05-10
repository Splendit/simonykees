package eu.jsparrow.core.visitor.junit.jupiter.assertions;

import static eu.jsparrow.core.visitor.junit.jupiter.assertions.JUnit4MethodInvocationAnalyzer.isDeprecatedAssertEqualsComparingObjectArrays;
import static eu.jsparrow.core.visitor.junit.jupiter.assertions.JUnit4MethodInvocationAnalyzer.isParameterTypeString;
import static eu.jsparrow.rules.common.util.ClassRelationUtil.isContentOfType;

import java.util.ArrayList;
import java.util.Collections;
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

import eu.jsparrow.rules.common.util.ASTNodeUtil;

/**
 * Replaces invocations of methods of the JUnit-4-class {@code org.junit.Assert}
 * by invocations of the corresponding methods of the JUnit-Jupiter-class
 * {@code org.junit.jupiter.api.Assertions}.
 * 
 * @since 3.28.0
 * 
 */
public class ReplaceJUnit4AssertionsWithJupiterASTVisitor extends AbstractReplaceJUnit4MethodInvocationsASTVisitor {

	private static final String ORG_JUNIT_JUPITER_API_FUNCTION_EXECUTABLE = "org.junit.jupiter.api.function.Executable"; //$NON-NLS-1$

	private final Set<String> potentialMethodNameReplacements;

	public ReplaceJUnit4AssertionsWithJupiterASTVisitor() {
		super(ORG_J_UNIT_JUPITER_API_ASSERTIONS);
		Set<String> tmp = new HashSet<>();
		tmp.add("assertArrayEquals"); //$NON-NLS-1$
		potentialMethodNameReplacements = Collections.unmodifiableSet(tmp);

	}

	@Override
	public boolean visit(CompilationUnit compilationUnit) {

		super.visit(compilationUnit);

		verifyImports(compilationUnit);

		List<JUnit4MethodInvocationAnalysisResult> allSupportedJUnit4InvocationDataList = collectJUnit4MethodInvocationAnalysisResult(
				compilationUnit);

		List<Type> throwingRunnableTypesToReplace = allSupportedJUnit4InvocationDataList.stream()
			.map(JUnit4MethodInvocationAnalysisResult::getTypeOfThrowingRunnableToReplace)
			.filter(Optional::isPresent)
			.map(Optional::get)
			.collect(Collectors.toList());

		List<ImportDeclaration> staticMethodImportsToRemove = collectStaticMethodImportsToRemove(compilationUnit,
				allSupportedJUnit4InvocationDataList);

		Set<String> supportedNewStaticMethodImports = findSupportedStaticImports(staticMethodImportsToRemove,
				allSupportedJUnit4InvocationDataList);

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

	protected void verifyImports(CompilationUnit compilationUnit) {
		verifyImport(compilationUnit, classDeclaringJUnit4MethodReplacement);
		verifyImport(compilationUnit, ORG_JUNIT_JUPITER_API_FUNCTION_EXECUTABLE);
	}

	@Override
	protected JUnit4MethodInvocationAnalysisResult findAnalysisResult(JUnit4MethodInvocationAnalyzer analyzer,
			MethodInvocation methodInvocation, IMethodBinding methodBinding, List<Expression> arguments) {
		String methodIdentifier = methodInvocation.getName()
			.getIdentifier();
		if (methodIdentifier.equals("assertThrows")) { //$NON-NLS-1$
			return analyzer.createAssertThrowsInvocationData(methodInvocation, methodBinding, arguments);
		}
		return new JUnit4MethodInvocationAnalysisResult(methodInvocation, methodBinding, arguments,
				analyzer.supportTransformation(methodInvocation, arguments));
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
				return Optional.of(new JUnit4MethodInvocationReplacementData(invocationData, newMethodStaticImport));
			}
			Supplier<List<Expression>> newArgumentsSupplier = () -> createNewMethodArguments(newArguments);
			return Optional.of(new JUnit4MethodInvocationReplacementData(invocationData,
					() -> createNewInvocationWithoutQualifier(newMethodName, newArgumentsSupplier),
					newMethodStaticImport));
		}
		Supplier<List<Expression>> newArgumentsSupplier = () -> createNewMethodArguments(newArguments);
		Supplier<MethodInvocation> newMethodInvocationSupplier = () -> createNewInvocationWithQualifier(
				methodInvocation,
				newMethodName, newArgumentsSupplier);

		return Optional.of(new JUnit4MethodInvocationReplacementData(invocationData, newMethodInvocationSupplier));
	}

	@Override
	protected boolean isSupportedJUnit4Method(IMethodBinding methodBinding) {
		return isContentOfType(methodBinding.getDeclaringClass(), "org.junit.Assert") //$NON-NLS-1$
				&& !methodBinding.getName()
					.equals("assertThat"); //$NON-NLS-1$
	}

	@Override
	protected Set<String> getSupportedMethodNameReplacements() {
		return potentialMethodNameReplacements;
	}
}