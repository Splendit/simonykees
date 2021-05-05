package eu.jsparrow.core.visitor.junit.jupiter.assertions;

import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.ExpressionStatement;

class AssumeNotNullInvocationAncestors {
	private final ExpressionStatement parent;
	private final Block block;

	public AssumeNotNullInvocationAncestors(ExpressionStatement parent, Block block) {
		super();
		this.parent = parent;
		this.block = block;
	}

	public ExpressionStatement getParent() {
		return parent;
	}

	public Block getBlock() {
		return block;
	}
}