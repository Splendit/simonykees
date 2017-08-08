package at.splendit.simonykees.core.visitor;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.NumberLiteral;

import at.splendit.simonykees.core.util.ClassRelationUtil;

/**
 * Finds all instantiations of {@link String} with no input parameter (new
 * String()) and all instantiations of {@link String} with a {@link String}
 * parameter (new String("foo")) and replaces those occurrences empty String
 * ("") or a String literal ("foo") respectively.
 * 
 * @author Martin Huter
 * @since 0.9.2
 */
public class UseIsEmptyRuleASTVisitor extends AbstractASTRewriteASTVisitor {

	private static final String STRING_FULLY_QUALLIFIED_NAME = java.lang.String.class.getName();
	private static final String COLLECTION_FULLY_QUALLIFIED_NAME = java.util.Collection.class.getName();
	private static final String MAP_FULLY_QUALLIFIED_NAME = java.util.Map.class.getName();
	private static final String LENGTH = "length";
	private static final String SIZE = "size";

	public boolean visit(MethodInvocation node) {
		if (node.arguments().isEmpty() && ASTNode.INFIX_EXPRESSION == node.getParent().getNodeType()) {
			InfixExpression parent = (InfixExpression) node.getParent();
			// more than two operands are present or the relation is not an
			// equals
			if (!parent.extendedOperands().isEmpty() || InfixExpression.Operator.EQUALS != parent.getOperator()) {
				return false;
			}

			// check type of variable and get name
			Expression varExpression = null;
			List<String> fullyQualifiedStringName = generateFullyQuallifiedNameList(STRING_FULLY_QUALLIFIED_NAME);
			List<String> fullyQualifiedDataStuctures = generateFullyQuallifiedNameList(COLLECTION_FULLY_QUALLIFIED_NAME,
					MAP_FULLY_QUALLIFIED_NAME);
			if (StringUtils.equals(LENGTH, node.getName().getFullyQualifiedName())
					&& ClassRelationUtil.isContentOfTypes(node.getExpression().resolveTypeBinding(),
							fullyQualifiedStringName)
					|| StringUtils.equals(SIZE, node.getName().getFullyQualifiedName()) && ClassRelationUtil
							.isContentOfTypes(node.getExpression().resolveTypeBinding(), fullyQualifiedDataStuctures)) {
				varExpression = node.getExpression();
			}

			// get other operand
			Expression otherOperand = null;
			if (InfixExpression.LEFT_OPERAND_PROPERTY == node.getLocationInParent()) {
				otherOperand = parent.getRightOperand();
			} else {
				otherOperand = parent.getLeftOperand();
			}

			// check if operand is zero (int, long, float, double)
			if (ASTNode.NUMBER_LITERAL == otherOperand.getNodeType()) {
				NumberLiteral nl = (NumberLiteral) otherOperand;
				
			}
		}
		return true;
	}
}
