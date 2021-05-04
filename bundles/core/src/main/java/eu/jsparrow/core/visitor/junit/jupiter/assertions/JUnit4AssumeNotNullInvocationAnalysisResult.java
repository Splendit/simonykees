package eu.jsparrow.core.visitor.junit.jupiter.assertions;

import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.ExpressionStatement;

class JUnit4AssumeNotNullInvocationAnalysisResult {
	private final JUnit4MethodInvocationAnalysisResult jUnitInvocationData;
	private ExpressionStatement methodInvocationStatement;
	private Block block;

	JUnit4AssumeNotNullInvocationAnalysisResult(JUnit4MethodInvocationAnalysisResult jUnitInvocationData,
			ExpressionStatement methodInvocationStatement, Block block) {
		this(jUnitInvocationData);
		this.methodInvocationStatement = methodInvocationStatement;
		this.block = block;
	}

	JUnit4AssumeNotNullInvocationAnalysisResult(JUnit4MethodInvocationAnalysisResult jUnitInvocationData) {
		this.jUnitInvocationData = jUnitInvocationData;
	}

	public ExpressionStatement getMethodInvocationStatement() {
		return methodInvocationStatement;
	}

	public Block getBlock() {
		return block;
	}

	public JUnit4MethodInvocationAnalysisResult getJUnit4InvocationData() {
		return jUnitInvocationData;
	}
}
