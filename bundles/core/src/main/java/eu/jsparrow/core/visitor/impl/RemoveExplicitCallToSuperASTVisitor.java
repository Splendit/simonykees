package eu.jsparrow.core.visitor.impl;

import org.eclipse.jdt.core.dom.MethodDeclaration;

import eu.jsparrow.rules.common.visitor.AbstractASTRewriteASTVisitor;

public class RemoveExplicitCallToSuperASTVisitor extends AbstractASTRewriteASTVisitor {

	@Override
	public boolean visit(MethodDeclaration node) {
		// if it is not a constructor, return
		if (!node.isConstructor()) {
			return false;
		}
		// TODO if it is, check if first invocation is to default parent constructor
				
		return super.visit(node);
	}
}
