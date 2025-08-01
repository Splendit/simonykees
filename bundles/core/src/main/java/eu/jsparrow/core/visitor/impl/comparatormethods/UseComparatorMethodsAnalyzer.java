package eu.jsparrow.core.visitor.impl.comparatormethods;

import static eu.jsparrow.core.visitor.impl.comparatormethods.UseComparatorMethodsASTVisitor.JAVA_LANG_COMPARABLE;
import static eu.jsparrow.core.visitor.impl.comparatormethods.UseComparatorMethodsASTVisitor.JAVA_UTIL_COMPARATOR;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.LambdaExpression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.VariableDeclaration;

import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.util.ClassRelationUtil;

/**
 * Helper class for {@link UseComparatorMethodsASTVisitor} which analyzes a
 * lambda expression representing a comparator and provides an
 * {@link java.util.Optional} storing an instance of
 * {@link LambdaAnalysisResult}.<br>
 * The transformation can only be carried out if the {@link java.util.Optional}
 * is not empty.
 * 
 * @since 3.23.0
 */
public class UseComparatorMethodsAnalyzer {

	/**
	 * 
	 * @param lambda
	 *            representing the {@link java.util.Comparator}.
	 * @return an {@code Optional<LambdaAnalysisResult>} indicating whether or
	 *         not the transformation can be carried out.
	 */
	Optional<LambdaAnalysisResult> analyze(LambdaExpression lambda) {
		ITypeBinding lambdaTypeBinding = lambda.resolveTypeBinding();
		if (!ClassRelationUtil.isContentOfType(lambdaTypeBinding,
				JAVA_UTIL_COMPARATOR)) {
			return Optional.empty();
		}

		CastExpression parentCastExpression = null;
		ASTNode outermostExpression = ASTNodeUtil.getOutermostParenthesizedExpression(lambda);
		if (outermostExpression.getLocationInParent() == CastExpression.EXPRESSION_PROPERTY) {
			parentCastExpression = (CastExpression) outermostExpression.getParent();
			if (isCastExpressionWithJokerTypeArgument(parentCastExpression)) {
				return Optional.empty();
			}
		}

		List<VariableDeclaration> lambdaParameters = ASTNodeUtil.convertToTypedList(lambda.parameters(),
				VariableDeclaration.class);

		ITypeBinding lambdaParameterType = lambdaParameters.get(0)
			.resolveBinding()
			.getType();

		if (lambdaParameterType.isWildcardType()) {
			return Optional.empty();
		}

		VariableDeclaration lambdaParameterLeftHS = lambdaParameters.get(0);
		VariableDeclaration lambdaParameterRightHS = lambdaParameters.get(1);

		if (lambda.getBody()
			.getNodeType() != ASTNode.METHOD_INVOCATION) {
			return Optional.empty();
		}
		MethodInvocation lambdaBody = (MethodInvocation) lambda.getBody();
		if (!isCompareToMethodOfComparator(lambdaBody)) {
			return Optional.empty();
		}

		Optional<LambdaAnalysisResult> lambdaAnalysisResult = analyzeLambdaBody(lambdaParameterLeftHS,
				lambdaParameterRightHS, lambdaBody);
		if (lambdaAnalysisResult.isPresent()) {
			LambdaAnalysisResult result = lambdaAnalysisResult.get();
			result.setParentCastExpression(parentCastExpression);
		}
		return lambdaAnalysisResult;
	}

	Optional<LambdaAnalysisResult> analyzeLambdaBody(VariableDeclaration lambdaParameterLeftHS,
			VariableDeclaration lambdaParameterRightHS, MethodInvocation lambdaBody) {
		Expression bodyInvocationExpression = lambdaBody.getExpression();
		if (bodyInvocationExpression == null) {
			return Optional.empty();
		}
		List<Expression> bodyInvocationArguments = ASTNodeUtil.convertToTypedList(lambdaBody.arguments(),
				Expression.class);

		Expression bodyInvocationArgument = bodyInvocationArguments.get(0);
		if (bodyInvocationExpression.getNodeType() == ASTNode.SIMPLE_NAME
				&& bodyInvocationArgument.getNodeType() == ASTNode.SIMPLE_NAME) {

			String nameLeft = ((SimpleName) bodyInvocationExpression).getIdentifier();
			String nameRight = ((SimpleName) bodyInvocationArgument).getIdentifier();
			return analyzeSimple(lambdaParameterLeftHS, lambdaParameterRightHS, nameLeft, nameRight);

		} else if (bodyInvocationExpression.getNodeType() == ASTNode.METHOD_INVOCATION
				&& bodyInvocationArgument.getNodeType() == ASTNode.METHOD_INVOCATION) {
			MethodInvocation invocationLeft = (MethodInvocation) bodyInvocationExpression;
			MethodInvocation invocationRight = (MethodInvocation) bodyInvocationArgument;
			return analyzeWithComparisonKeyMethod(lambdaParameterLeftHS, lambdaParameterRightHS, invocationLeft,
					invocationRight);

		}
		return Optional.empty();
	}

