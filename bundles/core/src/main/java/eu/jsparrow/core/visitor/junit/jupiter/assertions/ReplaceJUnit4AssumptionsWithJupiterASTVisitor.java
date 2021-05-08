package eu.jsparrow.core.visitor.junit.jupiter.assertions;

import static eu.jsparrow.core.visitor.junit.jupiter.assertions.JUnit4MethodInvocationAnalyzer.isDeprecatedAssertEqualsComparingObjectArrays;
import static eu.jsparrow.core.visitor.junit.jupiter.assertions.JUnit4MethodInvocationAnalyzer.isParameterTypeString;
import static eu.jsparrow.rules.common.util.ClassRelationUtil.isContentOfType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;

import eu.jsparrow.rules.common.util.ASTNodeUtil;

/**
 * Replaces the JUnit 4 method invocations {@code org.junit.Assume.assumeFalse}
 * and {@code org.junit.Assume.assumeTrue} by invocations of the corresponding
 * methods of the JUnit Jupiter class {@code org.junit.jupiter.api.Assumptions}.
 * 
 * @since 3.30.0
 * 
 */
public class ReplaceJUnit4AssumptionsWithJupiterASTVisitor extends AbstractReplaceJUnit4MethodInvocationsASTVisitor {

	public ReplaceJUnit4AssumptionsWithJupiterASTVisitor() {
		super(ORG_J_UNIT_JUPITER_API_ASSUMPTIONS);
	}

	@Override
	public boolean visit(CompilationUnit compilationUnit) {

		super.visit(compilationUnit);

		verifyImport(compilationUnit, classDeclaringJUnit4MethodReplacement);

		List<JUnit4MethodInvocationAnalysisResult> allSupportedJUnit4InvocationDataList = collectJUnit4MethodInvocationAnalysisResult(
				compilationUnit);

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

	@Override
	protected boolean isSupportedJUnit4Method(IMethodBinding methodBinding) {
		if (isContentOfType(methodBinding.getDeclaringClass(), "org.junit.Assume")) { //$NON-NLS-1$
			String methodName = methodBinding.getName();
			return methodName.equals("assumeFalse") || //$NON-NLS-1$
					methodName.equals("assumeTrue"); //$NON-NLS-1$
		}
		return false;
	}

	@Override
	protected Set<String> getSupportedMethodNameReplacements() {
		return Collections.emptySet();
	}
}