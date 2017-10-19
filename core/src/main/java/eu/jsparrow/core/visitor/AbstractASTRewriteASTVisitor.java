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
 * @author Martin Huter
 * @since 0.9
 */

public abstract class AbstractASTRewriteASTVisitor extends ASTVisitor {

	protected ASTRewrite astRewrite;
	
	private List<ASTRewriteVisitorListener> listeners = new ArrayList<>();
	
	public AbstractASTRewriteASTVisitor() {
		super();
	}

	public AbstractASTRewriteASTVisitor(boolean visitDocTags) {
		super(visitDocTags);
	}

	public ASTRewrite getASTRewrite() {
		return astRewrite;
	}

	public void setASTRewrite(ASTRewrite astRewrite) {
		this.astRewrite = astRewrite;
	}

	protected List<String> generateFullyQualifiedNameList(String... fullyQuallifiedName) {
		return Arrays.asList(fullyQuallifiedName);
	}
	
	public void addRewriteListener(ASTRewriteVisitorListener listener) {
		listeners.add(listener);
	}
	
	public void removeRewriteListener(ASTRewriteVisitorListener listener) {
		listeners.remove(listener);
	}
	
	public void onRewrite() {
		listeners.forEach(ASTRewriteVisitorListener::update);
	}

}