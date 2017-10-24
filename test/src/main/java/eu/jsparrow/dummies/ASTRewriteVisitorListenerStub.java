package eu.jsparrow.dummies;

import eu.jsparrow.core.visitor.ASTRewriteVisitorListener;

/**
 * This class is a simple stub to verify if a listener was updated by an astVisitor.
 * Strictly for use in unit tests. 
 * 
 * @author Hans-Jörg Schrödl
 *
 */
public class ASTRewriteVisitorListenerStub implements ASTRewriteVisitorListener {

	private boolean wasUpdated = false;

	@Override
	public void update() {
		wasUpdated = true;
	}
	
	public boolean wasUpdated() {
		return wasUpdated;
	}

}