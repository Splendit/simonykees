package eu.jsparrow.core.visitor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;

/**
 * Abstract implementation of an {@link ASTVisitor} to assure all used visitors
 * have an field for {@link ASTRewrite} to commit the changes in the tree.
 * 
 * @author Martin Huter, Hans-Jörg Schrödl
 * @since 0.9
 */

public abstract class AbstractASTRewriteASTVisitor extends ASTVisitor {

	protected ASTRewrite astRewrite;
	
	protected List<ASTRewriteVisitorListener> listeners = new ArrayList<>();
	
	public AbstractASTRewriteASTVisitor() {
		super();
	}

	public AbstractASTRewriteASTVisitor(boolean visitDocTags) {
		super(visitDocTags);
	}

	/**
	 * Gets the {@link ASTRewrite} for this instance.
	 * @return the ASTRewrite
	 */
	public ASTRewrite getASTRewrite() {
		return astRewrite;
	}

	/**
	 * Sets the {@link ASTRewrite} for this instance.
	 * @param astRewrite astRewrite to set
	 */
	public void setASTRewrite(ASTRewrite astRewrite) {
		this.astRewrite = astRewrite;
	}


	/**
	 * Converts a qualified name to a list
	 * @param fullyQualifiedName the qualified name as string
	 * @return a list of the qualified name
	 */
	protected List<String> generateFullyQualifiedNameList(String... fullyQualifiedName) {
		return Arrays.asList(fullyQualifiedName);
	}
	
	/**
	 * Adds an {@link ASTRewriteVisitorListener}
	 * @param listener listener to add
	 */
	public void addRewriteListener(ASTRewriteVisitorListener listener) {
		listeners.add(listener);
	}
	
	/**
	 * Removes a {@link ASTRewriteVisitorListener}
	 * @param listener listener to remove
	 */
	public void removeRewriteListener(ASTRewriteVisitorListener listener) {
		listeners.remove(listener);
	}
	
	/**
	 * Notifies all listeners that a rewrite occurred. 
	 */
	public void onRewrite() {
		listeners.forEach(ASTRewriteVisitorListener::update);
	}

}