package eu.jsparrow.core.visitor.junit.jupiter.assertions;

import static eu.jsparrow.rules.common.util.ClassRelationUtil.isContentOfType;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IMethodBinding;
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
public class ReplaceJUnit4AssumptionsWithJupiterASTVisitor extends AbstractReplaceJUnit4InvocationsASTVisitor {

	JUnitJupiterTestMethodsStore jUnitJupiterTestMethodsStore = new JUnitJupiterTestMethodsStore();

	public ReplaceJUnit4AssumptionsWithJupiterASTVisitor() {
		super(ORG_J_UNIT_JUPITER_API_ASSUMPTIONS);
	}

	@Override
	public boolean visit(CompilationUnit compilationUnit) {
		jUnitJupiterTestMethodsStore.collectJUnitJupiterTestMethods(compilationUnit);
		return super.visit(compilationUnit);
	}

	@Override
	protected Optional<JUnit4InvocationReplacementAnalysis> findAnalysisResult(MethodInvocation methodInvocation,
			IMethodBinding methodBinding, List<Expression> arguments) {

		if (!jUnitJupiterTestMethodsStore.isSurroundedWithJUnitJupiterTest(methodInvocation)) {
			return Optional.empty();
		}
		JUnit4InvocationReplacementAnalysis invocationAnalyzer = new JUnit4InvocationReplacementAnalysis(
				methodInvocation, methodBinding, arguments);
		invocationAnalyzer.analyzeAssumptionToJupiter();
		return Optional.of(invocationAnalyzer);
	}

	@Override
	protected JUnit4InvocationReplacementData createTransformationData(
			JUnit4InvocationReplacementAnalysis invocationData,
			Set<String> supportedNewStaticMethodImports) {

		MethodInvocation methodInvocation = invocationData.getMethodInvocation();

		List<Expression> originalArguments = ASTNodeUtil.convertToTypedList(methodInvocation.arguments(),
				Expression.class);

		Expression messageMovingToLastPosition = invocationData.getMessageMovedToLastPosition()
			.orElse(null);

		List<Expression> newArguments;
		if (messageMovingToLastPosition != null) {
			newArguments = new ArrayList<>(originalArguments);
			newArguments.remove(messageMovingToLastPosition);
			newArguments.add(messageMovingToLastPosition);
		} else {
			newArguments = originalArguments;
		}

		String originalMethodName = invocationData.getOriginalMethodName();
		String newMethodStaticImport = classDeclaringJUnit4MethodReplacement + "." + originalMethodName; //$NON-NLS-1$
		if (supportedNewStaticMethodImports.contains(newMethodStaticImport)) {
			if (methodInvocation.getExpression() == null
					&& newArguments == originalArguments) {
				return new JUnit4InvocationReplacementData(invocationData, newMethodStaticImport);
			}
			Supplier<List<Expression>> newArgumentsSupplier = () -> createNewMethodArguments(newArguments);
			return new JUnit4InvocationReplacementData(invocationData,
					() -> createNewInvocationWithoutQualifier(originalMethodName, newArgumentsSupplier),
					newMethodStaticImport);
		}
		Supplier<List<Expression>> newArgumentsSupplier = () -> createNewMethodArguments(newArguments);
		Supplier<MethodInvocation> newMethodInvocationSupplier = () -> createNewInvocationWithQualifier(
				methodInvocation,
				originalMethodName, newArgumentsSupplier);

		return new JUnit4InvocationReplacementData(invocationData, newMethodInvocationSupplier);
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
}