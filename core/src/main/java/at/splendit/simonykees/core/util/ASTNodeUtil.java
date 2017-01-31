package at.splendit.simonykees.core.util;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.WildcardType;

/**
 * TODO SIM-103 add class description
 * 
 * @author Martin Huter
 * @since 0.9.2
 */
public class ASTNodeUtil {

	/**
	 * Finds the surrounding Block node if there is one, otherwise returns null
	 * 
	 * @param node
	 *            ASTNode where the backward search is started
	 * @return surrounding {@link Block}, null if non exists
	 */
	public static Block getSurroundingBlock(ASTNode node) {
		if (node == null) {
			return null;
		}
		if (node.getParent() instanceof Block) {
			return (Block) node.getParent();
		} else {
			return getSurroundingBlock(node.getParent());
		}
	}

	/**
	 * Removes all surrounding parenthesizes
	 * 
	 * @param expression
	 *            is unwrapped, if it is a {@link ParenthesizedExpression}
	 * @return unwrapped expression
	 */
	public static Expression unwrapParenthesizedExpression(Expression expression) {
		if (expression instanceof ParenthesizedExpression) {
			return unwrapParenthesizedExpression(((ParenthesizedExpression) expression).getExpression());
		}
		return expression;
	}

	/** Returns the type parameter of a variableDeclaration if it only contains exactly one
	 * 
	 * @param variableDeclaration
	 *            {@link VariableDeclarationStatement} or
	 *            {@link VariableDeclarationExpression} that holds exactly on
	 *            type parameter
	 * @return
	 */
	public static Type getSingleTypeParameterOfVariableDeclaration(ASTNode variableDeclaration) {
		if (null == variableDeclaration) {
			return null;
		}

		Type tempType = null;
		if (ASTNode.VARIABLE_DECLARATION_STATEMENT == variableDeclaration.getNodeType()) {
			tempType = ((VariableDeclarationStatement) variableDeclaration).getType();
		}

		if (ASTNode.VARIABLE_DECLARATION_EXPRESSION == variableDeclaration.getNodeType()) {
			tempType = ((VariableDeclarationExpression) variableDeclaration).getType();
		}

		if (ASTNode.PARAMETERIZED_TYPE == tempType.getNodeType()) {
			ParameterizedType parameterizedType = (ParameterizedType) tempType;
			if (1 == parameterizedType.typeArguments().size()) {
				Type parameterType = (Type) parameterizedType.typeArguments().get(0);
				if (parameterType.isWildcardType()) {
					WildcardType wildcardType = (WildcardType) parameterType;
					if (wildcardType.isUpperBound()) {
						return wildcardType.getBound();
					}
				} else if (parameterType.isSimpleType()) {
					return parameterType;
				}
			}
		}

		return null;
	}
	
	public static SimpleName replaceableIteratorCondition(Expression node) {
		if (node instanceof MethodInvocation) {
			MethodInvocation methodInvocation = (MethodInvocation) node;
			// check for hasNext operation on Iterator
			if (StringUtils.equals("hasNext", methodInvocation.getName().getFullyQualifiedName()) //$NON-NLS-1$
					&& methodInvocation.getExpression() instanceof SimpleName) {
				return (SimpleName) methodInvocation.getExpression();
			}
		}
		return null;
	}
}
