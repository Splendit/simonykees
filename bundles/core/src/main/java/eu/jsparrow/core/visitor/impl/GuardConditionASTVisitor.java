package eu.jsparrow.core.visitor.impl;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.Comment;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.PrimitiveType.Code;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;

import eu.jsparrow.core.markers.common.GuardConditionEvent;
import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.util.OperatorUtil;
import eu.jsparrow.rules.common.visitor.AbstractASTRewriteASTVisitor;
import eu.jsparrow.rules.common.visitor.helper.CommentRewriter;

/**
 * A visitor for converting the last if-statement in a method body, to a guard
 * if.
 * 
 * @since 2.7.0
 */
public class GuardConditionASTVisitor extends AbstractASTRewriteASTVisitor implements GuardConditionEvent {

	private static final Code VOID = PrimitiveType.VOID;

	@Override
	public boolean visit(MethodDeclaration methodDeclaration) {

		Block methodBody = methodDeclaration.getBody();
		if (methodBody == null) {
			return true;
		}
		List<Statement> statements = ASTNodeUtil.convertToTypedList(methodBody.statements(), Statement.class);
		if (statements.isEmpty()) {
			return true;
		}

		Type returnType = methodDeclaration.getReturnType2();
		if (returnType == null) {
			return true;
		}
		if (isVoid(returnType)) {
			analyzeVoidMethod(methodBody, statements);
			return true;
		}

		int numStatements = statements.size();
		Statement lastStatement = statements.get(numStatements - 1);

		if (ASTNode.IF_STATEMENT == lastStatement.getNodeType()) {
			analyzeIfReturnElseReturn(methodDeclaration, lastStatement);
			return true;
		}

		if (ASTNode.RETURN_STATEMENT != lastStatement.getNodeType()) {
			return true;

		}
		if (numStatements == 1) {
			return true;
		}
		Statement secondLastStatement = statements.get(numStatements - 2);
		if (ASTNode.IF_STATEMENT != secondLastStatement.getNodeType()) {
			return true;
		}

		IfStatement ifStatement = (IfStatement) secondLastStatement;
		if (isTrivialIfStatementBody(ifStatement, 2)) {
			return true;
		}

		Statement elseStatement = ifStatement.getElseStatement();
		if (elseStatement != null) {
			analyzeIfElseReturn(methodDeclaration, ifStatement, elseStatement);
		} else {
			analyzeIfReturn(methodDeclaration, (ReturnStatement) lastStatement, ifStatement);
		}

		return true;
	}

	/**
	 * Analyzes the last statement of a void method. If possible, unwraps a
	 * guard-if statement. For example, the following:
	 * 
	 * <pre>
	 * <code>
	 * public void method() {
	 * 	...
	 * 	if(condition()) {
	 * 		doSomething();
	 * 		doSomethingMore();
	 * 	}
	 * }
	 * </code>
	 * </pre>
	 * 
	 * is transformed to:
	 * 
	 * <pre>
	 * <code>
	 * public void method() {
	 * 	...
	 * 	if(!condition()) {
	 * 		return;
	 * 	}
	 * 	doSomething();
	 * 	doSomethingMore();
	 * }
	 * </code>
	 * </pre>
	 * 
	 * @param methodBody
	 *            the body of a method to be analyzed.
	 * @param statements
	 *            the list of statements in the method body.
	 */
	private void analyzeVoidMethod(Block methodBody, List<Statement> statements) {
		IfStatement ifStatement = findLastIfStatement(statements).orElse(null);
		if (ifStatement == null) {
			return;
		}

		Statement elseStatement = ifStatement.getElseStatement();
		if (elseStatement != null) {
			return;
		}

		if (isTrivialIfStatementBody(ifStatement, 2)) {
			return;
		}

		// generate the new method body
		AST ast = methodBody.getAST();
		IfStatement guardIf = createGuardIfStatement(ifStatement, ast.newReturnStatement(), ast);
		insertGuardStatement(methodBody, ifStatement, guardIf);
	}

