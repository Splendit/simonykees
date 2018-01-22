package eu.jsparrow.core.visitor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ChildListPropertyDescriptor;
import org.eclipse.jdt.core.dom.Comment;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.StructuralPropertyDescriptor;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.jsparrow.core.util.ASTNodeUtil;

/**
 * Abstract implementation of an {@link ASTVisitor} to assure all used visitors
 * have an field for {@link ASTRewrite} to commit the changes in the tree.
 * 
 * @author Martin Huter, Hans-Jörg Schrödl, Matthias Webhofer, Ardit Ymeri
 * @since 0.9
 */

public abstract class AbstractASTRewriteASTVisitor extends ASTVisitor {

	private static final Logger logger = LoggerFactory.getLogger(AbstractASTRewriteASTVisitor.class);

	private static final String EMPTY_STRING = ""; //$NON-NLS-1$

	protected ASTRewrite astRewrite;

	protected String compilationUnitHandle;

	protected List<ASTRewriteVisitorListener> listeners = new ArrayList<>();

	protected String compilationUnitSource = EMPTY_STRING;

	private CompilationUnit compilationUnit;

	public AbstractASTRewriteASTVisitor() {
		super();
	}

	public AbstractASTRewriteASTVisitor(boolean visitDocTags) {
		super(visitDocTags);
	}

	@Override
	public boolean visit(CompilationUnit compilationUnit) {
		this.compilationUnit = compilationUnit;
		return true;
	}