	private Optional<LambdaAnalysisResult> analyzeSimple(VariableDeclaration parameterFirst,
			VariableDeclaration parameterSecond, String nameFirst,
			String nameSecond) {
		String parameterNameFirst = parameterFirst.getName()
			.getIdentifier();
		String parameterNameSecond = parameterSecond.getName()
			.getIdentifier();

		if (parameterNameFirst.equals(nameFirst) && parameterNameSecond.equals(nameSecond)) {
			return Optional.of(new LambdaAnalysisResult(parameterFirst, false));
		}
		if (parameterNameFirst.equals(nameSecond) && parameterNameSecond.equals(nameFirst)) {
			return Optional.of(new LambdaAnalysisResult(parameterFirst, true));
		}
		return Optional.empty();
	}

	private boolean checkComparisonKeyMethods(MethodInvocation invocationFirst, MethodInvocation invocationSecond) {
		String methodNameFirst = invocationFirst.getName()
			.getIdentifier();
		String methodNameSecond = invocationSecond.getName()
			.getIdentifier();

		if (!methodNameFirst.equals(methodNameSecond)) {
			return false;
		}
		return invocationFirst.resolveMethodBinding()
			.getParameterTypes().length == 0
				&& invocationSecond.resolveMethodBinding()
					.getParameterTypes().length == 0;
	}

	private Optional<LambdaAnalysisResult> analyzeWithComparisonKeyMethod(VariableDeclaration parameterFirst,
			VariableDeclaration parameterSecond,
			MethodInvocation invocationFirst, MethodInvocation invocationSecond) {

		if (!checkComparisonKeyMethods(invocationFirst, invocationSecond)) {
			return Optional.empty();
		}

		Expression invocationExpression1st = invocationFirst.getExpression();
		if (invocationExpression1st == null || invocationExpression1st.getNodeType() != ASTNode.SIMPLE_NAME) {
			return Optional.empty();
		}

		Expression invocationExpression2nd = invocationSecond.getExpression();
		if (invocationExpression2nd == null
				|| invocationExpression2nd.getNodeType() != ASTNode.SIMPLE_NAME) {
			return Optional.empty();
		}

		String invocationExpressionNameFirst = ((SimpleName) invocationExpression1st).getIdentifier();
		String invocationExpressionNameSecond = ((SimpleName) invocationExpression2nd).getIdentifier();

		String parameterNameFirst = parameterFirst.getName()
			.getIdentifier();
		String parameterNameSecond = parameterSecond.getName()
			.getIdentifier();

		IMethodBinding methodBinding = invocationFirst.resolveMethodBinding();
		if (methodBinding == null) {
			return Optional.empty();
		}
		if (parameterNameFirst.equals(invocationExpressionNameFirst)
				&& parameterNameSecond.equals(invocationExpressionNameSecond)) {
			return Optional.of(new LambdaAnalysisResult(parameterFirst, methodBinding, false));
		}
		if (parameterNameFirst.equals(invocationExpressionNameSecond)
				&& parameterNameSecond.equals(invocationExpressionNameFirst)) {
			return Optional.of(new LambdaAnalysisResult(parameterFirst, methodBinding, true));
		}
		return Optional.empty();
	}

	private boolean isCompareToMethodOfComparator(MethodInvocation methodInvocation) {
		IMethodBinding methodBinding = methodInvocation.resolveMethodBinding();
		if (methodBinding == null) {
			return false;
		}

		if (!methodBinding.getName()
			.equals("compareTo")) { //$NON-NLS-1$
			return false;
		}

		ITypeBinding[] parameterTypes = methodBinding.getParameterTypes();
		if (parameterTypes.length != 1) {
			return false;
		}

		ITypeBinding declaringClass = methodBinding.getDeclaringClass();
		if (!isComparable(declaringClass)) {
			return false;
		}

		ITypeBinding parameterType = parameterTypes[0];
		if (parameterType.isCapture()) {
			ITypeBinding wildcard = parameterType.getWildcard();
			parameterType = wildcard.getBound();
		}
		return isComparable(parameterType);
	}

	private boolean isComparable(ITypeBinding typeBinding) {
		boolean isComparable = ClassRelationUtil.isContentOfType(typeBinding, JAVA_LANG_COMPARABLE);
		boolean isInheritingComparable = ClassRelationUtil.isInheritingContentOfTypes(typeBinding,
				Collections.singletonList(JAVA_LANG_COMPARABLE));

		return isComparable || isInheritingComparable;
	}

	private boolean isCastExpressionWithJokerTypeArgument(CastExpression castExpression) {
		Type castExpressionType = castExpression.getType();
		if (castExpressionType.isParameterizedType()) {
			ParameterizedType parametrizedType = (ParameterizedType) castExpressionType;
			List<Type> castExpressionTypeArguments = ASTNodeUtil
				.convertToTypedList(parametrizedType.typeArguments(), Type.class);
			if (castExpressionTypeArguments.size() == 1) {
				return castExpressionTypeArguments.get(0)
					.isWildcardType();
			}
		}
		return false;
	}
}