	/**
	 * Checks whether the last statement of the method body is an
	 * {@link IfStatement} having an else branch consisting of a single
	 * {@link ReturnStatement}.
	 * 
	 * <pre>
	 * <code>
	 * public Object method() {
	 * 	...
	 * 	if(condition()) {
	 * 		doSomething();
	 * 		doSomethingMore();
	 * 		return something;
	 * 	} else {
	 * 		return somethingElse;
	 * 	}
	 * }
	 * </code>
	 * </pre>
	 * 
	 * is transformed to:
	 * 
	 * <pre>
	 * <code>
	 * public void method() {
	 * 	...
	 * 	if(!condition()) {
	 * 		return somethingElse;
	 * 	}
	 * 	doSomething();
	 * 	doSomethingMore();
	 * 	return something;
	 * }
	 * </code>
	 * </pre>
	 * 
	 * @param methodDeclaration
	 *            the method declaration to be analyzed.
	 * @param lastStatement
	 *            the last statement of the method body.
	 */
	private void analyzeIfReturnElseReturn(MethodDeclaration methodDeclaration, Statement lastStatement) {
		/*
		 * last statement must be an if-else with the following condition: then
		 * statement must not be trivial - else statement must not contain
		 * further else statements - both, else-then and if-then must end with
		 * return statements - the body of the else-then must consist of a
		 * single return statement
		 */
		IfStatement ifStatement = (IfStatement) lastStatement;
		if (isTrivialIfStatementBody(ifStatement, 3)) {
			return;
		}

		List<Statement> thenStatements = ASTNodeUtil
			.convertToTypedList(((Block) ifStatement.getThenStatement()).statements(), Statement.class);
		Statement lastThenStatement = thenStatements.get(thenStatements.size() - 1);
		if (ASTNode.RETURN_STATEMENT != lastThenStatement.getNodeType()) {
			return;
		}

		Statement elseStatement = ifStatement.getElseStatement();
		if (ASTNode.BLOCK != elseStatement.getNodeType() && ASTNode.RETURN_STATEMENT != elseStatement.getNodeType()) {
			return;
		}

		ReturnStatement elseReturnStatement = findSingleReturnStatement(elseStatement).orElse(null);
		if (elseReturnStatement == null) {
			return;
		}

		IfStatement guardIfStatement = createGuardIfStatement(ifStatement,
				(ReturnStatement) astRewrite.createMoveTarget(elseReturnStatement), methodDeclaration.getAST());
		insertGuardStatement(methodDeclaration.getBody(), ifStatement, guardIfStatement);
	}

	/**
	 * Checks whether the second last statement of the method body is an
	 * {@link IfStatement} ending with a return, and the last statement of the
	 * method body ends with a {@link ReturnStatement}.
	 * 
	 * <pre>
	 * <code>
	 * public Object method() {
	 * 	...
	 * 	if(condition()) {
	 * 		doSomething();
	 * 		doSomethingMore();
	 * 		return something;
	 * 	}
	 * 	return somethingElse;
	 * }
	 * </code>
	 * </pre>
	 * 
	 * is transformed to:
	 * 
	 * <pre>
	 * <code>
	 * public void method() {
	 * 	...
	 * 	if(!condition()) {
	 * 		return somethingElse;
	 * 	}
	 * 	doSomething();
	 * 	doSomethingMore();
	 * 	return something;
	 * }
	 * </code>
	 * </pre>
	 * 
	 * @param methodDeclaration
	 *            the method declaration to be analyzed.
	 * @param returnStatement
	 *            the last return statement of the method body.
	 * @param ifStatement
	 *            the last if statement of the method body.
	 */
	private void analyzeIfReturn(MethodDeclaration methodDeclaration, ReturnStatement returnStatement,
			IfStatement ifStatement) {

		// if it's not a block, it's trivial
		Block thenStatement = (Block) ifStatement.getThenStatement();
		List<Statement> ifBodyStatements = ASTNodeUtil.convertToTypedList(thenStatement.statements(), Statement.class);
		int size = ifBodyStatements.size();
		Statement lastIfBodyStatement = ifBodyStatements.get(size - 1);
		if (ASTNode.RETURN_STATEMENT != lastIfBodyStatement.getNodeType()) {
			return;
		}

		/*
		 * construct the guard statement and do the replacement
		 */
		ReturnStatement guardReturnStatement = (ReturnStatement) astRewrite.createMoveTarget(returnStatement);
		IfStatement guardStatement = createGuardIfStatement(ifStatement, guardReturnStatement,
				methodDeclaration.getAST());
		insertGuardStatement(methodDeclaration.getBody(), ifStatement, guardStatement);
	}

	/**
	 * Checks whether the second last statement of the method body is an
	 * {@link IfStatement} having an else branch consisting of a single
	 * {@link ReturnStatement}, and the last statement of the method body is a
	 * {@link ReturnStatement}
	 * 
	 * <pre>
	 * <code>
	 * public Object method() {
	 * 	...
	 * 	if(condition()) {
	 * 		doSomething();
	 * 		doSomethingMore();
	 * 	} else {
	 * 		return somethingElse;
	 * 	}
	 * 	return something;
	 * }
	 * </code>
	 * </pre>
	 * 
	 * is transformed to:
	 * 
	 * <pre>
	 * <code>
	 * public void method() {
	 * 	...
	 * 	if(!condition()) {
	 * 		return somethingElse;
	 * 	}
	 * 	doSomething();
	 * 	doSomethingMore();
	 * 	return something;
	 * }
	 * </code>
	 * </pre>
	 * 
	 * @param methodDeclaration
	 *            the method declaration to be analyzed.
	 * @param ifStatement
	 *            the last if statement of the method.
	 * @param elseStatement
	 *            the else branch of the if statement
	 * 
	 */
	private void analyzeIfElseReturn(MethodDeclaration methodDeclaration, IfStatement ifStatement,
			Statement elseStatement) {
		ReturnStatement elseReturnStatement = findSingleReturnStatement(elseStatement).orElse(null);
		if (elseReturnStatement == null) {
			return;
		}

		/*
		 * construct the guard and do the replacement
		 */
		AST ast = methodDeclaration.getAST();
		ReturnStatement guardReturnStatement = (ReturnStatement) astRewrite.createCopyTarget(elseReturnStatement);
		IfStatement guardStatement = createGuardIfStatement(ifStatement, guardReturnStatement, ast);
		insertGuardStatement(methodDeclaration.getBody(), ifStatement, guardStatement);
	}

