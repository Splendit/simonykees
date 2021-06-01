package eu.jsparrow.core.visitor.junit.jupiter.assertions;

import static eu.jsparrow.rules.common.util.ClassRelationUtil.isContentOfType;

import java.util.List;
import java.util.Optional;

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

	public ReplaceJUnit4AssumptionsWithJupiterASTVisitor() {
		super(ORG_J_UNIT_JUPITER_API_ASSUMPTIONS);
	}
	
	@Override
	protected Optional<JUnit4InvocationReplacementAnalysis> findAnalysisResult(MethodInvocation methodInvocation,
			IMethodBinding methodBinding) {

		List<Expression> arguments = ASTNodeUtil.convertToTypedList(methodInvocation.arguments(),
				Expression.class);

		if (!arguments.stream()
			.allMatch(this::isArgumentWithExplicitType)) {
			return Optional.empty();
		}

		JUnitJupiterTestMethodsStore jUnitJupiterTestMethodsStore = new JUnitJupiterTestMethodsStore(getCompilationUnit());
		if (!jUnitJupiterTestMethodsStore.isSurroundedWithJUnitJupiterTest(methodInvocation)) {
			return Optional.empty();
		}

		JUnit4InvocationReplacementAnalysis analysisObject = new JUnit4InvocationReplacementAnalysis(
				methodInvocation, methodBinding, arguments);

		if (analysisObject.analyzeAssumptionToJupiter()) {
			return Optional.of(analysisObject);
		}
		return Optional.empty();

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