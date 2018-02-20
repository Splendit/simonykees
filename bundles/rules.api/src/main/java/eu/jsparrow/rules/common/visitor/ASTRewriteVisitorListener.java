package eu.jsparrow.rules.common.visitor;

/**
 * Implementors listen on {@link AbstractASTRewriteASTVisitor} for rewrites.
 * 
 * @author Hans-Jörg Schrödl, Matthias Webhofer
 */
public interface ASTRewriteVisitorListener {

	/**
	 * Updates implementor with the given event.
	 * 
	 * @param event event published by a {@link AbstractASTRewriteASTVisitor}
	 */
	public void update(ASTRewriteEvent event);
}
