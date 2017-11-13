package eu.jsparrow.core.visitor;

/**
 * Implementors listen on {@link AbstractASTRewriteASTVisitor} for rewrites.
 * 
 * @author Hans-Jörg Schrödl, Matthias Webhofer
 */
public interface ASTRewriteVisitorListener {

	/**
	 * Notify of a change.
	 */
	public void update(String compilationUnitHandle);

	public boolean remove(String compilationUnitHandle);
}
