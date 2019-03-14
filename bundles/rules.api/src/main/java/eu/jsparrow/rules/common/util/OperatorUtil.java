package eu.jsparrow.rules.common.util;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.BooleanLiteral;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.PostfixExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;

import eu.jsparrow.rules.common.visitor.helper.SimpleExpressionVisitor;

/**
 * A utility class for expressions containing arithmetic operations and
 * assignments.
 * 
 * @author Martin Huter
 * @since 0.9
 */
public class OperatorUtil {

	public static final InfixExpression.Operator EQUALS_EQUALS = InfixExpression.Operator.EQUALS;
	public static final InfixExpression.Operator NOT_EQUALS = InfixExpression.Operator.NOT_EQUALS;
	public static final InfixExpression.Operator GREATER = InfixExpression.Operator.GREATER;
	public static final InfixExpression.Operator LESS_EQUALS = InfixExpression.Operator.LESS_EQUALS;
	public static final InfixExpression.Operator GREATER_EQUALS = InfixExpression.Operator.GREATER_EQUALS;
	public static final InfixExpression.Operator LESS = InfixExpression.Operator.LESS;
	private static final PrefixExpression.Operator NOT = PrefixExpression.Operator.NOT;

	private OperatorUtil() {
	}

	/**
	 * Generates a corresponding arithmetic assignment operator to an arithmetic
	 * operator. Works only for the four base arithmetic operations. Throws
	 * UnsupportedOperationException if other type is inserted + transforms to
	 * += - transforms to -= * transforms to *= / transforms to /=
	 * 
	 * @param infixExpressionOperator
	 *            is an InfixExpression.Operator that is converted
	 * @return returns an Assignment.Operator that is corresponding to the given
	 *         {@link InfixExpression.Operator}
	 */
	public static Assignment.Operator generateOperator(InfixExpression.Operator infixExpressionOperator) {
		if (InfixExpression.Operator.PLUS.equals(infixExpressionOperator)) {
			return Assignment.Operator.PLUS_ASSIGN;
		} else if (InfixExpression.Operator.MINUS.equals(infixExpressionOperator)) {
			return Assignment.Operator.MINUS_ASSIGN;
		} else if (InfixExpression.Operator.TIMES.equals(infixExpressionOperator)) {
			return Assignment.Operator.TIMES_ASSIGN;
		} else if (InfixExpression.Operator.DIVIDE.equals(infixExpressionOperator)) {
			return Assignment.Operator.DIVIDE_ASSIGN;
		}

		throw new UnsupportedOperationException();
	}

	/**
	 * Inverts if possible the operator of the given {@link InfixExpression}.
	 * Otherwise, wraps it into a negated {@link ParenthesizedExpression}. E.g.
	 * the following:
	 * 
	 * <pre>
	 * <code>
	 * a == b
	 * a == b == c
	 * </code>
	 * </pre>
	 * 
	 * are transformed to
	 * 
	 * <pre>
	 * <code>
	 * a != b
	 * !(a == b == c)
	 * </code>
	 * </pre>
	 * 
	 * Supports the invertion of the following operators:
	 * <ul>
	 * <li>{@link InfixExpression.Operator#EQUALS}</li>
	 * <li>{@link InfixExpression.Operator#NOT_EQUALS}</li>
	 * <li>{@link InfixExpression.Operator#GREATER}</li>
	 * <li>{@link InfixExpression.Operator#GREATER_EQUALS}</li>
	 * <li>{@link InfixExpression.Operator#LESS}</li>
	 * <li>{@link InfixExpression.Operator#LESS_EQUALS}</li>
	 * </ul>
	 * 
	 * <p/>
	 * <b>Note:</b> the returned expression must not be discarded!
	 * 
	 * @param infixExpression
	 *            the expression to be inverted
	 * @param astRewrite
	 *            a rewriter for recording the changes
	 * @param ast
	 *            the ast for creating the negated expression
	 * @return a new infix expression equivalent with the negated form of the
	 *         provided expression.
	 */
	public static Expression negateInfixExpression(InfixExpression infixExpression, ASTRewrite astRewrite) {
		AST ast = astRewrite.getAST();
		InfixExpression.Operator operator = infixExpression.getOperator();
		InfixExpression.Operator newOperator = null;

		if (!isSimpleExpression(infixExpression)) {
			return createNegatedParenthesized(infixExpression, astRewrite);
		}

		if (EQUALS_EQUALS.equals(operator)) {
			newOperator = NOT_EQUALS;
		} else if (NOT_EQUALS.equals(operator)) {
			newOperator = EQUALS_EQUALS;
		} else if (GREATER.equals(operator)) {
			newOperator = LESS_EQUALS;
		} else if (GREATER_EQUALS.equals(operator)) {
			newOperator = LESS;
		} else if (LESS.equals(operator)) {
			newOperator = GREATER_EQUALS;
		} else if (LESS_EQUALS.equals(operator)) {
			newOperator = GREATER;
		}

		if (newOperator == null) {
			return createNegatedParenthesized(infixExpression, astRewrite);
		}

		InfixExpression guardInfixExpression = ast.newInfixExpression();
		guardInfixExpression.setOperator(newOperator);
		guardInfixExpression.setLeftOperand((Expression) astRewrite.createCopyTarget(infixExpression.getLeftOperand()));
		guardInfixExpression
			.setRightOperand((Expression) astRewrite.createCopyTarget(infixExpression.getRightOperand()));

		return guardInfixExpression;
	}

