package eu.jsparrow.core.visitor.unused;

import java.util.Map;

import eu.jsparrow.rules.common.visitor.AbstractASTRewriteASTVisitor;

public class RemoveUnusedLocalVariabesASTVisitor extends AbstractASTRewriteASTVisitor {

	private final Map<String, Boolean> options;

	public RemoveUnusedLocalVariabesASTVisitor(Map<String, Boolean> options) {
		this.options = options;
	}
}
