package eu.jsparrow.core.visitor.junit.jupiter.assertions;

import static eu.jsparrow.core.visitor.junit.jupiter.assertions.JUnit4InvocationReplacementAnalyzer.isDeprecatedAssertEqualsComparingObjectArrays;
import static eu.jsparrow.core.visitor.junit.jupiter.assertions.JUnit4InvocationReplacementAnalyzer.isParameterTypeString;
import static eu.jsparrow.rules.common.util.ClassRelationUtil.isContentOfType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.SimpleType;

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
	protected void verifyImports(CompilationUnit compilationUnit) {
		verifyImport(compilationUnit, classDeclaringJUnit4MethodReplacement);
		verifyImport(compilationUnit, ORG_JUNIT_JUPITER_API_FUNCTION_EXECUTABLE);
	}

	@Override
	protected void transform(List<ImportDeclaration> staticAssertMethodImportsToRemove,
			Set<String> newStaticAssertionMethodImports,
			List<JUnit4MethodInvocationReplacementData> jUnit4AssertTransformationDataList) {
		super.transform(staticAssertMethodImportsToRemove, newStaticAssertionMethodImports,
				jUnit4AssertTransformationDataList);

		AST ast = astRewrite.getAST();
		jUnit4AssertTransformationDataList.stream()
			.map(JUnit4MethodInvocationReplacementData::getTypeOfThrowingRunnableToReplace)
			.filter(Optional::isPresent)
			.map(Optional::get)
			.forEach(typeToReplace -> {
				Name executableTypeName = addImport(ORG_JUNIT_JUPITER_API_FUNCTION_EXECUTABLE, typeToReplace);
				SimpleType typeReplacement = ast.newSimpleType(executableTypeName);
				astRewrite.replace(typeToReplace, typeReplacement, null);
			});
	}

	@Override
	protected Optional<JUnit4MethodInvocationAnalysisResult> findAnalysisResult(MethodInvocation methodInvocation,
			IMethodBinding methodBinding, List<Expression> arguments) {
		
		JUnit4InvocationReplacementAnalyzer invocationAnalyzer = new JUnit4InvocationReplacementAnalyzer();
		if(invocationAnalyzer.analyzeAssertion(methodBinding, arguments)) {
			return Optional.of(new JUnit4MethodInvocationAnalysisResult(methodInvocation, methodBinding, arguments, invocationAnalyzer));
		}
		
		return Optional.empty();
	}

	@Override
	protected JUnit4MethodInvocationReplacementData createTransformationData(
			JUnit4MethodInvocationAnalysisResult invocationData,
			Set<String> supportedNewStaticMethodImports) {

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
				return new JUnit4MethodInvocationReplacementData(invocationData, newMethodStaticImport);
			}
			Supplier<List<Expression>> newArgumentsSupplier = () -> createNewMethodArguments(newArguments);
			return new JUnit4MethodInvocationReplacementData(invocationData,
					() -> createNewInvocationWithoutQualifier(newMethodName, newArgumentsSupplier),
					newMethodStaticImport);
		}
		Supplier<List<Expression>> newArgumentsSupplier = () -> createNewMethodArguments(newArguments);
		Supplier<MethodInvocation> newMethodInvocationSupplier = () -> createNewInvocationWithQualifier(
				methodInvocation,
				newMethodName, newArgumentsSupplier);

		return new JUnit4MethodInvocationReplacementData(invocationData, newMethodInvocationSupplier);
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