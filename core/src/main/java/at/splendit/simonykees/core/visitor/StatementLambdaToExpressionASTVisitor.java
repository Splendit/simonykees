package at.splendit.simonykees.core.visitor;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.LambdaExpression;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.Statement;

/**
 * 
 * If the body of the {@link LambdaExpression} is a {@link Block} and it only
 * contains a single {@link ExpressionStatement} or a {@link ReturnStatement},
 * this rule will replace the {@link Block} with the containing {@link Expression}. Hence,
 * the statement lambda becomes an expression lambda.
 * 
 * before: list.stream().map(element -> { 
 * 				return element * 2; 
 * 			}
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
			if(block.statements().size() == 2) {
				Statement statement = (Statement) block.statements().get(1);
				if(statement instanceof ReturnStatement) {
					ReturnStatement returnStatement = (ReturnStatement) statement;
					if(returnStatement.getExpression() == null) {
						hasExplicitReturnStatement = true;
					}
				}
			}
			if (block.statements().size() == 1 || hasExplicitReturnStatement) {
				// change to expression
				Statement statement = (Statement) block.statements().get(0);
				if (statement instanceof ReturnStatement) {
					ReturnStatement returnStatement = (ReturnStatement) statement;
					astRewrite.replace(block, returnStatement.getExpression(), null);
				} else if (statement instanceof ExpressionStatement) {
					ExpressionStatement expressionStatemnet = (ExpressionStatement) statement;
					astRewrite.replace(block, expressionStatemnet.getExpression(), null);
				}
			}
		}

		return true;
	}

}
