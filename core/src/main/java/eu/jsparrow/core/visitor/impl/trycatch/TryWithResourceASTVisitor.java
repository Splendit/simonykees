package eu.jsparrow.core.visitor.impl.trycatch;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.Comment;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.LineComment;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;

import eu.jsparrow.core.builder.NodeBuilder;
import eu.jsparrow.core.util.ASTNodeUtil;
import eu.jsparrow.core.util.ClassRelationUtil;
import eu.jsparrow.core.visitor.AbstractASTRewriteASTVisitor;
import eu.jsparrow.core.visitor.CommentsASTVisitor;

/**
 * The {@link TryWithResourceASTVisitor} is used to find resources in an
 * Try-Block and moves it to the resource-head of try. A resource is a source
 * that implements {@link Closeable} or {@link AutoCloseable}
 * 
 * @author Martin Huter, Ardit Ymeri
 * @since 0.9
 */

public class TryWithResourceASTVisitor extends AbstractASTRewriteASTVisitor {

	private static final String AUTO_CLOSEABLE_FULLY_QUALIFIED_NAME = java.lang.AutoCloseable.class.getName();
	private static final String CLOSEABLE_FULLY_QUALIFIED_NAME = java.io.Closeable.class.getName();
	static final String CLOSE = "close"; //$NON-NLS-1$
	private CompilationUnit compilationUnit;
	private List<Comment> comments;

	@Override
	public boolean visit(CompilationUnit cu) {
		this.compilationUnit = cu;
		comments = ASTNodeUtil.convertToTypedList(cu.getCommentList(), Comment.class);

		return true;
	}

	// TODO improvement for suppressed deprecation needed, see SIM-878
	@SuppressWarnings("unchecked")
	@Override
	public boolean visit(TryStatement node) {
		List<VariableDeclarationExpression> resourceList = new ArrayList<>();
		List<SimpleName> resourceNameList = new ArrayList<>();

		List<VariableDeclarationStatement> varDeclarationStatements = ASTNodeUtil.convertToTypedList(node.getBody()
			.statements(), VariableDeclarationStatement.class);

		List<VariableDeclarationFragment> toBeMovedToResources = new ArrayList<>();

		for (VariableDeclarationStatement varDeclStatmentNode : varDeclarationStatements) {
			/*
			 * Move all AutoCloseable Object to resource header, stop collection
			 * after first non resource object
			 */
			ITypeBinding typeBind = varDeclStatmentNode.getType()
				.resolveBinding();
			if (!ClassRelationUtil.isInheritingContentOfTypes(typeBind, generateFullyQualifiedNameList(
					AUTO_CLOSEABLE_FULLY_QUALIFIED_NAME, CLOSEABLE_FULLY_QUALIFIED_NAME))) {
				break;
			}

			List<VariableDeclarationFragment> fragments = ASTNodeUtil
				.convertToTypedList(varDeclStatmentNode.fragments(), VariableDeclarationFragment.class);

			int numFragments = fragments.size();

			for (VariableDeclarationFragment variableDeclarationFragment : fragments) {

				SimpleName varName = variableDeclarationFragment.getName();

				TwrPreconditionASTVisitor visitor = new TwrPreconditionASTVisitor(varName, toBeMovedToResources);
				node.accept(visitor);

				if (variableDeclarationFragment.getInitializer() != null && visitor.safeToGo()) {

					toBeMovedToResources.add(variableDeclarationFragment);
					VariableDeclarationExpression variableDeclarationExpression = varDeclStatmentNode.getAST()
						.newVariableDeclarationExpression((VariableDeclarationFragment) ASTNode
							.copySubtree(variableDeclarationFragment.getAST(), variableDeclarationFragment));
					variableDeclarationExpression.setType(
							(Type) ASTNode.copySubtree(varDeclStatmentNode.getAST(), varDeclStatmentNode.getType()));

					List<Modifier> modifierList = ASTNodeUtil.convertToTypedList(varDeclStatmentNode.modifiers(),
							Modifier.class);
					Function<Modifier, Modifier> cloneModifier = modifier -> (Modifier) ASTNode
						.copySubtree(modifier.getAST(), modifier);

					variableDeclarationExpression.modifiers()
						.addAll(modifierList.stream()
							.map(cloneModifier)
							.collect(Collectors.toList()));

					resourceList.add(variableDeclarationExpression);
					resourceNameList.add(variableDeclarationFragment.getName());
					List<Comment> relatedComments;

					if (numFragments > 1) {
						astRewrite.remove(variableDeclarationFragment, null);
						relatedComments = findComments(variableDeclarationFragment);
						numFragments--;
					} else {
						astRewrite.remove(varDeclStatmentNode, null);
						relatedComments = findComments(varDeclStatmentNode);
					}
					
					CommentsASTVisitor commentsVisitor = new CommentsASTVisitor();
					commentsVisitor.parseSource(compilationUnit);
					relatedComments.forEach(comment -> comment.accept(commentsVisitor));
					commentsVisitor.getLineComments().forEach((key, value) -> addLineComment(node, value));
					commentsVisitor.getBlockComments().forEach((key, value) -> addBlockComment(node, value));
					
				}
			}
		}

		if (!resourceList.isEmpty()) {
			replaceTryStatement(node, resourceList, resourceNameList, toBeMovedToResources);

		}
		return true;
	}

