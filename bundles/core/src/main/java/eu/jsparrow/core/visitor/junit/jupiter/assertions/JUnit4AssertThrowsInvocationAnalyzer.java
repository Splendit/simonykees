package eu.jsparrow.core.visitor.junit.jupiter.assertions;

import static eu.jsparrow.rules.common.util.ClassRelationUtil.isContentOfType;

import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;

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
public class JUnit4AssertThrowsInvocationAnalyzer extends JUnit4AssertMethodInvocationAnalyzer {

	JUnit4AssertThrowsInvocationAnalyzer(CompilationUnit compilationUnit) {
		super(compilationUnit);
	}

	@Override
	boolean isSupportedJUnit4AssertMethod(IMethodBinding methodBinding) {
		if (isContentOfType(methodBinding.getDeclaringClass(), "org.junit.Assert")) { //$NON-NLS-1$
			return methodBinding.getName()
				.equals("assertThrows"); //$NON-NLS-1$
		}
		return false;
	}

	@Override
	boolean isTransformableInvocation(MethodInvocation methodInvocation) {
		if (!super.isTransformableInvocation(methodInvocation)) {
			return false;
		}

		List<Expression> arguments = ASTNodeUtil.convertToTypedList(methodInvocation.arguments(), Expression.class);
		int throwingRunnableArgumentIndex = arguments.size() - 1;
		Expression throwingRunnableArgument = arguments.get(throwingRunnableArgumentIndex);
		return throwingRunnableArgument.getNodeType() == ASTNode.LAMBDA_EXPRESSION;
	}
}