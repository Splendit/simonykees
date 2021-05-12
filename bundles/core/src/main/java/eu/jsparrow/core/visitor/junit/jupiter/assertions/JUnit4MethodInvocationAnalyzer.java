package eu.jsparrow.core.visitor.junit.jupiter.assertions;

import static eu.jsparrow.rules.common.util.ClassRelationUtil.isContentOfType;

import java.util.List;
import java.util.Optional;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;

/**
 * Helper class analyzing a {@link MethodInvocation}-node. If the
 * {@link MethodInvocation} represents the invocation of one of the supported
 * methods of the class {@code org.junit.Assert} or {@code org.junit.Assume},
 * then all necessary informations are stored in a corresponding wrapper object.
 * 
 * @since 3.28.0
 *
 */
class JUnit4MethodInvocationAnalyzer {

	Optional<JUnit4MethodInvocationAnalysisResult> analyzeAssumptionToHamcrest(MethodInvocation methodInvocation,
			IMethodBinding methodBinding, List<Expression> arguments) {

		String methodIdentifier = methodInvocation.getName()
			.getIdentifier();
		if (methodIdentifier.equals("assumeNotNull")) { //$NON-NLS-1$
			return createAssumeNotNullInvocationAnalysisResult(methodInvocation, methodBinding,
					arguments);
		}
		return Optional.of(new JUnit4MethodInvocationAnalysisResult(methodInvocation, methodBinding, arguments));
	}

	Optional<JUnit4MethodInvocationAnalysisResult> analyzeAssumptionToJupiter(MethodInvocation methodInvocation,
			IMethodBinding methodBinding, List<Expression> arguments) {

		return Optional.of(new JUnit4MethodInvocationAnalysisResult(methodInvocation, methodBinding, arguments));
	}

	private Optional<JUnit4MethodInvocationAnalysisResult> createAssumeNotNullInvocationAnalysisResult(
			MethodInvocation methodInvocation, IMethodBinding methodBinding, List<Expression> arguments) {

		if (arguments.size() == 1) {
			Expression onlyOneArgument = arguments.get(0);
			if (onlyOneArgument.getNodeType() == ASTNode.ARRAY_CREATION || !onlyOneArgument.resolveTypeBinding()
				.isArray()) {
				return Optional
					.of(new JUnit4MethodInvocationAnalysisResult(methodInvocation, methodBinding, arguments));
			}
			AssumeNotNullWithNullableArray assumptionThatEveryItemNotNull = findAssumptionThatEveryItemNotNull(
					methodInvocation, onlyOneArgument).orElse(null);
			if (assumptionThatEveryItemNotNull != null) {
				return Optional
					.of(new JUnit4MethodInvocationAnalysisResult(methodInvocation, methodBinding, arguments,
							assumptionThatEveryItemNotNull));
			}
			return Optional.empty();
		}
		return Optional
			.of(new JUnit4MethodInvocationAnalysisResult(methodInvocation, methodBinding, arguments));
	}

	private Optional<AssumeNotNullWithNullableArray> findAssumptionThatEveryItemNotNull(
			MethodInvocation methodInvocation, Expression arrayArgument) {
		if (methodInvocation.getLocationInParent() != ExpressionStatement.EXPRESSION_PROPERTY) {
			return Optional.empty();
		}

		ExpressionStatement methodInvocationStatement = (ExpressionStatement) methodInvocation.getParent();
		if (methodInvocationStatement.getLocationInParent() != Block.STATEMENTS_PROPERTY) {
			return Optional.empty();
		}

		Block block = (Block) methodInvocationStatement.getParent();
		return Optional
			.of(new AssumeNotNullWithNullableArray(arrayArgument, methodInvocationStatement, block));

	}

	boolean isArgumentWithUnambiguousType(Expression expression) {
		if (expression.getNodeType() == ASTNode.METHOD_INVOCATION) {
			MethodInvocation methodInvocation = (MethodInvocation) expression;
			IMethodBinding methodBinding = methodInvocation.resolveMethodBinding();
			return methodBinding != null && !(methodBinding.isParameterizedMethod() && methodInvocation.typeArguments()
				.isEmpty());
		}
		if (expression.getNodeType() == ASTNode.SUPER_METHOD_INVOCATION) {
			SuperMethodInvocation superMethodInvocation = (SuperMethodInvocation) expression;
			IMethodBinding superMethodBinding = superMethodInvocation.resolveMethodBinding();
			return superMethodBinding != null
					&& !(superMethodBinding.isParameterizedMethod() && superMethodInvocation.typeArguments()
						.isEmpty());
		}
		return true;
	}



	static boolean isParameterTypeString(ITypeBinding parameterType) {
		return isContentOfType(parameterType, "java.lang.String"); //$NON-NLS-1$
	}


}