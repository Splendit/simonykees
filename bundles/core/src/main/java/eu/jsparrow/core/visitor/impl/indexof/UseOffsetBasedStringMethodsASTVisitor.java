package eu.jsparrow.core.visitor.impl.indexof;

import org.eclipse.jdt.core.dom.MethodInvocation;

import eu.jsparrow.rules.common.visitor.AbstractASTRewriteASTVisitor;

public class UseOffsetBasedStringMethodsASTVisitor extends AbstractASTRewriteASTVisitor {

	@Override
	public boolean visit(MethodInvocation node) {
		return true;
	}
	

	

}
