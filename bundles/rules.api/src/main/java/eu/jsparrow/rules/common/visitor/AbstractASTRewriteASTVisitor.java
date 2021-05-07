package eu.jsparrow.rules.common.visitor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;

import eu.jsparrow.rules.common.markers.RefactoringMarkerEvent;
import eu.jsparrow.rules.common.markers.RefactoringMarkerListener;
import eu.jsparrow.rules.common.util.GeneratedNodesUtil;
import eu.jsparrow.rules.common.visitor.helper.CommentRewriter;

/**
 * Abstract implementation of an {@link ASTVisitor} to assure all used visitors
 * have an field for {@link ASTRewrite} to commit the changes in the tree.
 * 
 * @author Martin Huter, Hans-Jörg Schrödl, Matthias Webhofer, Ardit Ymeri
 * @since 0.9
 */

public abstract class AbstractASTRewriteASTVisitor extends ASTVisitor {

	private static final String EMPTY_STRING = ""; //$NON-NLS-1$

	protected ASTRewrite astRewrite;

	protected String compilationUnitHandle;

	protected List<ASTRewriteVisitorListener> listeners = new ArrayList<>();
	private List<RefactoringMarkerListener> refactoringMarkerListeners = new ArrayList<>();

	protected String compilationUnitSource = EMPTY_STRING;

	private CompilationUnit compilationUnit;
	private CommentRewriter commentRewriter;

	public AbstractASTRewriteASTVisitor() {
		super();
		commentRewriter = new CommentRewriter();
	}

	public AbstractASTRewriteASTVisitor(boolean visitDocTags) {
		super(visitDocTags);
	}

	@Override
	public boolean visit(CompilationUnit compilationUnit) {
		this.compilationUnit = compilationUnit;
		this.commentRewriter.initCommentHelper(compilationUnit, astRewrite);
		return true;
	}

	@Override
	public void endVisit(CompilationUnit compilationUnit) {
		this.commentRewriter.resetCommentHelper();
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

	public CompilationUnit getCompilationUnit() {
		return compilationUnit;
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
	public void onRewrite() {
		listeners.forEach(listener -> listener.update(new ASTRewriteEvent(this.compilationUnitHandle)));
	}

	protected void addMarkerEvent(ASTNode original, ASTNode newNode) {
	}

	public void addMarkerEvent(RefactoringMarkerEvent event) {
		refactoringMarkerListeners.forEach(listener -> listener.update(event));
	}

	public void addMarkerListener(RefactoringMarkerListener listener) {
		this.refactoringMarkerListeners.add(listener);
	}

	public void clearMarkerListeners() {
		this.refactoringMarkerListeners.clear();
	}

	protected CommentRewriter getCommentRewriter() {
		return this.commentRewriter;
	}

	protected boolean isGeneratedNode(ASTNode node) {
		return GeneratedNodesUtil.findPropertyValue(node, "$isGenerated"); //$NON-NLS-1$
	}
}