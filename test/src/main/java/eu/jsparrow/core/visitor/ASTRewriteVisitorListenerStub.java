package eu.jsparrow.core.visitor;

/**
 * This class is a simple stub to verify if a listener was updated by an astVisitor.
 * @author Hans-Jörg Schrödl
 *
 */
public class ASTRewriteVisitorListenerStub implements ASTRewriteVisitorListener {

	public boolean wasUpdated = false;

	@Override
	public void update() {
		wasUpdated = true;
	}

}