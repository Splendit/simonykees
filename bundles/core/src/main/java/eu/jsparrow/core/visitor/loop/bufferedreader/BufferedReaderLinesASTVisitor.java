package eu.jsparrow.core.visitor.loop.bufferedreader;

import static eu.jsparrow.rules.common.builder.NodeBuilder.newExpressionStatement;
import static eu.jsparrow.rules.common.builder.NodeBuilder.newLambdaExpression;
import static eu.jsparrow.rules.common.builder.NodeBuilder.newMethodInvocation;
import static eu.jsparrow.rules.common.builder.NodeBuilder.newSimpleName;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.Comment;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.LambdaExpression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.WhileStatement;

import eu.jsparrow.core.markers.common.BufferedReaderLinesEvent;
import eu.jsparrow.core.visitor.sub.ExternalNonEffectivelyFinalReferencesVisitor;
import eu.jsparrow.core.visitor.sub.FlowBreakersVisitor;
import eu.jsparrow.core.visitor.sub.UnhandledExceptionVisitor;
import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.util.ClassRelationUtil;
import eu.jsparrow.rules.common.visitor.AbstractASTRewriteASTVisitor;
import eu.jsparrow.rules.common.visitor.helper.CommentRewriter;

/**
 * An {@link AbstractASTRewriteASTVisitor} for replacing loops iterating over
 * the lines of a file through a {@link java.io.BufferedReader} by a stream
 * generated from {@link java.io.BufferedReader#lines}
 * <p/>
 * For example, the following:
 * 
 * <pre>
 * <code>
 * String line;
 * while((line = bufferedReader.readLine()) != null) {
 * 	print(line);
 * }
 * </code>
 * </pre>
 * 
 * becomes:
 * 
 * <pre>
 * <code>
 * bufferedReader.lines().forEach(line -> {
 * 	print(line);
 * });
 * </code>
 * </pre>
 * 
 * @since 3.3.0
 *
 */
public class BufferedReaderLinesASTVisitor extends AbstractASTRewriteASTVisitor implements BufferedReaderLinesEvent {

	@Override
	public boolean visit(WhileStatement whileLoop) {
		Statement body = whileLoop.getBody();
		Expression expression = whileLoop.getExpression();
		analyseLoop(whileLoop, expression, body);

		return true;
	}

	@Override
	public boolean visit(ForStatement forLoop) {
		Statement body = forLoop.getBody();
		Expression expression = forLoop.getExpression();
		if (expression == null) {
			return true;
		}
		if (!forLoop.updaters()
			.isEmpty()) {
			return true;
		}
		analyseLoop(forLoop, expression, body);
		return true;
	}

	private void analyseLoop(Statement loop, Expression loopExpression, Statement body) {
		Assignment readLineAssignment = verifyExpressionPrecondition(loopExpression).orElse(null);
		if (readLineAssignment == null) {
			return;
		}
		SimpleName lineName = (SimpleName) readLineAssignment.getLeftHandSide();
		MethodInvocation readLine = (MethodInvocation) readLineAssignment.getRightHandSide();
		SimpleName bufferName = (SimpleName) readLine.getExpression();

		BufferedReaderLinesPreconditionVisitor preconditionVisitor = new BufferedReaderLinesPreconditionVisitor(loop,
				lineName, bufferName);

		ASTNode ancestor = findLoopAncestor(loop);
		ancestor.accept(preconditionVisitor);
		if (!preconditionVisitor.isSatisfied()) {
			return;
		}

		VariableDeclarationFragment lineDeclaration = preconditionVisitor.getLineDeclaration();
		if (lineDeclaration.getLocationInParent() != VariableDeclarationStatement.FRAGMENTS_PROPERTY &&
				lineDeclaration.getLocationInParent() != VariableDeclarationExpression.FRAGMENTS_PROPERTY) {
			return;
		}

		ExternalNonEffectivelyFinalReferencesVisitor visitor = new ExternalNonEffectivelyFinalReferencesVisitor(
				Collections.singletonList(lineName.getIdentifier()));
		body.accept(visitor);
		if (visitor.containsReferencesToExternalNonFinalVariables()) {
			return;
		}

		FlowBreakersVisitor flowBreakersVisitor = new FlowBreakersVisitor();
		body.accept(flowBreakersVisitor);
		if (flowBreakersVisitor.hasFlowBreakerStatement()) {
			return;
		}

		if(!UnhandledExceptionVisitor.analyzeExceptionHandling(body, loop)) {
			return;
		}

		ExpressionStatement expressionStatement = createForEachStatement(loop.getAST(), lineName, bufferName, body);
		astRewrite.replace(loop, expressionStatement, null);
		removeFragment(lineDeclaration);
		saveComments(loop, body);
		onRewrite();
		addMarkerEvent(loop);
	}

