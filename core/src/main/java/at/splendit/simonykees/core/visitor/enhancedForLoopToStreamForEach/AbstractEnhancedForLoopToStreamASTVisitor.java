package at.splendit.simonykees.core.visitor.enhancedForLoopToStreamForEach;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;

import at.splendit.simonykees.core.visitor.lambdaForEach.AbstractLambdaForEachASTVisitor;

/**
 * An abstract class to be extended by the visitors that convert
 * an {@link EnhancedForStatement} to a stream. 
 * 
 * @author Matthias Webhofer, Ardit Ymeri
 * @since 2.1.1
 *
 */
public abstract class AbstractEnhancedForLoopToStreamASTVisitor extends AbstractLambdaForEachASTVisitor {
	
	/**
	 * Checks whether the type binding is a raw type, capture, wildcard or a
	 * parameterized type having any of the above as a parameter.
	 * 
	 * @param typeBinding
	 * @return {@code false} if any of the aforementioned types, or {@link true}
	 *         otherwise.
	 */
	protected boolean isTypeSafe(ITypeBinding typeBinding) {
		if (typeBinding.isRawType()) {
			return false;
		}

		if (typeBinding.isCapture()) {
			return false;
		}

		if (typeBinding.isWildcardType()) {
			return false;
		}

		if (typeBinding.isParameterizedType()) {
			for (ITypeBinding argument : typeBinding.getTypeArguments()) {
				return isTypeSafe(argument);
			}
		}

		return true;
	}
	
	/**
	 * creates a copy target for the expression on the left of the stream()
	 * method invocation. if the expression itself is a cast expression, then it
	 * will be wrapped in a parenthesized expression.
	 * 
	 * @param expression
	 *            the expression, which will be on the left of the stream method
	 *            invocation
	 * @return a copy target of the given expression, or a parenthesized
	 *         expression (if expression is of type CastExpression.
	 */
	protected Expression createExpressionForStreamMethodInvocation(Expression expression) {
		Expression expressionCopy = (Expression) astRewrite.createCopyTarget(expression);
		if (expression.getNodeType() == ASTNode.CAST_EXPRESSION) {
			ParenthesizedExpression parenthesizedExpression = astRewrite.getAST().newParenthesizedExpression();
			parenthesizedExpression.setExpression(expressionCopy);
			return parenthesizedExpression;
		}
		return expressionCopy;
	}
}
