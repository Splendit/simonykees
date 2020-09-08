package eu.jsparrow.core.visitor.impl;

import org.eclipse.jdt.core.dom.MethodInvocation;

import eu.jsparrow.rules.common.visitor.AbstractAddImportASTVisitor;

/**
 * 
 * 
 * @since 3.21.0
 *
 */
public class UsePredefinedStandardCharsetASTVisitor extends AbstractAddImportASTVisitor {

	@Override
	public boolean visit(MethodInvocation node) {
		return true;
	}

}
