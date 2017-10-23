package eu.jsparrow.core.visitor.impl;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.LambdaExpression;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.Statement;

import eu.jsparrow.core.visitor.AbstractASTRewriteASTVisitor;

/**
 * 
 * If the body of the {@link LambdaExpression} is a {@link Block} and it only
 * contains a single {@link ExpressionStatement} or a {@link ReturnStatement},
 * this rule will replace the {@link Block} with the containing
 * {@link Expression}. Hence, the statement lambda becomes an expression lambda.
 * 
 * before: list.stream().map(element -> { return element * 2; }
 * 
 * after: list.stream.map(element -> element * 2);
 * 
 * @author Matthias Webhofer
 * @since 1.2
 *
 */
public class StatementLambdaToExpressionASTVisitor extends AbstractASTRewriteASTVisitor {

	@Override
	public boolean visit(LambdaExpression lambdaExpression) {
		ASTNode lambdaBody = lambdaExpression.getBody();
		if (lambdaBody instanceof Block) {
			Block block = (Block) lambdaBody;
			boolean hasExplicitReturnStatement = false;
			if (block.statements()
				.size() == 2) {
				hasExplicitReturnStatement = this.checkForExplicitReutrnStatement(block);
			}
			if (block.statements()
				.size() == 1 || hasExplicitReturnStatement) {
				this.replaceNode(block);
			}
		}

		return true;
	}

	/**
	 * replaces the given block by the newly calculated expression
	 * 
	 * @param block
	 */
	private void replaceNode(Block block) {
		Statement statement = (Statement) block.statements()
			.get(0);
		if (statement instanceof ReturnStatement) {
			ReturnStatement returnStatement = (ReturnStatement) statement;
			astRewrite.replace(block, returnStatement.getExpression(), null);
		} else if (statement instanceof ExpressionStatement) {
			ExpressionStatement expressionStatemnet = (ExpressionStatement) statement;
			astRewrite.replace(block, expressionStatemnet.getExpression(), null);
		}
	}

	/**
	 * checks if the given {@link Block} has an explicit return statement in it
	 * 
	 * @param block
	 * @return true, if an explicit return statement is present, false otherwise
	 */
	private boolean checkForExplicitReutrnStatement(Block block) {
		boolean hasExplicitReturnStatement = false;
		Statement statement = (Statement) block.statements()
			.get(1);
		if (statement instanceof ReturnStatement) {
			ReturnStatement returnStatement = (ReturnStatement) statement;
			if (returnStatement.getExpression() == null) {
				hasExplicitReturnStatement = true;
			}
		}
		return hasExplicitReturnStatement;
	}
}
