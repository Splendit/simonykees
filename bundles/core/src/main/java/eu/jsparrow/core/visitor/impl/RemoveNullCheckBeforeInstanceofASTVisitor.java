package eu.jsparrow.core.visitor.impl;

import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.InstanceofExpression;

import eu.jsparrow.rules.common.visitor.AbstractASTRewriteASTVisitor;

/**
 * @since 3.8.0
 */
public class RemoveNullCheckBeforeInstanceofASTVisitor extends AbstractASTRewriteASTVisitor {

	@Override
	public boolean visit(InstanceofExpression expression) {
		
		if (InfixExpression.RIGHT_OPERAND_PROPERTY == expression.getLocationInParent()) {
			// this is what we are looking for
		}
		
		return super.visit(expression);
	}

}
