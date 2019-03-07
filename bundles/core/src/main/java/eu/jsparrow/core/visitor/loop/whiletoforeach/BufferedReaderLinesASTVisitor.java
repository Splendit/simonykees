package eu.jsparrow.core.visitor.loop.whiletoforeach;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Comment;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.LambdaExpression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.WhileStatement;

import eu.jsparrow.core.builder.NodeBuilder;
import eu.jsparrow.core.visitor.loop.LoopToForEachASTVisitor;
import eu.jsparrow.core.visitor.sub.ExternalNonEffectivelyFinalReferencesVisitor;
import eu.jsparrow.core.visitor.sub.FlowBreakersVisitor;
import eu.jsparrow.rules.common.util.ClassRelationUtil;

public class BufferedReaderLinesASTVisitor extends LoopToForEachASTVisitor<WhileStatement> {

	@Override
	protected List<Comment> getHeaderComments(WhileStatement loop) {
		return Collections.emptyList();
	}

	@Override
	public boolean visit(WhileStatement loop) {
		Assignment readLineAssignment = verifyExpressionPrecondition(loop).orElse(null);
		if (readLineAssignment == null) {
			return true;
		}
		SimpleName lineName = (SimpleName) readLineAssignment.getLeftHandSide();
		MethodInvocation readLine = (MethodInvocation) readLineAssignment.getRightHandSide();
		SimpleName bufferName = (SimpleName) readLine.getExpression();

		BufferedReaderLinesPreconditionVisitor preconditionVisitor = new BufferedReaderLinesPreconditionVisitor(loop,
				lineName, bufferName);
		ASTNode parent = loop.getParent();
		if (parent.getLocationInParent() == TryStatement.BODY_PROPERTY) {
			parent = parent.getParent();
		}
		parent.accept(preconditionVisitor);

		if (!preconditionVisitor.isSatisfied()) {
			return true;
		}

		Statement body = loop.getBody();
		ExternalNonEffectivelyFinalReferencesVisitor visitor = new ExternalNonEffectivelyFinalReferencesVisitor(
				Collections.singletonList(lineName.getIdentifier()));
		body.accept(visitor);

		if (visitor.containsReferencesToExternalNonFinalVariables()) {
			return true;
		}

		FlowBreakersVisitor flowBreakersVisitor = new FlowBreakersVisitor();
		body.accept(flowBreakersVisitor);
		if (flowBreakersVisitor.hasFlowBreakerStatement()) {
			return true;
		}

		VariableDeclarationFragment lineDeclaration = preconditionVisitor.getLineDeclaration();
		AST ast = loop.getAST();
		MethodInvocation linesInvocation = ast.newMethodInvocation();
		linesInvocation.setName(ast.newSimpleName("lines"));
		linesInvocation.setExpression(ast.newSimpleName(bufferName.getIdentifier()));

		MethodInvocation forEach = ast.newMethodInvocation();
		forEach.setName(ast.newSimpleName("forEach"));
		forEach.setExpression(linesInvocation);

		LambdaExpression lambda = NodeBuilder.newLambdaExpression(ast, astRewrite.createCopyTarget(body),
				lineName.getIdentifier());
		forEach.arguments()
			.add(lambda);
		ExpressionStatement expressionStatement = NodeBuilder.newExpressionStatement(ast, forEach);
		astRewrite.replace(loop, expressionStatement, null);
		removeFragment(lineDeclaration);

		onRewrite();

		return true;
	}

	private void removeFragment(VariableDeclarationFragment lineDeclaration) {
		ASTNode parent = lineDeclaration.getParent();
		if (parent.getNodeType() == ASTNode.VARIABLE_DECLARATION_STATEMENT) {
			VariableDeclarationStatement declarationStatement = (VariableDeclarationStatement) parent;
			if (declarationStatement.fragments()
				.size() == 1) {
				astRewrite.remove(declarationStatement, null);
				return;
			}
		}
		astRewrite.remove(lineDeclaration, null);
	}

	private Optional<Assignment> verifyExpressionPrecondition(WhileStatement loop) {
		Expression loopExpression = loop.getExpression();
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
