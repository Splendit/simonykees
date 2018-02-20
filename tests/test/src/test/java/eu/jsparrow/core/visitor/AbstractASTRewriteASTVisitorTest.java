package eu.jsparrow.core.visitor;

import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import eu.jsparrow.dummies.ASTRewriteVisitorListenerStub;
import eu.jsparrow.rules.common.visitor.AbstractASTRewriteASTVisitor;

public class AbstractASTRewriteASTVisitorTest {

	private AbstractASTRewriteASTVisitor visitor;

	@Before
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
