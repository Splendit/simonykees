package eu.jsparrow.core.visitor;

/**
 * Implementors listen on {@link AbstractASTRewriteASTVisitor} for rewrites.
 * 
 * @author Hans-Jörg Schrödl
 */
public interface ASTRewriteVisitorListener {

	/**
	 * Notify of a change.
	 */
	public void update();
}
