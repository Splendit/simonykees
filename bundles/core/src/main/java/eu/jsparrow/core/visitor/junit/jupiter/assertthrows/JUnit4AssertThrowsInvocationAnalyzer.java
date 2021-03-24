package eu.jsparrow.core.visitor.junit.jupiter.assertthrows;

import static eu.jsparrow.rules.common.util.ClassRelationUtil.isContentOfType;

import java.util.List;
import java.util.Optional;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;

import eu.jsparrow.core.visitor.junit.jupiter.common.JUnit4AssertMethodInvocationAnalysisResult;

import eu.jsparrow.core.visitor.junit.jupiter.common.AbstractJUnit4AssertionAnalyzer;
import eu.jsparrow.core.visitor.junit.jupiter.common.MethodInvocationInJUnitJupiterAnalyzer;
import eu.jsparrow.rules.common.util.ASTNodeUtil;

/**
 * Helper class analyzing a {@link MethodInvocation}-node . If the
 * {@link MethodInvocation} represents the invocation of
 * {@code org.junit.Assert.assertThrows}, then all necessary informations for a
 * possible transformation are collected in an instance of
 * {@link JUnit4AssertMethodInvocationAnalysisResult}.
 * 
 * @since 3.29.0
 *
 */
public class JUnit4AssertThrowsInvocationAnalyzer extends AbstractJUnit4AssertionAnalyzer {

	private final MethodInvocationInJUnitJupiterAnalyzer invocationInJUnitJupiterAnalyzer;

	JUnit4AssertThrowsInvocationAnalyzer(CompilationUnit compilationUnit) {
		invocationInJUnitJupiterAnalyzer = new MethodInvocationInJUnitJupiterAnalyzer(compilationUnit);
	}

	public Optional<JUnit4AssertMethodInvocationAnalysisResult> findAnalysisResult(
			MethodInvocation methodInvocation) {
		IMethodBinding methodBinding = methodInvocation.resolveMethodBinding();
		if (methodBinding == null) {
			return Optional.empty();
		}
		if (!isSupportedJUnit4AssertMethod(methodBinding)) {
			return Optional.empty();
		}

		ITypeBinding[] declaredParameterTypes = methodBinding.getMethodDeclaration()
			.getParameterTypes();
		boolean messageAsFirstParameter = declaredParameterTypes.length > 0
				&& isContentOfType(declaredParameterTypes[0], "java.lang.String"); //$NON-NLS-1$

		boolean transformableInvocation = isTransformableInvocation(methodInvocation);

		return Optional.of(new JUnit4AssertMethodInvocationAnalysisResult(methodInvocation, messageAsFirstParameter,
				transformableInvocation));
	}

	boolean isSupportedJUnit4AssertMethod(IMethodBinding methodBinding) {
		if (isContentOfType(methodBinding.getDeclaringClass(), "org.junit.Assert")) { //$NON-NLS-1$
			return methodBinding.getName()
				.equals("assertThrows"); //$NON-NLS-1$
		}
		return false;
	}

	private boolean isTransformableInvocation(MethodInvocation methodInvocation) {
		if (!invocationInJUnitJupiterAnalyzer.isWithinJUnitJupiterTest(methodInvocation)) {
			return false;
		}

		List<Expression> arguments = ASTNodeUtil.convertToTypedList(methodInvocation.arguments(), Expression.class);
		int throwingRunnableArgumentIndex = arguments.size() - 1;
		Expression throwingRunnableArgument = arguments.get(throwingRunnableArgumentIndex);
		if (throwingRunnableArgument.getNodeType() != ASTNode.LAMBDA_EXPRESSION) {
			return false;
		}

		return arguments
			.stream()
			.allMatch(this::isArgumentWithUnambiguousType);
	}
}