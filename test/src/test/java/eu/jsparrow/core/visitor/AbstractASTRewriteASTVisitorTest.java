package eu.jsparrow.core.visitor;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

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

		assertTrue(listener.wasUpdated);
	}

	class DummyASTRewriteASTVisitorImpl extends AbstractASTRewriteASTVisitor {

	}
}
