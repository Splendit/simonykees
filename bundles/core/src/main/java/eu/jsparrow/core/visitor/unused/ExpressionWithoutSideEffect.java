package eu.jsparrow.core.visitor.unused;

import java.util.List;
import java.util.Optional;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ArrayCreation;
import org.eclipse.jdt.core.dom.ArrayInitializer;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import eu.jsparrow.rules.common.util.ASTNodeUtil;

/**
 * Analyzes an expression to find out whether or not side effects can be
 * excluded. For example, the side effect of a method invocation can be the
 * change of the state of a mutable Object.
 * 
 * <ul>
 * <li>either be removed safely together with the fragment declaring the unused
 * field if it is the initializer of the unused field</li>
 * <li>or be removed safely together with an assignment to the unused field</li>
 * </ul>
 */
public class ExpressionWithoutSideEffect {

	private ExpressionWithoutSideEffect() {
		/*
		 * Private default constructor hiding implicit public one
		 */
	}

	static boolean hasNoInitializerWithSideEffect(VariableDeclarationFragment fragment) {
		Expression initializer = fragment.getInitializer();
		return initializer == null || isExpressionWithoutSideEffect(initializer);
	}

	static boolean isExpressionWithoutSideEffect(Expression expression) {
		int expressionNodeType = expression.getNodeType();

		if (expressionNodeType == ASTNode.CLASS_INSTANCE_CREATION) {
			return isClassInstanceCreationWithoutSideEffect((ClassInstanceCreation) expression);
		}

		if (expressionNodeType == ASTNode.METHOD_INVOCATION) {
			return isMethodInvocationWithoutSideEffect((MethodInvocation) expression);
		}

		if (expressionNodeType == ASTNode.ARRAY_CREATION) {
			return isArrayCreationWithoutSideEffect(((ArrayCreation) expression));
		}

		return isExpressionExcludingSideEffects(expression);

	}

	private static boolean isExpressionExcludingSideEffects(Expression expression) {

		int expressionNodeType = expression.getNodeType();

		if (expressionNodeType == ASTNode.FIELD_ACCESS) {
			FieldAccess fieldAccess = (FieldAccess) expression;
			return isExpressionExcludingSideEffects(fieldAccess.getExpression());
		}
		return expressionNodeType == ASTNode.NULL_LITERAL
				|| expressionNodeType == ASTNode.NUMBER_LITERAL
				|| expressionNodeType == ASTNode.STRING_LITERAL
				|| expressionNodeType == ASTNode.CHARACTER_LITERAL
				|| expressionNodeType == ASTNode.BOOLEAN_LITERAL
				|| expressionNodeType == ASTNode.TYPE_LITERAL
				|| expressionNodeType == ASTNode.SIMPLE_NAME
				|| expressionNodeType == ASTNode.QUALIFIED_NAME
				|| expressionNodeType == ASTNode.THIS_EXPRESSION
				|| expressionNodeType == ASTNode.SUPER_FIELD_ACCESS;

	}

	private static boolean isClassInstanceCreationWithoutSideEffect(ClassInstanceCreation classInstanceCreation) {
		if (classInstanceCreation.getExpression() != null) {
			return false;
		}
		ITypeBinding constructedType = classInstanceCreation.resolveTypeBinding();
		if (constructedType != null && !BindingWithoutSideEffect.isSupportedConstructorType(constructedType)) {
			return false;
		}
		return ASTNodeUtil.convertToTypedList(classInstanceCreation.arguments(),
				Expression.class)
			.stream()
			.allMatch(ExpressionWithoutSideEffect::isExpressionExcludingSideEffects);
	}

	private static boolean isMethodInvocationWithoutSideEffect(MethodInvocation methodInvocation) {
		Expression methodInvocationExpression = methodInvocation.getExpression();
		if (methodInvocationExpression != null && !isExpressionExcludingSideEffects(methodInvocationExpression)) {
			return false;
		}
		IMethodBinding methodBinding = methodInvocation.resolveMethodBinding();
		if (methodBinding != null || !BindingWithoutSideEffect.isSupportedMethod(methodBinding)) {
			return false;
		}
		return ASTNodeUtil.convertToTypedList(methodInvocation.arguments(), Expression.class)
			.stream()
			.allMatch(ExpressionWithoutSideEffect::isExpressionExcludingSideEffects);
	}

	private static boolean isArrayCreationWithoutSideEffect(ArrayCreation arrayCreation) {

		List<Expression> dimensions = ASTNodeUtil.convertToTypedList(arrayCreation.dimensions(), Expression.class);
		boolean dimensionswithoutSideEffect = dimensions.stream()
			.allMatch(ExpressionWithoutSideEffect::isExpressionExcludingSideEffects);
		if (dimensionswithoutSideEffect) {

			ArrayInitializer arrayInitializer = arrayCreation.getInitializer();
			if (arrayInitializer == null) {
				return true;
			}
			return ASTNodeUtil.convertToTypedList(arrayInitializer.expressions(),
					Expression.class)
				.stream()
				.allMatch(ExpressionWithoutSideEffect::isExpressionExcludingSideEffects);
		}
		return false;
	}
}
