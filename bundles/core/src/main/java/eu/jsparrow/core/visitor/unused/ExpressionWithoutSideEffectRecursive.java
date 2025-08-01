package eu.jsparrow.core.visitor.unused;

import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ArrayAccess;
import org.eclipse.jdt.core.dom.ArrayCreation;
import org.eclipse.jdt.core.dom.ArrayInitializer;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.LambdaExpression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Statement;

import eu.jsparrow.rules.common.util.ASTNodeUtil;

/**
 * Analyzes an expression to find out whether side effects can be excluded.
 * <p>
 * One example for a side effects is be the change of state of a mutable
 * collection by calling an 'add' method.
 * <p>
 * Another example for a side effect is a change in the file system by calling a
 * method.
 * 
 * @since 4.8.0
 */
public class ExpressionWithoutSideEffectRecursive {

	private ExpressionWithoutSideEffectRecursive() {
		/*
		 * Private default constructor hiding implicit public one
		 */
	}

	static boolean isExpressionWithoutSideEffect(Expression expression) {
		int expressionNodeType = expression.getNodeType();

		if (expressionNodeType == ASTNode.CLASS_INSTANCE_CREATION) {
			return isClassInstanceCreationWithoutSideEffect((ClassInstanceCreation) expression);
		}
		if (expressionNodeType == ASTNode.METHOD_INVOCATION) {
			return isMethodInvocationWithoutSideEffect((MethodInvocation) expression);
		}
		if (expressionNodeType == ASTNode.FIELD_ACCESS) {
			return isExpressionWithoutSideEffect(((FieldAccess) expression).getExpression());
		}
		if (expressionNodeType == ASTNode.ARRAY_CREATION) {
			return isArrayCreationWithoutSideEffect((ArrayCreation) expression);
		}
		if (expressionNodeType == ASTNode.ARRAY_INITIALIZER) {
			return isArrayInitializerWithoutSideEffect((ArrayInitializer) expression);
		}
		if (expressionNodeType == ASTNode.ARRAY_ACCESS) {
			return isArrayAccessWithoutSideEffect((ArrayAccess) expression);
		}
		if (expressionNodeType == ASTNode.LAMBDA_EXPRESSION) {
			return isSimpleLambdaExpression((LambdaExpression) expression);
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
			.allMatch(ExpressionWithoutSideEffectRecursive::isExpressionWithoutSideEffect);
	}

	private static boolean isMethodInvocationWithoutSideEffect(MethodInvocation methodInvocation) {
		Expression methodInvocationExpression = methodInvocation.getExpression();
		if (methodInvocationExpression != null && !isExpressionWithoutSideEffect(methodInvocationExpression)) {
			return false;
		}
		IMethodBinding methodBinding = methodInvocation.resolveMethodBinding();
		if (methodBinding == null || !BindingWithoutSideEffect.isSupportedMethod(methodBinding)) {
			return false;
		}
		return ASTNodeUtil.convertToTypedList(methodInvocation.arguments(), Expression.class)
			.stream()
			.allMatch(ExpressionWithoutSideEffectRecursive::isExpressionWithoutSideEffect);
	}

	private static boolean isArrayCreationWithoutSideEffect(ArrayCreation arrayCreation) {

		List<Expression> dimensions = ASTNodeUtil.convertToTypedList(arrayCreation.dimensions(), Expression.class);
		boolean dimensionswithoutSideEffect = dimensions.stream()
			.allMatch(ExpressionWithoutSideEffectRecursive::isExpressionWithoutSideEffect);
		if (dimensionswithoutSideEffect) {
			ArrayInitializer arrayInitializer = arrayCreation.getInitializer();
			return arrayInitializer == null || isArrayInitializerWithoutSideEffect(arrayInitializer);
		}
		return false;
	}

	private static boolean isArrayInitializerWithoutSideEffect(ArrayInitializer arrayInitializer) {
		List<Expression> initializerExpressions = ASTNodeUtil.convertToTypedList(arrayInitializer.expressions(),
				Expression.class);

		return initializerExpressions
			.stream()
			.allMatch(ExpressionWithoutSideEffectRecursive::isExpressionWithoutSideEffect);

	}

	private static boolean isArrayAccessWithoutSideEffect(ArrayAccess arrayAccess) {
		Expression index = arrayAccess.getIndex();
		Expression array = arrayAccess.getArray();
		return isExpressionWithoutSideEffect(array) && isExpressionWithoutSideEffect(index);
	}

	private static boolean isSimpleLambdaExpression(LambdaExpression lambdaExpression) {
		ASTNode lambdaBody = lambdaExpression.getBody();
		if (lambdaBody.getNodeType() == ASTNode.BLOCK) {
			Block block = (Block) lambdaBody;
			List<Statement> lambdaStatements = ASTNodeUtil.convertToTypedList(block.statements(), Statement.class);
			if (lambdaStatements.isEmpty()) {
				return true;
			}
			if (lambdaStatements.size() > 1) {
				return false;
			}
			Statement lambdaStatement = lambdaStatements.get(0);
			return lambdaStatement.getNodeType() == ASTNode.EMPTY_STATEMENT ||
					lambdaStatement.getNodeType() == ASTNode.RETURN_STATEMENT ||
					lambdaStatement.getNodeType() == ASTNode.EXPRESSION_STATEMENT ||
					lambdaStatement.getNodeType() == ASTNode.ASSERT_STATEMENT ||
					lambdaStatement.getNodeType() == ASTNode.VARIABLE_DECLARATION_STATEMENT;

		}
		// In this case the body of the lambda expression can only be an
		// expression
		return true;
	}

}
