package eu.jsparrow.core.visitor;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.dummies.ASTRewriteVisitorListenerStub;
import eu.jsparrow.rules.common.visitor.AbstractASTRewriteASTVisitor;

public class AbstractASTRewriteASTVisitorTest {

	private AbstractASTRewriteASTVisitor visitor;

	@BeforeEach
	public void setUp() {
		visitor = new DummyASTRewriteASTVisitorImpl();
	}

	@Test
	public void onRewrite_withListener_shouldUpdateListener() {
		ASTRewriteVisitorListenerStub listener = new ASTRewriteVisitorListenerStub();
		visitor.addRewriteListener(listener);

		visitor.onRewrite();

		assertTrue(listener.wasUpdated());
	}

	class DummyASTRewriteASTVisitorImpl extends AbstractASTRewriteASTVisitor {

	}
}
