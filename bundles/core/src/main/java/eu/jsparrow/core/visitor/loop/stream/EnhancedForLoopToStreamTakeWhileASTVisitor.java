package eu.jsparrow.core.visitor.loop.stream;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.Comment;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.LambdaExpression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.Type;

import eu.jsparrow.core.visitor.sub.FlowBreakersVisitor;
import eu.jsparrow.core.visitor.sub.UnhandledExceptionVisitor;
import eu.jsparrow.rules.common.builder.NodeBuilder;
import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.util.ClassRelationUtil;
import eu.jsparrow.rules.common.util.OperatorUtil;
import eu.jsparrow.rules.common.visitor.helper.CommentRewriter;
import eu.jsparrow.rules.common.visitor.helper.LocalVariableUsagesVisitor;

/**
 * A visitor for replacing for-loops with {@code Stream::takeWhile}.
 * 
 * @since 3.7.0
 *
 */
public class EnhancedForLoopToStreamTakeWhileASTVisitor extends AbstractEnhancedForLoopToStreamASTVisitor {

	@Override
	public boolean visit(EnhancedForStatement enhancedForStatement) {
		if (isConditionalExpression(enhancedForStatement.getExpression())) {
			return true;
		}

		SingleVariableDeclaration loopParameter = enhancedForStatement.getParameter();
		Type parameterType = loopParameter.getType();
		if (isGeneratedNode(parameterType)) {
			return true;
		}
		Expression loopExpression = enhancedForStatement.getExpression();
		ITypeBinding parameterTypeBinding = parameterType.resolveBinding();
		ITypeBinding expressionBinding = loopExpression.resolveTypeBinding();

		if (parameterTypeBinding == null || expressionBinding == null) {
			return true;
		}

		if (!ClassRelationUtil.isContentOfType(expressionBinding, java.util.Collection.class.getName())
				&& !ClassRelationUtil.isInheritingContentOfTypes(expressionBinding,
						Collections.singletonList(java.util.Collection.class.getName()))) {
			return true;
		}

		if (!isTypeSafe(parameterTypeBinding) || !isTypeSafe(expressionBinding)) {
			return true;
		}

		Statement bodyStatement = enhancedForStatement.getBody();
		if (bodyStatement.getNodeType() != ASTNode.BLOCK) {
			return true;
		}

		Block body = (Block) bodyStatement;

		if (containsNonEffectivelyFinalVariable(body)) {
			return true;
		}

		List<Statement> bodyStatements = ASTNodeUtil.convertToTypedList(body.statements(), Statement.class);
		if (bodyStatements.size() <= 1) {
			return true;
		}

		Statement firstStatement = bodyStatements.get(0);
		if (!isIfStatementWithBreakBody(firstStatement)) {
			return true;
		}

		IfStatement ifStatement = (IfStatement) firstStatement;
		if (!UnhandledExceptionVisitor.analyzeExceptionHandling(firstStatement, ifStatement)) {
			return true;
		}

		List<Statement> statementsAfterIf = bodyStatements.subList(1, bodyStatements.size());
		if (containsFlowBreakerStatements(statementsAfterIf)) {
			return true;
		}

		if (containsUnhandledException(statementsAfterIf, body)) {
			return true;
		}

		if (containsReferenceTo(body, loopExpression) || loopExpression.getNodeType() == ASTNode.QUALIFIED_NAME) {
			return false;
		}

		ExpressionStatement streamStatement = createStreamStatement(loopParameter.getName(), loopExpression,
				ifStatement, body);
		astRewrite.replace(enhancedForStatement, streamStatement, null);
		saveComments(enhancedForStatement, loopExpression, ifStatement);
		addMarkerEvent(enhancedForStatement);
		onRewrite();

		return true;
	}

	private boolean containsUnhandledException(List<Statement> statements, Block excludedAncestor) {
		for (Statement statement : statements) {
			if (!UnhandledExceptionVisitor.analyzeExceptionHandling(statement, excludedAncestor)) {
				return true;
			}
		}
		return false;
	}