	@Override
	public void endVisit(CompilationUnit compilationUnit) {
		resetCompilationUnitSource();
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
	protected void onRewrite() {
		listeners.forEach(listener -> listener.update(new ASTRewriteEvent(this.compilationUnitHandle)));
	}

	/**
	 * Finds the leading, the trailing and the internal comments of the given
	 * node.
	 * 
	 * @param node
	 *            an {@link ASTNode} to find the comments for.
	 * @return a list the found {@link Comment}s in the order that they occur.
	 */
	protected List<Comment> findRelatedComments(ASTNode node) {
		List<Comment> relatedComments = new ArrayList<>();
		relatedComments.addAll(findLeadingComments(node));
		relatedComments.addAll(findInternalComments(node));
		relatedComments.addAll(findTrailingComments(node));
		return relatedComments;
	}

	/**
	 * 
	 * @return the list of the comments in the compilation unit which is
	 *         currently being visited.
	 */
	protected List<Comment> getCompilationUnitComments() {
		return ASTNodeUtil.convertToTypedList(compilationUnit.getCommentList(), Comment.class);
	}

	/**
	 * Finds the list of comments whose starting position falls between the
	 * starting and ending position of the given node.
	 * 
	 * @param node
	 *            the {@link ASTNode} to find be checked for internal comments.
	 * @param comments
	 *            the list of all existing comments
	 * @return the list of internal comments of the node in the order that they
	 *         occur.
	 */
	protected List<Comment> findInternalComments(ASTNode node) {
		int nodeStartPos = node.getStartPosition();
		int nodeEndPos = nodeStartPos + node.getLength();
		List<Comment> comments = getCompilationUnitComments();

		return comments.stream()
			.filter(comment -> comment.getStartPosition() > nodeStartPos && comment.getStartPosition() < nodeEndPos)
			.collect(Collectors.toList());
	}

	/**
	 * Creates a new {@link Statement} with the given content and inserts it
	 * before the given node.
	 * 
	 * @param node
	 *            the node which will be proceeded by the comment.
	 * @param content
	 *            the contet of the comment to be inserted.
	 */
	protected void addComment(Statement node, String content) {

		StructuralPropertyDescriptor locationInParent = node.getLocationInParent();
		if(!locationInParent.isChildListProperty()) {
			return;
		}

		ListRewrite listRewrite = astRewrite.getListRewrite(node.getParent(),
				(ChildListPropertyDescriptor) locationInParent);
		Statement placeHolder = createPlaceHolder(content);
		listRewrite.insertBefore(placeHolder, node, null);
	}

	protected Statement createPlaceHolder(String content) {
		return (Statement) astRewrite.createStringPlaceholder(content, ASTNode.EMPTY_STATEMENT);
	}

	/**
	 * Find the content of the given comment from the {@link ICompilationUnit}. 
	 * 
	 * @param comment an node representing the comment.
	 * @return the formatted content of the comment
	 */
	protected String findCommentContent(Comment comment) {
		String source = readCompilationUnitSource();
		int start = comment.getStartPosition();
		int length = comment.getLength();
		int end = start + length;
		if (source.length() < end) {
			return EMPTY_STRING;
		}
		String content = source.substring(start, end);
		return content.replaceAll("\\t", EMPTY_STRING); //$NON-NLS-1$
	}

	/**
	 * A lazy read of the source of the {@link ICompilationUnit} being visitied.
	 * Note that the source of the {@link ICompilationUnit} contains also the
	 * comments, whereas the source of the {@link CompilationUnit} does not
	 * contain them.
	 * 
	 * @return the read content of the {@link ICompilationUnit}.
	 */
	protected String readCompilationUnitSource() {
		String source = getCompilationUnitSource();
		if (!source.isEmpty()) {
			return source;
		}

		try {
			source = ((ICompilationUnit) compilationUnit.getJavaElement()).getSource();
		} catch (JavaModelException e) {
			logger.error("Cannot read the source of the compilation unit", e); //$NON-NLS-1$
		}

		setCompilationUnitSource(source);

		return source;
	}

	private void setCompilationUnitSource(String source) {
		this.compilationUnitSource = source;
	}

	private void resetCompilationUnitSource() {
		this.compilationUnitSource = EMPTY_STRING;
	}

	private String getCompilationUnitSource() {
		return this.compilationUnitSource;
	}

	protected void saveRelatedComments(Statement statement) {
		saveRelatedComments(statement, statement);
	}

	/**
	 * Inserts a copy of the comments related to the given {@link ASTNode} 
	 * before the given {@link Statement}. 
	 * 
	 * @param node a node whose comments will be saved
	 * @param statement a statement which will be proceeded by the new comments. 
	 */
	protected void saveRelatedComments(ASTNode node, Statement statement) {
		List<Comment> comments = findRelatedComments(node);
		saveBeforeStatement(statement, comments);
	}

	/**
	 * Inserts a copy of the given comments before the given statement.
	 * 
	 * @param statement
	 *            the statement to be proceeded by the comments.
	 * @param comments
	 *            a list of comments to be inserted.
	 */
	protected void saveBeforeStatement(Statement statement, List<Comment> comments) {
		comments.stream()
			.map(this::findCommentContent)
			.forEach(content -> addComment(statement, content));
	}

	/**
	 * Creates a new statement and inserts it right before the given node. Uses
	 * the contents of the leading comments as the body of the new statement. If
	 * no leading comment is found, no new statement is created.
	 * 
	 * @param node
	 *            the node to check for leading comments.
	 */
	protected void saveLeadingComment(Statement node) {
		List<Comment> leadingComments = findLeadingComments(node);
		saveBeforeStatement(node, leadingComments);

	}

	protected List<Comment> findLeadingComments(ASTNode node) {
		List<Comment> leadingComments = new ArrayList<>();
		List<Comment> compilatinUnitComments = getCompilationUnitComments();
		CompilationUnit cu = getCompilationUnit();
		int leadingCommentIndex = cu.firstLeadingCommentIndex(node);
		if (leadingCommentIndex < 0) {
			return Collections.emptyList();
		}
		leadingComments.add(compilatinUnitComments.get(leadingCommentIndex));
		for (int i = leadingCommentIndex + 1; i < compilatinUnitComments.size(); i++) {
			Comment comment = compilatinUnitComments.get(i);
			if (comment.getStartPosition() < node.getStartPosition()) {
				leadingComments.add(comment);
			} else {
				break;
			}
		}
		return leadingComments;
	}
	
	protected List<Comment> findTrailingComments(ASTNode node) {
		CompilationUnit cu = getCompilationUnit();
		int trailCommentIndex = cu.lastTrailingCommentIndex(node);
		if (trailCommentIndex < 0) {
			return Collections.emptyList();
		}
		
		List<Comment> cuComments = getCompilationUnitComments();
		List<Comment> trailingComment = new ArrayList<>();
		trailingComment.add(cuComments.get(trailCommentIndex));
		int nodeEndPos = node.getStartPosition() + node.getLength();
		for(int i = trailCommentIndex-1; i>= 0 ; i--) {
			Comment comment = cuComments.get(i);
			if(comment.getStartPosition() > nodeEndPos) {
				trailingComment.add(comment);
			} else {
				break;
			}
		}
		
		return trailingComment;
	}
	
	protected List<Comment> findSurroundingComments(ASTNode node) {
		ASTNode parent = node.getParent();
		int parentStartPos = parent.getStartPosition();
		int parentEndPOs = parentStartPos + parent.getLength();
		int nodeStartPos = node.getStartPosition();
		int nodeEndPos = nodeStartPos + node.getLength();

		return getCompilationUnitComments().stream()
			.filter(comment -> {
				int startPos = comment.getStartPosition();
				return (startPos > parentStartPos && startPos < nodeStartPos)
						|| (startPos > nodeEndPos && startPos < parentEndPOs);
			})
			.collect(Collectors.toList());
	}
}