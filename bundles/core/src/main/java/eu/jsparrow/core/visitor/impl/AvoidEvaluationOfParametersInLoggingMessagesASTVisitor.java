package eu.jsparrow.core.visitor.impl;


import org.eclipse.jdt.core.dom.MethodInvocation;

import eu.jsparrow.rules.common.visitor.AbstractASTRewriteASTVisitor;

/**
 * @since 3.18.0
 */
public class AvoidEvaluationOfParametersInLoggingMessagesASTVisitor extends AbstractASTRewriteASTVisitor {

	@Override
	public boolean visit(MethodInvocation methodInvocation) {
		return true;
	}

}