	private void saveComments(EnhancedForStatement enhancedForStatement, Expression loopExpression,
			IfStatement ifStatement) {
		CommentRewriter commentRewriter = getCommentRewriter();
		List<Comment> allComments = commentRewriter.findRelatedComments(enhancedForStatement);
		List<Comment> loopExpressionComments = commentRewriter.findRelatedComments(loopExpression);
		List<Comment> conditionComments = commentRewriter.findRelatedComments(ifStatement.getExpression());
		Block body = (Block) enhancedForStatement.getBody();
		List<Comment> copiedStatementComments = ASTNodeUtil.convertToTypedList(body.statements(), Statement.class)
			.stream()
			.skip(1)
			.map(commentRewriter::findRelatedComments)
			.flatMap(List::stream)
			.collect(Collectors.toList());
		allComments.removeAll(loopExpressionComments);
		allComments.removeAll(conditionComments);
		allComments.removeAll(copiedStatementComments);
		commentRewriter.saveBeforeStatement(enhancedForStatement, allComments);
	}

	private boolean containsReferenceTo(Block body, Expression loopExpression) {
		if (loopExpression.getNodeType() != ASTNode.SIMPLE_NAME) {
			return false;
		}

		SimpleName simpleName = (SimpleName) loopExpression;
		LocalVariableUsagesVisitor visitor = new LocalVariableUsagesVisitor(simpleName);
		body.accept(visitor);
		return !visitor.getUsages()
			.isEmpty();
	}

	private boolean containsFlowBreakerStatements(List<Statement> statements) {
		for (Statement statement : statements) {
			if (FlowBreakersVisitor.containsFlowControlStatement(statement)) {
				return true;
			}
		}
		return false;
	}

	@SuppressWarnings("unchecked")
	private ExpressionStatement createStreamStatement(SimpleName parameter, Expression loopExpression,
			IfStatement ifStatement, Block loopBody) {
		AST ast = astRewrite.getAST();
		MethodInvocation stream = ast.newMethodInvocation();
		stream.setName(ast.newSimpleName("stream")); //$NON-NLS-1$
		stream.setExpression((Expression) astRewrite.createCopyTarget(loopExpression));
		MethodInvocation takeWhile = ast.newMethodInvocation();
		takeWhile.setName(ast.newSimpleName("takeWhile")); //$NON-NLS-1$
		takeWhile.setExpression(stream);

		Expression negatedCondition = OperatorUtil.createNegatedExpression(ifStatement.getExpression(), astRewrite);
		LambdaExpression takeWhilePredicate = NodeBuilder.newLambdaExpression(ast, negatedCondition,
				parameter.getIdentifier());
		takeWhile.arguments()
			.add(takeWhilePredicate);

		MethodInvocation forEach = ast.newMethodInvocation();
		forEach.setExpression(takeWhile);
		forEach.setName(ast.newSimpleName("forEach")); //$NON-NLS-1$
		Statement forEachLambdaBody = createForEachLambdaBody(loopBody);
		LambdaExpression forEachBody = NodeBuilder.newLambdaExpression(ast, forEachLambdaBody,
				parameter.getIdentifier());
		forEach.arguments()
			.add(forEachBody);
		return ast.newExpressionStatement(forEach);
	}

	@SuppressWarnings("unchecked")
	private Statement createForEachLambdaBody(Block loopBody) {
		AST ast = loopBody.getAST();
		List<Statement> originalBodyStatements = ASTNodeUtil.convertToTypedList(loopBody.statements(), Statement.class);
		// drop the if statement
		originalBodyStatements.remove(0);

		if (originalBodyStatements.size() > 1) {
			Block newBlock = ast.newBlock();
			for (Statement statement : originalBodyStatements) {
				newBlock.statements()
					.add((Statement) astRewrite.createMoveTarget(statement));
			}
			return newBlock;
		}

		return (Statement) astRewrite.createMoveTarget(originalBodyStatements.get(0));
	}

	private boolean isIfStatementWithBreakBody(Statement statement) {
		if (statement.getNodeType() != ASTNode.IF_STATEMENT) {
			return false;
		}

		IfStatement ifStatement = (IfStatement) statement;
		Statement elseStatement = ifStatement.getElseStatement();
		if (elseStatement != null) {
			return false;
		}

		Statement thenStatement = ifStatement.getThenStatement();
		if (thenStatement.getNodeType() == ASTNode.BLOCK) {
			Block ifBody = (Block) thenStatement;
			List<Statement> ifBodyStatements = ASTNodeUtil.convertToTypedList(ifBody.statements(), Statement.class);
			if (ifBodyStatements.size() != 1) {
				return false;
			}
			Statement singleBodyStatement = ifBodyStatements.get(0);
			return singleBodyStatement.getNodeType() == ASTNode.BREAK_STATEMENT;
		}
		return thenStatement.getNodeType() == ASTNode.BREAK_STATEMENT;
	}

}
