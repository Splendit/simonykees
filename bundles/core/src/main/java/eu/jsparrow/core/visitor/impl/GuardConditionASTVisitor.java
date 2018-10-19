package eu.jsparrow.core.visitor.impl;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BooleanLiteral;
import org.eclipse.jdt.core.dom.Comment;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.PostfixExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.PrimitiveType.Code;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;

import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.visitor.AbstractASTRewriteASTVisitor;
import eu.jsparrow.rules.common.visitor.helper.CommentRewriter;

/**
 * 
 * @since 2.7.0
 */
public class GuardConditionASTVisitor extends AbstractASTRewriteASTVisitor {

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
			analyzeIfReturnElseReturn(methodDeclaration, methodBody, lastStatement);
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
			analyzeIfElseReturn(methodDeclaration, methodBody, ifStatement, elseStatement);
		} else {
			analyzeIfReturn(methodDeclaration, methodBody, lastStatement, ifStatement);
		}

		return true;
	}

	private void analyzeIfReturnElseReturn(MethodDeclaration methodDeclaration, Block methodBody,
			Statement lastStatement) {
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
		if (ASTNode.BLOCK != elseStatement.getNodeType()) {
			return;
		}

		ReturnStatement elseReturnStatement = isSingleReturnStatement(elseStatement);
		if (elseReturnStatement == null) {
			return;
		}

		/*
		 * last statement must be an if-else with the following condition: -
		 * then statement must not be trivial - else statement must not contain
		 * further else statements - both, else-then and if-then must end with
		 * return statements - the body of the else-then must consist of a
		 * single return statement
		 */

		IfStatement guardIfStatement = createGuardIfStatement(ifStatement,
				(ReturnStatement) astRewrite.createMoveTarget(elseReturnStatement), methodDeclaration.getAST());
		insertGuardStatement(methodBody, ifStatement, guardIfStatement);
	}

	private void analyzeIfReturn(MethodDeclaration methodDeclaration, Block methodBody, Statement lastStatement,
			IfStatement ifStatement) {
		// else if there is no else branch, the body of the if must
		// end with a return statement
		// the last return statement is the return on the guard

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
		ReturnStatement guardReturnStatement = (ReturnStatement) astRewrite.createMoveTarget(lastStatement);
		IfStatement guardStatement = createGuardIfStatement(ifStatement, guardReturnStatement,
				methodDeclaration.getAST());
		insertGuardStatement(methodBody, ifStatement, guardStatement);
	}

	private void analyzeIfElseReturn(MethodDeclaration methodDeclaration, Block methodBody, IfStatement ifStatement,
			Statement elseStatement) {
		ReturnStatement elseReturnStatement = isSingleReturnStatement(elseStatement);
		if (elseReturnStatement == null) {
			return;
		}

		// if there is an else branch, its body must consist of a
		// single return statement
		// the return on else branch is the return on the guard

		/*
		 * construct the guard and do the replacement
		 */
		AST ast = methodDeclaration.getAST();
		ReturnStatement guardReturnStatement = (ReturnStatement) astRewrite.createCopyTarget(elseReturnStatement);
		IfStatement guardStatement = createGuardIfStatement(ifStatement, guardReturnStatement, ast);
		insertGuardStatement(methodBody, ifStatement, guardStatement);
	}

	private void analyzeVoidMethod(Block methodBody, List<Statement> statements) {
		IfStatement ifStatement = findLastIfStatement(statements).orElse(null);
		if (ifStatement == null) {
			return;
		}

		Statement elseStatement = ifStatement.getElseStatement();
		if (elseStatement != null) {
			/*
			 * Consider using the body of the else statement as body of the
			 * guard
			 */
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

	private ReturnStatement isSingleReturnStatement(Statement elseStatement) {
		if (ASTNode.IF_STATEMENT == elseStatement.getNodeType()) {
			return null;
		}

		ReturnStatement elseReturnStatement = null;
		if (ASTNode.RETURN_STATEMENT == elseStatement.getNodeType()) {
			elseReturnStatement = (ReturnStatement) elseStatement;
		} else if (ASTNode.BLOCK == elseStatement.getNodeType()) {
			Block elseBlock = (Block) elseStatement;
			List<Statement> elseBlockStatements = ASTNodeUtil.convertToTypedList(elseBlock.statements(),
					Statement.class);
			if (elseBlockStatements.size() != 1) {
				return null;
			}

			Statement elseBodyStatement = elseBlockStatements.get(0);
			if (ASTNode.RETURN_STATEMENT != elseBodyStatement.getNodeType()) {
				return null;
			}
			elseReturnStatement = (ReturnStatement) elseBodyStatement;

		}
		return elseReturnStatement;
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
		Expression guardExpression = createGuardExpression(ifStatement.getExpression());
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
		return "void".equals(code.toString());

	}

	private Expression createGuardExpression(Expression expression) {
		int expressionType = expression.getNodeType();
		AST ast = expression.getAST();
		if (ASTNode.INFIX_EXPRESSION == expressionType) {
			InfixExpression infixExpression = (InfixExpression) expression;
			return negateInfixExpression(infixExpression, ast);
		}

		if (ASTNode.PREFIX_EXPRESSION == expressionType) {
			PrefixExpression prefixExpression = (PrefixExpression) expression;
			PrefixExpression.Operator operator = prefixExpression.getOperator();

			Expression guardExpression;
			if ("!".equals(operator.toString())) {
				Expression body = findPrefixExpressionBody(prefixExpression);
				guardExpression = (Expression) astRewrite.createCopyTarget(body);
			} else {
				guardExpression = createNegatedParenthesized(ast, expression);
			}
			return guardExpression;
		}

		if (ASTNode.METHOD_INVOCATION == expressionType || ASTNode.SIMPLE_NAME == expressionType) {
			return createNegated(ast, expression);
		}
		
		if(ASTNode.BOOLEAN_LITERAL != expressionType) {
			return createNegatedParenthesized(ast, expression);
		}
		
		BooleanLiteral booleanLiteral = (BooleanLiteral)expression;
		if(booleanLiteral.booleanValue()) {
			return ast.newBooleanLiteral(false);
		}
		return ast.newBooleanLiteral(true);
	}

	private Expression findPrefixExpressionBody(PrefixExpression prefixExpression) {
		Expression body = prefixExpression.getOperand();
		if (ASTNode.PARENTHESIZED_EXPRESSION != body.getNodeType()) {
			return body;
		}

		ParenthesizedExpression parenthesizedExpression = (ParenthesizedExpression) body;
		return parenthesizedExpression.getExpression();
	}

	private Expression negateInfixExpression(InfixExpression infixExpression, AST ast) {
		InfixExpression.Operator operator = infixExpression.getOperator();
		InfixExpression.Operator newOperator = null;

		if (!isSimpleExpression(infixExpression)) {
			return createNegatedParenthesized(ast, infixExpression);
		}

		if ("==".equals(operator.toString())) {
			newOperator = InfixExpression.Operator.toOperator("!=");
		} else if ("!=".equals(operator.toString())) {
			newOperator = InfixExpression.Operator.toOperator("==");
		} else if (">".equals(operator.toString())) {
			newOperator = InfixExpression.Operator.toOperator("<=");
		} else if (">=".equals(operator.toString())) {
			newOperator = InfixExpression.Operator.toOperator("<");
		} else if ("<".equals(operator.toString())) {
			newOperator = InfixExpression.Operator.toOperator(">=");
		} else if ("<=".equals(operator.toString())) {
			newOperator = InfixExpression.Operator.toOperator(">");
		}

		if (newOperator == null) {
			return createNegatedParenthesized(ast, infixExpression);
		}

		InfixExpression guardInfixExpression = ast.newInfixExpression();
		guardInfixExpression.setOperator(newOperator);
		guardInfixExpression.setLeftOperand((Expression) astRewrite.createCopyTarget(infixExpression.getLeftOperand()));
		guardInfixExpression
			.setRightOperand((Expression) astRewrite.createCopyTarget(infixExpression.getRightOperand()));

		return guardInfixExpression;

	}

	private Expression createNegatedParenthesized(AST ast, Expression expression) {
		PrefixExpression guardPrefixExpression = ast.newPrefixExpression();
		guardPrefixExpression.setOperator(PrefixExpression.Operator.toOperator("!"));
		ParenthesizedExpression parenthesizedExpression = ast.newParenthesizedExpression();
		parenthesizedExpression.setExpression((Expression) astRewrite.createCopyTarget(expression));
		guardPrefixExpression.setOperand(parenthesizedExpression);
		return guardPrefixExpression;
	}

	private Expression createNegated(AST ast, Expression expression) {
		PrefixExpression guardPrefixExpression = ast.newPrefixExpression();
		guardPrefixExpression.setOperator(PrefixExpression.Operator.toOperator("!"));
		guardPrefixExpression.setOperand((Expression) astRewrite.createCopyTarget(expression));
		return guardPrefixExpression;
	}

	private boolean isSimpleExpression(InfixExpression expression) {
		SimpleExpressionVisitor visitor = new SimpleExpressionVisitor();
		Expression left = expression.getLeftOperand();
		left.accept(visitor);
		boolean isSimpleLeftOperand = visitor.isSimple();
		if (!isSimpleLeftOperand) {
			return false;
		}
		visitor = new SimpleExpressionVisitor();
		Expression right = expression.getRightOperand();
		right.accept(visitor);
		return visitor.isSimple();
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

	class SimpleExpressionVisitor extends ASTVisitor {

		private boolean isSimple = true;

		@Override
		public boolean preVisit2(ASTNode node) {
			return isSimple;
		}

		@Override
		public boolean visit(InfixExpression expression) {
			isSimple = false;
			return true;
		}

		@Override
		public boolean visit(PostfixExpression expression) {
			isSimple = false;
			return true;
		}

		@Override
		public boolean visit(PrefixExpression expression) {
			isSimple = false;
			return true;
		}

		public boolean isSimple() {
			return isSimple;
		}

	}
}
