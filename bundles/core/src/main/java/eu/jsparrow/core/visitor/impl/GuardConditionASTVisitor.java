package eu.jsparrow.core.visitor.impl;

import java.util.List;
import java.util.Optional;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.InfixExpression.Operator;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.PrimitiveType.Code;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;

import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.visitor.AbstractASTRewriteASTVisitor;

public class GuardConditionASTVisitor extends AbstractASTRewriteASTVisitor {

	@SuppressWarnings("unchecked")
	@Override
	public boolean visit(MethodDeclaration methodDeclaration) {

		Block methodBody = methodDeclaration.getBody();
		List<Statement> statements = ASTNodeUtil.convertToTypedList(methodBody.statements(), Statement.class);

		Type returnType = methodDeclaration.getReturnType2();
		if (returnType.isPrimitiveType()) {
			PrimitiveType primitiveType = (PrimitiveType) returnType;
			Code code = primitiveType.getPrimitiveTypeCode();
			if ("void".equals(code.toString())) {

				IfStatement ifStatement = findSingleIfStatement(statements).orElse(null);
				if (ifStatement == null) {
					return true;
				}

				Statement elseStatement = ifStatement.getElseStatement();
				if (elseStatement != null) {
					/*
					 * Consider using the body of the else statement as body of
					 * the guard
					 */
					return true;
				}

				Statement thenStatement = ifStatement.getThenStatement();
				if (ASTNode.BLOCK != thenStatement.getNodeType()) {
					return true;
				}

				Block ifBody = (Block) thenStatement;
				List<Statement> ifBodyStatements = ASTNodeUtil.convertToTypedList(ifBody.statements(), Statement.class);
				if (ifBodyStatements.size() < 2) {
					/*
					 * Trivial if body
					 */
					return true;
				}

				// generate the new method body
				AST ast = methodDeclaration.getAST();
				IfStatement guardIf = ast.newIfStatement();

				Expression guardExpression = createGuardExpression(ifStatement.getExpression());
				guardIf.setExpression(guardExpression);
				ReturnStatement returnStatement = ast.newReturnStatement();
				Block guardThen = ast.newBlock();
				guardThen.statements().add(returnStatement);
				guardIf.setThenStatement(guardThen);

				ListRewrite listRewrite = astRewrite.getListRewrite(methodBody, Block.STATEMENTS_PROPERTY);
				for (Statement ifBodyStatement : ifBodyStatements) {
					listRewrite.insertLast(ifBodyStatement, null);
				}
				astRewrite.replace(ifStatement, guardIf, null);

				/*
				 * the return statement is empty.
				 */
			}
		}

		// If method is void, list of statements should consist of a single if
		// statement
		/*
		 * if there is an else branch, it makes no sense to have a guard
		 * statement
		 */

		// If the method is not void, find the return statement for the guard
		/*
		 * 3 different cases to consider.
		 */

		// Find the condition of the if statement

		// Find the body of the if statement

		// Make sure the body of the if statement is not trivial (i.e. it does
		// not consist of a single statement)

		// Create the guard statement

		// Create the remaining body of the method

		// Construct the new method body

		return true;
	}

	private Expression createGuardExpression(Expression expression) {
		AST ast = expression.getAST();
		if (ASTNode.INFIX_EXPRESSION == expression.getNodeType()) {
			InfixExpression infixExpression = (InfixExpression) expression;
			Operator operator = infixExpression.getOperator();
			Operator newOperator = null;
			if ("==".equals(operator.toString())) {
				newOperator = Operator.toOperator("!=");
			} else if ("!=".equals(operator.toString())) {
				newOperator = Operator.toOperator("==");
			} else if (">".equals(operator.toString())) {
				newOperator = Operator.toOperator("<=");
			} else if (">=".equals(operator.toString())) {
				newOperator = Operator.toOperator("<");
			} else if ("<".equals(operator.toString())) {
				newOperator = Operator.toOperator(">=");
			} else if ("<=".equals(operator.toString())) {
				newOperator = Operator.toOperator(">");
			}

			Expression guardExpression;
			if (newOperator != null) {
				InfixExpression guardInfixExpression = ast.newInfixExpression();
				guardInfixExpression.setOperator(newOperator);
				guardInfixExpression.setLeftOperand((Expression) astRewrite.createCopyTarget(infixExpression.getLeftOperand()));
				guardInfixExpression.setRightOperand((Expression) astRewrite.createCopyTarget(infixExpression.getRightOperand()));
				guardExpression = guardInfixExpression;
			} else {
				PrefixExpression guardPrefixExpression = ast.newPrefixExpression();
				guardPrefixExpression.setOperator(PrefixExpression.Operator.toOperator("!"));
				guardPrefixExpression.setOperand((Expression)astRewrite.createCopyTarget(infixExpression));
				guardExpression = guardPrefixExpression;
			}
			
			return guardExpression;

		} else if(ASTNode.PREFIX_EXPRESSION == expression.getNodeType()) {
			PrefixExpression prefixExpression = (PrefixExpression)expression;
			PrefixExpression.Operator operator = prefixExpression.getOperator();
			
			Expression guardExpression;
			if("!".equals(operator.toString())) {
				guardExpression = (Expression) astRewrite.createCopyTarget(prefixExpression.getOperand());
			} else {
				PrefixExpression guardPrefixExpression = ast.newPrefixExpression();
				guardPrefixExpression.setOperator(PrefixExpression.Operator.toOperator("!"));
				guardPrefixExpression.setOperand((Expression)astRewrite.createCopyTarget(expression));
				guardExpression = guardPrefixExpression;
			}
			return guardExpression;
		} else {
			PrefixExpression guardPrefixExpression = ast.newPrefixExpression();
			guardPrefixExpression.setOperator(PrefixExpression.Operator.toOperator("!"));
			guardPrefixExpression.setOperand((Expression)astRewrite.createCopyTarget(expression));
			return guardPrefixExpression;
		}
	}

	private Optional<IfStatement> findSingleIfStatement(List<Statement> statements) {
		if (statements.size() != 1) {
			return Optional.empty();
		}

		Statement statement = statements.get(0);
		if (statement.getNodeType() != ASTNode.IF_STATEMENT) {
			return Optional.empty();
		}

		return Optional.of((IfStatement) statement);
	}
}
