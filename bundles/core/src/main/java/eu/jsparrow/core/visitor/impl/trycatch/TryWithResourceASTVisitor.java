package eu.jsparrow.core.visitor.impl.trycatch;

import static eu.jsparrow.rules.common.util.ASTNodeUtil.convertToTypedList;
import static eu.jsparrow.rules.common.util.ASTNodeUtil.getSpecificAncestor;
import static java.util.stream.Collectors.toList;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.eclipse.jdt.core.dom.ASTMatcher;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.ChildListPropertyDescriptor;
import org.eclipse.jdt.core.dom.Comment;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.ITypeBinding;
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
import org.osgi.framework.Version;

import eu.jsparrow.rules.common.builder.NodeBuilder;
import eu.jsparrow.rules.common.util.ClassRelationUtil;
import eu.jsparrow.rules.common.util.JdtCoreVersionBindingUtil;
import eu.jsparrow.rules.common.visitor.AbstractASTRewriteASTVisitor;
import eu.jsparrow.rules.common.visitor.helper.CommentRewriter;

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

	@SuppressWarnings("unchecked")
	@Override
	public boolean visit(TryStatement node) {
		List<VariableDeclarationExpression> resourceList = new ArrayList<>();
		List<SimpleName> resourceNameList = new ArrayList<>();

		List<VariableDeclarationStatement> varDeclarationStatements = convertToTypedList(node.getBody()
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

			if(isGeneratedNode(varDeclStatmentNode.getType())) {				
				break;
			}

			List<VariableDeclarationFragment> fragments = convertToTypedList(varDeclStatmentNode.fragments(),
					VariableDeclarationFragment.class);

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

					List<Modifier> modifierList = convertToTypedList(varDeclStatmentNode.modifiers(), Modifier.class);
					Function<Modifier, Modifier> cloneModifier = modifier -> (Modifier) ASTNode
						.copySubtree(modifier.getAST(), modifier);

					variableDeclarationExpression.modifiers()
						.addAll(modifierList.stream()
							.map(cloneModifier)
							.collect(toList()));

					resourceList.add(variableDeclarationExpression);
					resourceNameList.add(variableDeclarationFragment.getName());

					CommentRewriter cRewriter = getCommentRewriter();
					if (numFragments > 1) {
						cRewriter.saveRelatedComments(variableDeclarationFragment, node);
						astRewrite.remove(variableDeclarationFragment, null);
						numFragments--;
					} else {
						cRewriter.saveRelatedComments(varDeclStatmentNode, node);
						astRewrite.remove(varDeclStatmentNode, null);
					}
				}
			}
		}

		if (!resourceList.isEmpty()) {
			replaceTryStatement(node, resourceList, resourceNameList, toBeMovedToResources);

		}
		return true;
	}

	private void replaceTryStatement(TryStatement node, List<VariableDeclarationExpression> resourceList,
			List<SimpleName> resourceNameList, List<VariableDeclarationFragment> toBeMovedToResources) {
		// remove all close operations on the found resources
		Function<SimpleName, MethodInvocation> mapper = simpleName -> NodeBuilder.newMethodInvocation(node.getAST(),
				(SimpleName) ASTNode.copySubtree(simpleName.getAST(), simpleName),
				NodeBuilder.newSimpleName(node.getAST(), CLOSE));

		List<MethodInvocation> closeInvocations = resourceNameList.stream()
			.map(mapper)
			.collect(toList());

		CommentRewriter cRewriter = getCommentRewriter();
		if (node.resources()
			.isEmpty() && resourceList.size() != 1) {

			TryStatement tryStatement = createNewTryStatement(node, resourceList, toBeMovedToResources,
					closeInvocations);

			astRewrite.replace(node, tryStatement, null);

		} else {
			Version version = JdtCoreVersionBindingUtil.findCurrentJDTVersion();
			ChildListPropertyDescriptor resourcesProperty = JdtCoreVersionBindingUtil.findTryWithResourcesProperty(version);
			ListRewrite listRewrite = astRewrite.getListRewrite(node, resourcesProperty);
			resourceList.forEach(iteratorNode -> listRewrite.insertLast(iteratorNode, null));
			TwrCloseStatementsASTVisitor visitor = new TwrCloseStatementsASTVisitor(closeInvocations);
			node.accept(visitor);
			List<Statement> invocations = visitor.getCloseInvocationStatements();
			invocations.forEach(invocation -> {
				cRewriter.saveRelatedComments(invocation);
				astRewrite.remove(invocation, null);
			});
		}
		onRewrite();
	}

	@SuppressWarnings("unchecked")
	private TryStatement createNewTryStatement(TryStatement node, List<VariableDeclarationExpression> resourceList,
			List<VariableDeclarationFragment> toBeMovedToResources, List<MethodInvocation> closeInvocations) {

		CommentRewriter commentRewriter = getCommentRewriter();

		TryStatement tryStatement = getASTRewrite().getAST()
			.newTryStatement();
		tryStatement.resources()
			.addAll(resourceList);
		Map<Integer, List<Comment>> comments = findBodyComments(node, toBeMovedToResources, closeInvocations);
		Block newBody = (Block) ASTNode.copySubtree(node.getAST(), node.getBody());

		TwrRemoveNodesASTVisitor visitor = new TwrRemoveNodesASTVisitor(toBeMovedToResources, closeInvocations);
		newBody.accept(visitor);

		List<Statement> newBodyStatements = convertToTypedList(newBody.statements(), Statement.class);
		comments.forEach((key, value) -> {
			int newBodySize = newBodyStatements.size();
			if (newBodySize > key) {
				Statement statement = newBodyStatements.get(key);
				commentRewriter.saveBeforeStatement(statement, value);
			} else if (!newBodyStatements.isEmpty()) {
				Statement statement = newBodyStatements.get(newBodySize - 1);
				commentRewriter.saveAfterStatement(statement, value);
			} else {
				commentRewriter.saveBeforeStatement(node, value);
			}
		});

		List<CatchClause> newCatchClauses = convertToTypedList(node.catchClauses(), CatchClause.class).stream()
			.map(clause -> (CatchClause) ASTNode.copySubtree(node.getAST(), clause))
			.collect(toList());

		tryStatement.setBody(newBody);
		tryStatement.catchClauses()
			.addAll(newCatchClauses);
		tryStatement.setFinally((Block) ASTNode.copySubtree(node.getAST(), node.getFinally()));

		return tryStatement;
	}

	/**
	 * Finds the comments in the body of the {@link TryStatement} and their
	 * corresponding position in the {@link TryStatement} where the resource
	 * declarations and the close invocations are removed.
	 * 
	 * @param node
	 *            a node representin a {@link TryStatement} to be checked
	 * @param toBeMovedToResources
	 *            the list of resoruce declarations that will be removed from
	 *            the body of the {@link TryStatement}
	 * @param closeInvocations
	 *            the list of the {@link Closeable#close()} invocations to be
	 *            removed.
	 * @return a map from position to the list of comments to be placed in the
	 *         new {@link TryStatement}.
	 */
	private Map<Integer, List<Comment>> findBodyComments(TryStatement node,
			List<VariableDeclarationFragment> toBeMovedToResources, List<MethodInvocation> closeInvocations) {
		List<Statement> stmsToBeRemoved = new ArrayList<>();
		stmsToBeRemoved.addAll(toBeMovedToResources.stream()
			.map(methodInvocation -> getSpecificAncestor(methodInvocation, Statement.class))
			.collect(toList()));
		Block body = node.getBody();
		Map<Integer, List<Comment>> bodyComments = new HashMap<>();
		CommentRewriter commentRewriter = getCommentRewriter();
		List<Statement> statements = convertToTypedList(body.statements(), Statement.class);
		List<Comment> connectedComments = new ArrayList<>();
		List<Comment> skippedComments = new ArrayList<>();
		int key = 0;
		for (Statement statement : statements) {
			List<Comment> ithStatementComments = commentRewriter.findRelatedComments(statement);
			if (!stmsToBeRemoved.contains(statement)) {
				if (bodyComments.containsKey(key)) {
					ithStatementComments.addAll(0, bodyComments.get(key));
				}
				if (!ithStatementComments.isEmpty()) {
					bodyComments.put(key, ithStatementComments);
					connectedComments.addAll(ithStatementComments);
				}
				if (!isCloseInvocation(statement, closeInvocations)) {
					key++;
				}
			} else {
				skippedComments.addAll(ithStatementComments);
			}
		}

		/*
		 * comments in the body that are not related with any node.
		 */
		List<Comment> unconnected = commentRewriter.findRelatedComments(body);

		unconnected.removeAll(connectedComments);
		unconnected.removeAll(skippedComments);
		putUnconnectedComments(bodyComments, unconnected);
		return bodyComments;
	}

	private boolean isCloseInvocation(Statement statement, List<MethodInvocation> closeInvocations) {
		if (ASTNode.EXPRESSION_STATEMENT != statement.getNodeType()) {
			return false;
		}
		ExpressionStatement expressionStatement = (ExpressionStatement) statement;
		Expression expression = expressionStatement.getExpression();
		if (ASTNode.METHOD_INVOCATION != expression.getNodeType()) {
			return false;
		}

		ASTMatcher matcher = new ASTMatcher();

		MethodInvocation methodInvocation = (MethodInvocation) expression;
		return closeInvocations.stream()
			.anyMatch(closeInvocation -> matcher.match(methodInvocation, closeInvocation));
	}

	/**
	 * Finds the position of the comments that are not connected to any node
	 * based on the position in the compilation unit of the connected and
	 * unconnected body comments.
	 * 
	 * @param bodyComments
	 *            a map of the connected body comments
	 * @param unconnectedComments
	 *            the list of unconnected body comments.
	 */
	private void putUnconnectedComments(Map<Integer, List<Comment>> bodyComments, List<Comment> unconnectedComments) {
		List<Integer> keySet = new ArrayList<>(bodyComments.keySet());
		if (keySet.isEmpty()) {
			return;
		}

		if (keySet.size() == 1) {
			int key = keySet.get(0);
			List<Comment> commentList = bodyComments.get(key);
			commentList.addAll(unconnectedComments);
			unconnectedComments.clear();
			return;
		}
		int index = 0;

		for (Comment comment : unconnectedComments) {
			int startPos = comment.getStartPosition();
			Integer key = keySet.get(index);
			List<Comment> commentList = bodyComments.get(key);
			while (commentList.get(0)
				.getStartPosition() < startPos && index < keySet.size()) {
				index++;
				key = keySet.get(index);
				commentList = bodyComments.get(key);
			}

			if (index >= keySet.size()) {
				break;
			}

			commentList.add(0, comment);
			unconnectedComments.remove(comment);
		}

		if (unconnectedComments.isEmpty()) {
			return;
		}

		Integer newKey = keySet.get(keySet.size() - 1) + 1;
		bodyComments.put(newKey, unconnectedComments);
	}
}
