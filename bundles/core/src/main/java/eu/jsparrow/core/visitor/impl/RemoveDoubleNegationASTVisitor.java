package eu.jsparrow.core.visitor.impl;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.PrefixExpression;

import eu.jsparrow.rules.common.visitor.AbstractASTRewriteASTVisitor;

/**
 * ASTVisitor that searches control statements for non-block bodies and wraps it
 * into a block.
 * 
 * @since 2.7
 *
 */
public class RemoveDoubleNegationASTVisitor extends AbstractASTRewriteASTVisitor {

	@Override
	public boolean visit(PrefixExpression prefixExpression) {
		ASTNode replaceNode = unwrapNegations(prefixExpression, true);

		if (null != replaceNode && prefixExpression != replaceNode) {
			astRewrite.replace(prefixExpression, replaceNode, null);
			onRewrite();			
		}

		return true;
	}

	
	/**
	 * Recursive search for possible replacement node
	 * @param node input node
	 * @param selector alternating selector for node
	 * @return node to replace the expression with
	 */
	ASTNode unwrapNegations(ASTNode node, boolean selector) {
		PrefixExpression prefixExpression = null;
		if (ASTNode.PREFIX_EXPRESSION == node.getNodeType()) {
			prefixExpression = (PrefixExpression) node;
		}
		
		boolean isNotOperator = prefixExpression != null && PrefixExpression.Operator.NOT == prefixExpression.getOperator();

		if (isNotOperator) {
			ASTNode returnValue = unwrapNegations(prefixExpression.getOperand(), !selector);
			if (null == returnValue) {
				return node;
			} else {
				return returnValue;
			}
		}
		else {
			if(selector) {
				return node;
			}
			return null;
		}
	}
}
