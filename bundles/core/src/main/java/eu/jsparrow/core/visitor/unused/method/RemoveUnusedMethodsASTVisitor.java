package eu.jsparrow.core.visitor.unused.method;

import java.util.List;

import eu.jsparrow.rules.common.visitor.AbstractASTRewriteASTVisitor;

public class RemoveUnusedMethodsASTVisitor extends AbstractASTRewriteASTVisitor {

	private List<UnusedMethodWrapper> unusedMethods;
	
	public RemoveUnusedMethodsASTVisitor(List<UnusedMethodWrapper> unusedMethods) {
		this.unusedMethods = unusedMethods;
	}
}