	private void saveComments(Statement loop, Statement body) {
		CommentRewriter commentRewriter = getCommentRewriter();
		List<Comment> loopComments = new ArrayList<>(commentRewriter.findRelatedComments(loop));
		List<Comment> bodyComments = new ArrayList<>(commentRewriter.findRelatedComments(body));
		loopComments.removeAll(bodyComments);
		commentRewriter.saveBeforeStatement(loop, loopComments);
	}

	@SuppressWarnings("unchecked")
	private ExpressionStatement createForEachStatement(AST ast, SimpleName lineName, SimpleName bufferName,
			Statement body) {
		SimpleName linesExpression = newSimpleName(ast, bufferName.getIdentifier());
		MethodInvocation linesInvocation = newMethodInvocation(ast, linesExpression, "lines"); //$NON-NLS-1$
		MethodInvocation forEach = newMethodInvocation(ast, linesInvocation, "forEach"); //$NON-NLS-1$
		LambdaExpression lambda = newLambdaExpression(ast, astRewrite.createCopyTarget(body), lineName.getIdentifier());
		forEach.arguments()
			.add(lambda);
		return newExpressionStatement(ast, forEach);
	}

	private ASTNode findLoopAncestor(Statement loop) {
		ASTNode ancestor = ASTNodeUtil.getSpecificAncestor(loop, Block.class);
		if (ancestor.getLocationInParent() == TryStatement.BODY_PROPERTY) {
			ancestor = ancestor.getParent();
		}
		return ancestor;
	}

	private void removeFragment(VariableDeclarationFragment lineDeclaration) {
		if (lineDeclaration.getLocationInParent() == VariableDeclarationStatement.FRAGMENTS_PROPERTY) {
			CommentRewriter commentRewriter = getCommentRewriter();
			ASTNode parent = lineDeclaration.getParent();
			VariableDeclarationStatement declarationStatement = (VariableDeclarationStatement) parent;
			if (declarationStatement.fragments()
				.size() == 1) {
				List<Comment> comments = commentRewriter.findRelatedComments(declarationStatement);
				commentRewriter.saveBeforeStatement(declarationStatement, comments);
				astRewrite.remove(declarationStatement, null);
				return;
			}
			astRewrite.remove(lineDeclaration, null);
			commentRewriter.saveCommentsInParentStatement(lineDeclaration);
		}
	}

	private Optional<Assignment> verifyExpressionPrecondition(Expression loopExpression) {
		if (loopExpression.getNodeType() != ASTNode.INFIX_EXPRESSION) {
			return Optional.empty();
		}
		InfixExpression infixExpression = (InfixExpression) loopExpression;
		InfixExpression.Operator infixOperator = infixExpression.getOperator();
		if (infixOperator != InfixExpression.Operator.NOT_EQUALS || infixExpression.hasExtendedOperands()) {
			return Optional.empty();
		}
		Expression rightOperand = infixExpression.getRightOperand();
		if (rightOperand.getNodeType() != ASTNode.NULL_LITERAL) {
			return Optional.empty();
		}

		Expression leftOperand = infixExpression.getLeftOperand();
		if (leftOperand.getNodeType() != ASTNode.PARENTHESIZED_EXPRESSION) {
			return Optional.empty();
		}

		ParenthesizedExpression parenthesizedExpression = (ParenthesizedExpression) leftOperand;
		Expression inParenthesis = parenthesizedExpression.getExpression();

		if (inParenthesis.getNodeType() != ASTNode.ASSIGNMENT) {
			return Optional.empty();
		}

		Assignment assignment = (Assignment) inParenthesis;
		Expression assignmentLHS = assignment.getLeftHandSide();
		if (assignmentLHS.getNodeType() != ASTNode.SIMPLE_NAME) {
			return Optional.empty();
		}

		Expression assignmentRHS = assignment.getRightHandSide();
		if (assignmentRHS.getNodeType() != ASTNode.METHOD_INVOCATION) {
			return Optional.empty();
		}

		MethodInvocation methodInvocation = (MethodInvocation) assignmentRHS;
		if (!bufferedReaderReadLine(methodInvocation)) {
			return Optional.empty();
		}

		return Optional.of(assignment);
	}

	private boolean bufferedReaderReadLine(MethodInvocation methodInvocation) {
		SimpleName name = methodInvocation.getName();
		if (!"readLine".equals(name.getIdentifier())) { //$NON-NLS-1$
			return false;
		}
		Expression expression = methodInvocation.getExpression();
		if (expression.getNodeType() != ASTNode.SIMPLE_NAME) {
			return false;
		}
		SimpleName expressionName = (SimpleName) expression;
		ITypeBinding expressionTypeBinding = expressionName.resolveTypeBinding();
		if (expressionTypeBinding == null) {
			return false;
		}
		String bufferedReadQualifiedName = java.io.BufferedReader.class.getName();
		return ClassRelationUtil.isContentOfType(expressionTypeBinding, bufferedReadQualifiedName);
	}

}
