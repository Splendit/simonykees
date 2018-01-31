package eu.jsparrow.core.visitor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
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
 * A helper class for writing comments which can be lost after the refactoring
 * process.
 * 
 * @author Ardit Ymeri
 * @since 2.4.2
 *
 */
public class CommentRewriter {

	private static final Logger logger = LoggerFactory.getLogger(CommentRewriter.class);

	private CompilationUnit compilationUnit;
	private static final String EMPTY_STRING = ""; //$NON-NLS-1$
	private String compilationUnitSource;
	private ASTRewrite astRewrite;

	void initCommentHelper(CompilationUnit compilationUnit, ASTRewrite astRewrite) {
		resetCompilationUnitSource();
		setCompilationUnit(compilationUnit);
		setASTRewrite(astRewrite);
	}

	void resetCommentHelper() {
		resetCompilationUnitSource();
	}

	/**
	 * Finds the leading, the trailing and the internal comments of the given
	 * node.
	 * 
	 * @param node
	 *            an {@link ASTNode} to find the comments for.
	 * @return a list the found {@link Comment}s in the order that they occur.
	 */
	public List<Comment> findRelatedComments(ASTNode node) {
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
	private List<Comment> getCompilationUnitComments() {
		return ASTNodeUtil.convertToTypedList(getCompilationUnit().getCommentList(), Comment.class);
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
	public List<Comment> findInternalComments(ASTNode node) {
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
	 *            the node which will be preceded by the comment.
	 * @param content
	 *            the contet of the comment to be inserted.
	 */
	private void addComment(Statement node, String content) {

		StructuralPropertyDescriptor locationInParent = node.getLocationInParent();
		if (!locationInParent.isChildListProperty()) {
			return;
		}

		ListRewrite listRewrite = astRewrite.getListRewrite(node.getParent(),
				(ChildListPropertyDescriptor) locationInParent);
		Statement placeHolder = createPlaceHolder(content);
		listRewrite.insertBefore(placeHolder, node, null);
	}

	private Statement createPlaceHolder(String content) {
		return (Statement) astRewrite.createStringPlaceholder(content, ASTNode.EMPTY_STATEMENT);
	}

	/**
	 * Find the content of the given comment from the {@link ICompilationUnit}.
	 * 
	 * @param comment
	 *            an node representing the comment.
	 * @return the formatted content of the comment
	 */
	private String findCommentContent(Comment comment) {
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
	 * A lazy read of the source of the {@link ICompilationUnit} being visited.
	 * Note that the source of the {@link ICompilationUnit} contains also the
	 * comments, whereas the source of the {@link CompilationUnit} does not
	 * contain them.
	 * 
	 * @return the read content of the {@link ICompilationUnit}.
	 */
	private String readCompilationUnitSource() {
		String source = getCompilationUnitSource();
		if (!source.isEmpty()) {
			return source;
		}
		try {
			source = ((ICompilationUnit) getCompilationUnit().getJavaElement()).getSource();
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
		setCompilationUnitSource(EMPTY_STRING);
	}

	private String getCompilationUnitSource() {
		return this.compilationUnitSource;
	}

	/**
	 * Inserts a copy of the comments related to the given {@link Statement}
	 * above itself
	 * 
	 * @param statement
	 *            a {@link Statement} to be checked for comments.
	 */
	public void saveRelatedComments(Statement statement) {
		saveRelatedComments(statement, statement);
	}

	/**
	 * Inserts a copy of the comments related to the given {@link ASTNode}
	 * before the given {@link Statement}.
	 * 
	 * @param node
	 *            a node whose comments will be saved.
	 * @param statement
	 *            a statement which will be preceded by the new comments.
	 */
	public void saveRelatedComments(ASTNode node, Statement statement) {
		List<Comment> comments = findRelatedComments(node);
		saveBeforeStatement(statement, comments);
	}

	/**
	 * Inserts the comments related to the given node before its first parent of
	 * type {@link Statement}.
	 * 
	 * @param node
	 *            an {@link ASTNode} to be checked for comments.
	 */
	public void saveCommentsInParentStatement(ASTNode node) {
		Statement statement = ASTNodeUtil.getSpecificAncestor(node, Statement.class);
		saveRelatedComments(node, statement);
	}

	/**
	 * Inserts a copy of the given comments before the given statement.
	 * 
	 * @param statement
	 *            the statement to be preceded by the comments.
	 * @param comments
	 *            a list of comments to be inserted.
	 */
	public void saveBeforeStatement(Statement statement, List<Comment> comments) {
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
	public void saveLeadingComment(Statement node) {
		List<Comment> leadingComments = findLeadingComments(node);
		saveBeforeStatement(node, leadingComments);

	}

	/**
	 * Finds the list of comments that are preceding the given node.
	 * 
	 * @param node
	 *            a node on the current compilation unit being visited
	 * @return list of the leading comments, i.e. the comments which are
	 *         immediatelly preceding the node.
	 */
	public List<Comment> findLeadingComments(ASTNode node) {
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

	/**
	 * Finds the list of comments that are succeeding the given node.
	 * 
	 * @param node
	 *            a node on the current compilation unit being visited
	 * @return list of the trailing comments, i.e. the comments which are
	 *         immediatelly succedding the node.
	 */
	public List<Comment> findTrailingComments(ASTNode node) {
		CompilationUnit cu = getCompilationUnit();
		int trailCommentIndex = cu.lastTrailingCommentIndex(node);
		if (trailCommentIndex < 0) {
			return Collections.emptyList();
		}

		List<Comment> cuComments = getCompilationUnitComments();
		List<Comment> trailingComment = new ArrayList<>();
		trailingComment.add(cuComments.get(trailCommentIndex));
		int nodeEndPos = node.getStartPosition() + node.getLength();
		for (int i = trailCommentIndex - 1; i >= 0; i--) {
			Comment comment = cuComments.get(i);
			if (comment.getStartPosition() > nodeEndPos) {
				trailingComment.add(comment);
			} else {
				break;
			}
		}

		return trailingComment;
	}

	/**
	 * Finds the list of comments that are placed in between the given node and
	 * its parent.
	 * 
	 * @param node
	 *            a node in the current compilation unit being visited.
	 * @return list of comments that do not fall in the given node but inside
	 *         its parent.
	 */
	public List<Comment> findSurroundingComments(ASTNode node) {
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

	private CompilationUnit getCompilationUnit() {
		return compilationUnit;
	}

	private void setCompilationUnit(CompilationUnit compilationUnit) {
		this.compilationUnit = compilationUnit;
	}

	private void setASTRewrite(ASTRewrite astRewrite) {
		this.astRewrite = astRewrite;
	}

	public void saveCommentsInBlock(Block reference, List<Comment> relatedComments) {
		List<String> referenceBlockComments = findRelatedComments(reference).stream()
			.map(this::findCommentContent)
			.collect(Collectors.toList());

		List<Statement> filteredComments = relatedComments.stream()
			.map(this::findCommentContent)
			.distinct()
			.filter(comment -> !referenceBlockComments.contains(comment))
			.map(this::createPlaceHolder)
			.collect(Collectors.toList());

		ListRewrite listRewrite = astRewrite.getListRewrite(reference, Block.STATEMENTS_PROPERTY);
		filteredComments.forEach(comment -> listRewrite.insertFirst(comment, null));
	}

	public boolean isTrailing(Comment comment, ASTNode node) {
		List<Comment> comments = getCompilationUnitComments();
		CompilationUnit cu = getCompilationUnit();
		int lastCommentIndex = cu.lastTrailingCommentIndex(node);
		if (lastCommentIndex < 0) {
			return false;

		}
		Comment trailingComment = comments.get(lastCommentIndex);
		return trailingComment == comment;
	}
}
