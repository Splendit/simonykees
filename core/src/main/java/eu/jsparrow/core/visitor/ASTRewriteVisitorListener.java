package eu.jsparrow.core.visitor;

import org.eclipse.jdt.core.ICompilationUnit;

/**
 * Implementors listen on {@link AbstractASTRewriteASTVisitor} for rewrites.
 * 
 * @author Hans-Jörg Schrödl
 */
public interface ASTRewriteVisitorListener {

	/**
	 * Notify of a change.
	 */
	public void update(String compilationUnitHandle);
	
	public boolean remove(String compilationUnitHandle);
}