	private Optional<ReturnStatement> findSingleReturnStatement(Statement elseStatement) {
		if (ASTNode.IF_STATEMENT == elseStatement.getNodeType()) {
			return Optional.empty();
		}

		if (ASTNode.RETURN_STATEMENT == elseStatement.getNodeType()) {
			return Optional.of((ReturnStatement) elseStatement);
		}

		if (ASTNode.BLOCK == elseStatement.getNodeType()) {
			Block elseBlock = (Block) elseStatement;
			return ASTNodeUtil
				.findSingletonListElement(elseBlock.statements(), ReturnStatement.class);

		}
		return Optional.empty();
	}

	private void insertGuardStatement(Block methodBody, IfStatement ifStatement, IfStatement guardStatement) {
		astRewrite.replace(ifStatement, guardStatement, null);
		ListRewrite listRewrite = astRewrite.getListRewrite(methodBody, Block.STATEMENTS_PROPERTY);
		Block thenStatement = (Block) ifStatement.getThenStatement();
		List<Statement> ifBodyStatements = ASTNodeUtil.convertToTypedList(thenStatement.statements(), Statement.class);
		saveComments(ifStatement, ifBodyStatements);
		Collections.reverse(ifBodyStatements);
		for (Statement statement : ifBodyStatements) {
			listRewrite.insertAfter(astRewrite.createMoveTarget(statement), ifStatement, null);
		}
		onRewrite();
		addMarkerEvent(ifStatement);
	}

	private List<Comment> saveComments(IfStatement ifStatement, List<Statement> ifBodyStatements) {
		CommentRewriter commentRewriter = getCommentRewriter();
		List<Comment> comments = commentRewriter.findRelatedComments(ifStatement);
		List<Comment> expressionRelatedComments = commentRewriter.findRelatedComments(ifStatement.getExpression());
		List<Comment> commentsRelatedWithBodyStatements = ifBodyStatements.stream()
			.map(commentRewriter::findRelatedComments)
			.flatMap(List::stream)
			.collect(Collectors.toList());
		comments.removeAll(expressionRelatedComments);
		comments.removeAll(commentsRelatedWithBodyStatements);
		commentRewriter.saveBeforeStatement(ifStatement, comments);
		return comments;
	}

	@SuppressWarnings("unchecked")
	private IfStatement createGuardIfStatement(IfStatement ifStatement, ReturnStatement returnStatement, AST ast) {
		IfStatement guardStatement = ast.newIfStatement();
		Block guardBody = ast.newBlock();
		guardBody.statements()
			.add(returnStatement);
		Expression guardExpression = OperatorUtil.createNegatedExpression(ifStatement.getExpression(), astRewrite);
		guardStatement.setExpression(guardExpression);
		guardStatement.setThenStatement(guardBody);
		return guardStatement;
	}

	private boolean isTrivialIfStatementBody(IfStatement ifStatement, int minBodyStatements) {
		Statement thenStatement = ifStatement.getThenStatement();
		if (ASTNode.BLOCK != thenStatement.getNodeType()) {
			return true;
		}

		Block ifBody = (Block) thenStatement;
		List<Statement> ifBodyStatements = ASTNodeUtil.convertToTypedList(ifBody.statements(), Statement.class);
		return ifBodyStatements.size() < minBodyStatements;
	}

	private boolean isVoid(Type returnType) {
		if (!returnType.isPrimitiveType()) {
			return false;
		}
		PrimitiveType primitiveType = (PrimitiveType) returnType;

		Code code = primitiveType.getPrimitiveTypeCode();
		return VOID.equals(code);
	}

	private Optional<IfStatement> findLastIfStatement(List<Statement> statements) {
		if (statements.isEmpty()) {
			return Optional.empty();
		}

		Statement statement = statements.get(statements.size() - 1);
		if (statement.getNodeType() != ASTNode.IF_STATEMENT) {
			return Optional.empty();
		}

		return Optional.of((IfStatement) statement);
	}
}
