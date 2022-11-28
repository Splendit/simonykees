package eu.jsparrow.rules.java16.switchexpression;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ArrayAccess;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldAccess;

/**
 * Helper class to determine whether or not the referencing of a variable is
 * causing side effects. If it can be excluded that the referencing of an
 * Expression has side effects, then it is possible to reference that expression
 * in any order and with any frequency without causing a difference of vstate
 * and behavior.
 * 
 * @since 4.14.0
 *
 */
public class VariableWithoutSideEffect {

	public static boolean isVariableWithoutSideEffect(Expression expression) {
		int expressionNodeType = expression.getNodeType();

		if (expressionNodeType == ASTNode.FIELD_ACCESS) {
			return isVariableWithoutSideEffect(((FieldAccess) expression).getExpression());
		}

		if (expressionNodeType == ASTNode.ARRAY_ACCESS) {
			return isArrayAccessWithoutSideEffect((ArrayAccess) expression);
		}

		return expressionNodeType == ASTNode.SIMPLE_NAME
				|| expressionNodeType == ASTNode.QUALIFIED_NAME
				|| expressionNodeType == ASTNode.THIS_EXPRESSION
				|| expressionNodeType == ASTNode.SUPER_FIELD_ACCESS;
	}

	private static boolean isArrayAccessWithoutSideEffect(ArrayAccess arrayAccess) {
		Expression index = arrayAccess.getIndex();
		Expression array = arrayAccess.getArray();
		return isVariableWithoutSideEffect(array)
				&& (index.getNodeType() == ASTNode.NUMBER_LITERAL || isVariableWithoutSideEffect(index));
	}

	private VariableWithoutSideEffect() {
		// private default constructor of utility class in order to hide
		// implicit public one
	}
}
