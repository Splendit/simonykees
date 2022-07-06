package eu.jsparrow.core.visitor.impl;

import org.eclipse.jdt.core.dom.TypeLiteral;

import eu.jsparrow.rules.common.visitor.AbstractASTRewriteASTVisitor;

/**
 * 
 * @since 4.13.0
 *
 */
public class ReplaceLoggingWithForeignClassASTVisitor extends AbstractASTRewriteASTVisitor {

	@Override
	public boolean visit(TypeLiteral node) {
		return false;
	}
}
