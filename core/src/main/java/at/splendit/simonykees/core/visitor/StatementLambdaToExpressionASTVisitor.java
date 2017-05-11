package at.splendit.simonykees.core.visitor;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.LambdaExpression;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.Statement;

/**
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
			if (block.statements().size() == 1) {
				// change to expression
				Statement statement = (Statement) block.statements().get(0);
				if (statement instanceof ReturnStatement) {
					ReturnStatement returnStatement = (ReturnStatement) statement;
					Expression expression = (Expression) astRewrite.createMoveTarget(returnStatement.getExpression());
					astRewrite.replace(block, expression, null);
				}
			} else {
				// do nothing
			}
		} else if (lambdaBody instanceof Expression) {
			// allready finished
		} else {
			// error?
		}

		return true;
	}

}
