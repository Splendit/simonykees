package eu.jsparrow.core.visitor.junit.jupiter.assertions;

import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.MethodInvocation;

public class JUnit4AssumeNotNullInvocationAnalysisResult {
	private final MethodInvocation methodInvocation;
	private final ExpressionStatement methodInvocationStatement;
	private final Block block;
	private final boolean transformable;

	public JUnit4AssumeNotNullInvocationAnalysisResult(MethodInvocation methodInvocation,
			ExpressionStatement methodInvocationStatement, Block block, boolean transformable) {
		this.methodInvocation = methodInvocation;
		this.methodInvocationStatement = methodInvocationStatement;
		this.block = block;
		this.transformable = transformable;
	}

	public MethodInvocation getMethodInvocation() {
		return methodInvocation;
	}

	public ExpressionStatement getMethodInvocationStatement() {
		return methodInvocationStatement;
	}

	public Block getBlock() {
		return block;
	}

	public boolean isTransformable() {
		return transformable;
	}
}