	private void addBlockComment(TryStatement node, String value) {
		Block block = ASTNodeUtil.getSpecificAncestor(node, Block.class);
		
		value = StringUtils.replace(value, "\\n\\t", "\\n");
		ListRewrite listRewrite = astRewrite.getListRewrite(block, Block.STATEMENTS_PROPERTY);
		Statement placeHolder = (Statement) astRewrite.createStringPlaceholder(value,
				ASTNode.EMPTY_STATEMENT);
		listRewrite.insertBefore(placeHolder, node, null);
	}
	
	private void addLineComment(TryStatement node, String content) {

		Block block = ASTNodeUtil.getSpecificAncestor(node, Block.class);

		ListRewrite listRewrite = astRewrite.getListRewrite(block, Block.STATEMENTS_PROPERTY);
		Statement placeHolder = (Statement) astRewrite.createStringPlaceholder(content,
				ASTNode.EMPTY_STATEMENT);
		listRewrite.insertBefore(placeHolder, node, null);
	}

	private List<Comment> findComments(ASTNode node) {
		List<Comment> relatedComments = new ArrayList<>();
		relatedComments.addAll(findInternalComments(node));
		int leadingCommentIndex = this.compilationUnit.firstLeadingCommentIndex(node);
		if (leadingCommentIndex >= 0) {
			relatedComments.add(0, comments.get(leadingCommentIndex));
		}
		
		int trailCommentIndex = this.compilationUnit.lastTrailingCommentIndex(node);
		if (trailCommentIndex >= 0) {
			relatedComments.add(comments.get(trailCommentIndex));
		}

		return relatedComments;
	}

	private Collection<? extends Comment> findInternalComments(ASTNode node) {
		int nodeStartPos = node.getStartPosition();
		int nodeEndPos = nodeStartPos + node.getLength();

		return comments.stream()
			.filter(comment -> comment.getStartPosition() > nodeStartPos && comment.getStartPosition() < nodeEndPos)
			.collect(Collectors.toList());
	}

	@SuppressWarnings("deprecation")
	private void replaceTryStatement(TryStatement node, List<VariableDeclarationExpression> resourceList,
			List<SimpleName> resourceNameList, List<VariableDeclarationFragment> toBeMovedToResources) {
		// remove all close operations on the found resources
		Function<SimpleName, MethodInvocation> mapper = simpleName -> NodeBuilder.newMethodInvocation(node.getAST(),
				(SimpleName) ASTNode.copySubtree(simpleName.getAST(), simpleName),
				NodeBuilder.newSimpleName(node.getAST(), CLOSE));

		List<MethodInvocation> closeInvocations = resourceNameList.stream()
			.map(mapper)
			.collect(Collectors.toList());

		if (node.resources()
			.isEmpty() && resourceList.size() != 1) {

			TryStatement tryStatement = createNewTryStatement(node, resourceList, toBeMovedToResources,
					closeInvocations);

			astRewrite.replace(node, tryStatement, null);
			// remove all close operations on the found resources

		} else {
			ListRewrite listRewrite = astRewrite.getListRewrite(node, TryStatement.RESOURCES_PROPERTY);
			resourceList.forEach(iteratorNode -> listRewrite.insertLast(iteratorNode, null));
			node.accept(new TwrRemoveCloseASTVisitor(astRewrite, closeInvocations));
		}

		onRewrite();
	}

	@SuppressWarnings("unchecked")
	private TryStatement createNewTryStatement(TryStatement node, List<VariableDeclarationExpression> resourceList,
			List<VariableDeclarationFragment> toBeMovedToResources, List<MethodInvocation> closeInvocations) {

		TryStatement tryStatement = getASTRewrite().getAST()
			.newTryStatement();
		tryStatement.resources()
			.addAll(resourceList);
		Block newBody = (Block) ASTNode.copySubtree(node.getAST(), node.getBody());
		TwrRemoveNodesASTVisitor visitor = new TwrRemoveNodesASTVisitor(toBeMovedToResources, closeInvocations);
		newBody.accept(visitor);

		List<CatchClause> newCatchClauses = ASTNodeUtil.convertToTypedList(node.catchClauses(), CatchClause.class)
			.stream()
			.map(clause -> (CatchClause) ASTNode.copySubtree(node.getAST(), clause))
			.collect(Collectors.toList());

		tryStatement.setBody(newBody);
		tryStatement.catchClauses()
			.addAll(newCatchClauses);
		tryStatement.setFinally((Block) ASTNode.copySubtree(node.getAST(), node.getFinally()));

		return tryStatement;
	}
}
