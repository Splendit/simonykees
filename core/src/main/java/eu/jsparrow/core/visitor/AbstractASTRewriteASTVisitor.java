package eu.jsparrow.core.visitor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.Comment;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Statement;
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

	protected List<Comment> findRelatedComments(ASTNode node) {
		List<Comment> comments = getCompilationUnitComments();
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

	public List<Comment> getCompilationUnitComments() {
		return ASTNodeUtil.convertToTypedList(compilationUnit.getCommentList(), Comment.class);
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
		if(block == null) {
			return;
		}
	
		ListRewrite listRewrite = astRewrite.getListRewrite(block, Block.STATEMENTS_PROPERTY);
		Statement placeHolder = (Statement) astRewrite.createStringPlaceholder(content,
				ASTNode.EMPTY_STATEMENT);
		listRewrite.insertBefore(placeHolder, node, null);
	}
	
	protected String findCommentContent(Comment comment) {
		String source = readCompilationUnitSource();
		int start = comment.getStartPosition();
		int length = comment.getLength();
		int end = start + length;
		if(source.length() < end) {
			return null;
		}
		
		return source.substring(start, end);
	}
	
	protected String readCompilationUnitSource() {
		String source = getCompilationUnitSource();
		if(!source.isEmpty()) {
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

	protected void saveRelatedComments(ASTNode node, Statement statement) {
		List<Comment> invocationComments = findRelatedComments(node);
		saveBeforeStatement(statement, invocationComments);
	}
	
	public void saveBeforeStatement(Statement statement, List<Comment> invocationComments) {
		invocationComments.stream()
			.map(this::findCommentContent)
			.forEach(content -> addComment(statement, content));
	}
	
	protected void saveLeadingComment(Statement node) {
		List<Comment> leadingComments = new ArrayList<>();
		List<Comment> compilatinUnitComments = getCompilationUnitComments();
		CompilationUnit cu = getCompilationUnit();
		int leadingCommentIndex = cu.firstLeadingCommentIndex(node);
		if(leadingCommentIndex >= 0) {
			leadingComments.add(compilatinUnitComments.get(leadingCommentIndex));
			saveBeforeStatement(node, leadingComments);
		}
	}
}