	/**
	 * Checks whether the given {@link InfixExpression} contains further
	 * {@link InfixExpression}, {@link PrefixExpression} or
	 * {@link PostfixExpression} expression.
	 * 
	 * 
	 * @param expression
	 *            the expression to be checked
	 * @return if the above condition is satisfied.
	 */
	public static boolean isSimpleExpression(InfixExpression expression) {
		Expression left = expression.getLeftOperand();
		boolean isSimpleLeftOperand = isSimpleExpression(left);
		if (!isSimpleLeftOperand) {
			return false;
		}
		Expression right = expression.getRightOperand();
		return isSimpleExpression(right);
	}

	/**
	 * Checks whether an expression contains other {@link InfixExpression}s,
	 * {@link PrefixExpression}s or {@link PostfixExpression}s.
	 * 
	 * @see SimpleExpressionVisitor
	 * 
	 * @param expression
	 *            the expression to be checked
	 * @return if the above condition is met.
	 */
	public static boolean isSimpleExpression(Expression expression) {
		SimpleExpressionVisitor visitor = new SimpleExpressionVisitor();
		expression.accept(visitor);
		return visitor.isSimple();
	}

	/**
	 * Wraps the given expression into a {@link ParenthesizedExpression} and
	 * negates it with a {@link PrefixExpression.Operator#NOT}.
	 * <p/>
	 * 
	 * <b>Note:</b> the returned expression must not be discarded!
	 * @param expression
	 *            expression to be negated
	 * @param astRewrite
	 *            an {@link ASTRewrite} to register the changes
	 * 
	 * @return the negated expression of type {@link PrefixExpression}.
	 */
	public static Expression createNegatedParenthesized(Expression expression, ASTRewrite astRewrite) {
		AST ast = astRewrite.getAST();
		PrefixExpression guardPrefixExpression = ast.newPrefixExpression();
		guardPrefixExpression.setOperator(NOT);
		ParenthesizedExpression parenthesizedExpression = ast.newParenthesizedExpression();
		parenthesizedExpression.setExpression((Expression) astRewrite.createCopyTarget(expression));
		guardPrefixExpression.setOperand(parenthesizedExpression);
		return guardPrefixExpression;
	}

	/**
	 * Creates a new {@link Expression} with the inverted logic by:
	 * <ul>
	 * <li>evaluating a new operator for {@link InfixExpression}s. See
	 * {@link #negateInfixExpression()}</li>
	 * <li>removing the existing negation operator {@link #NOT} in
	 * {@link PrefixExpression}s</li>
	 * <li>inverting the boolean literals</li>
	 * <li>or inserting a new negation operator {@link #NOT}</li>
	 * </ul>
	 * 
	 * @param expression
	 *            expression to be negated
	 * @return a new uparented negated expression.
	 */
	public static Expression createNegatedExpression(Expression expression, ASTRewrite astRewrite) {
		int expressionType = expression.getNodeType();
		AST ast = expression.getAST();
		if (ASTNode.INFIX_EXPRESSION == expressionType) {
			InfixExpression infixExpression = (InfixExpression) expression;
			return negateInfixExpression(infixExpression, astRewrite);
		}

		if (ASTNode.PREFIX_EXPRESSION == expressionType) {
			PrefixExpression prefixExpression = (PrefixExpression) expression;
			PrefixExpression.Operator operator = prefixExpression.getOperator();

			Expression negatedExpression;
			if (NOT.equals(operator)) {
				Expression body = prefixExpression.getOperand();
				if (ASTNode.PARENTHESIZED_EXPRESSION == body.getNodeType()) {
					ParenthesizedExpression parenthesizedExpression = (ParenthesizedExpression) body;
					body = parenthesizedExpression.getExpression();
				}
				negatedExpression = (Expression) astRewrite.createCopyTarget(body);
			} else {
				negatedExpression = createNegatedParenthesized(expression, astRewrite);
			}
			return negatedExpression;
		}

		if (ASTNode.METHOD_INVOCATION == expressionType || ASTNode.SIMPLE_NAME == expressionType) {
			return negate(astRewrite, expression);
		}

		if (ASTNode.BOOLEAN_LITERAL != expressionType) {
			return createNegatedParenthesized(expression, astRewrite);
		}

		BooleanLiteral booleanLiteral = (BooleanLiteral) expression;
		if (booleanLiteral.booleanValue()) {
			return ast.newBooleanLiteral(false);
		}
		return ast.newBooleanLiteral(true);
	}

	/**
	 * Adds a {@link PrefixExpression.Operator#NOT} in front of the given
	 * expression.
	 * 
	 * @param astRewrite
	 *            the {@link ASTRewrite} to create a copy of the given
	 *            expression
	 * @param expression
	 *            expression to be negated
	 * @return a new unparented {@link Expression} negated with the
	 *         {@link PrefixExpression.Operator#NOT}.
	 */
	public static PrefixExpression negate(ASTRewrite astRewrite, Expression expression) {
		AST ast = astRewrite.getAST();
		PrefixExpression prefixExpression = ast.newPrefixExpression();
		prefixExpression.setOperator(NOT);
		prefixExpression.setOperand((Expression) astRewrite.createCopyTarget(expression));
		return prefixExpression;
	}

}
