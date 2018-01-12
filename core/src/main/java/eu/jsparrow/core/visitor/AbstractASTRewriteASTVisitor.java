package eu.jsparrow.core.visitor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.Comment;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;

import eu.jsparrow.core.util.ASTNodeUtil;

/**
 * Abstract implementation of an {@link ASTVisitor} to assure all used visitors
 * have an field for {@link ASTRewrite} to commit the changes in the tree.
 * 
 * @author Martin Huter, Hans-Jörg Schrödl, Matthias Webhofer
 * @since 0.9
 */

public abstract class AbstractASTRewriteASTVisitor extends ASTVisitor {

	protected ASTRewrite astRewrite;

	protected String compilationUnitHandle;

	protected List<ASTRewriteVisitorListener> listeners = new ArrayList<>();

	public AbstractASTRewriteASTVisitor() {
		super();
	}

	public AbstractASTRewriteASTVisitor(boolean visitDocTags) {
		super(visitDocTags);
	}

	/**
	 * Gets the {@link ASTRewrite} for this instance.
	 * 
	 * @return the ASTRewrite
	 */
	public ASTRewrite getASTRewrite() {
		return astRewrite;
	}

	/**
	 * Sets the {@link ASTRewrite} for this instance.
	 * 
	 * @param astRewrite
	 *            astRewrite to set
	 */
	public void setASTRewrite(ASTRewrite astRewrite) {
		this.astRewrite = astRewrite;
	}

	public String getCompilationUnit() {
		return compilationUnitHandle;
	}

	public void setCompilationUnit(String compilationUnitHandle) {
		this.compilationUnitHandle = compilationUnitHandle;
	}

	/**
	 * Converts a qualified name to a list
	 * 
	 * @param fullyQualifiedName
	 *            the qualified name as string
	 * @return a list of the qualified name
	 */
	protected List<String> generateFullyQualifiedNameList(String... fullyQualifiedName) {
		return Arrays.asList(fullyQualifiedName);
	}

	/**
	 * Adds an {@link ASTRewriteVisitorListener}
	 * 
	 * @param listener
	 *            listener to add
	 */
	public void addRewriteListener(ASTRewriteVisitorListener listener) {
		listeners.add(listener);
	}

	/**
	 * Removes a {@link ASTRewriteVisitorListener}
	 * 
	 * @param listener
	 *            listener to remove
	 */
	public void removeRewriteListener(ASTRewriteVisitorListener listener) {
		listeners.remove(listener);
	}

	/**
	 * Notifies all listeners that a rewrite occurred.
	 */
	protected void onRewrite() {
		listeners.forEach(listener -> listener.update(new ASTRewriteEvent(this.compilationUnitHandle)));
	}

	protected List<Comment> findRelatedComments(ASTNode node, CompilationUnit compilationUnit, List<Comment>comments) {
		List<Comment> relatedComments = new ArrayList<>();
		relatedComments.addAll(findInternalComments(node, comments));
		int leadingCommentIndex = compilationUnit.firstLeadingCommentIndex(node);
		if (leadingCommentIndex >= 0) {
			relatedComments.add(0, comments.get(leadingCommentIndex));
		}
		
		int trailCommentIndex = compilationUnit.lastTrailingCommentIndex(node);
		if (trailCommentIndex >= 0) {
			relatedComments.add(comments.get(trailCommentIndex));
		}
	
		return relatedComments;
	}

	private Collection<Comment> findInternalComments(ASTNode node, List<Comment> comments) {
		int nodeStartPos = node.getStartPosition();
		int nodeEndPos = nodeStartPos + node.getLength();
	
		return comments.stream()
			.filter(comment -> comment.getStartPosition() > nodeStartPos && comment.getStartPosition() < nodeEndPos)
			.collect(Collectors.toList());
	}

	protected void addComment(Statement node, String content) {
	
		Block block = ASTNodeUtil.getSpecificAncestor(node, Block.class);
	
		ListRewrite listRewrite = astRewrite.getListRewrite(block, Block.STATEMENTS_PROPERTY);
		Statement placeHolder = (Statement) astRewrite.createStringPlaceholder(content,
				ASTNode.EMPTY_STATEMENT);
		listRewrite.insertBefore(placeHolder, node, null);
	}

}