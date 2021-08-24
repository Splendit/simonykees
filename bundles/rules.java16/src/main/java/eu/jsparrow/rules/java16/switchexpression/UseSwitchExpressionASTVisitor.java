package eu.jsparrow.rules.java16.switchexpression;

import java.util.List;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.SwitchExpression;
import org.eclipse.jdt.core.dom.SwitchStatement;

import eu.jsparrow.rules.common.visitor.AbstractASTRewriteASTVisitor;

public class UseSwitchExpressionASTVisitor extends AbstractASTRewriteASTVisitor {
	
	@Override
	public boolean visit(SwitchStatement switchStatement) {
		Expression expression = switchStatement.getExpression();
		List statements = switchStatement.statements();
		
		/*
		 * Group the statements into switch-case expressions. 
		 */
		
		Controller controller = new Controller();
		
		AST ast = switchStatement.getAST();
		SwitchExpression switchExpression = ast.newSwitchExpression();
		
		switchExpression.setExpression(controller.getNewExpression());
		
		List<Statement> newStatements = controller.getNewStatements();
		for(Statement newStatement : newStatements) {
			switchExpression.statements().add(newStatement);
		}

		Statement switchExpressionStatement = controller.calcNewStatement(switchExpression);
		astRewrite.replace(switchStatement, switchExpressionStatement, null);
		
		/*
		 * Consider the following: 
		 * 1. Switch Case with break statements
		 * 2. Switch Case with return statements. 
		 * 
		 * Each Switch case may have:
		 * a) Only one statement and a break/return
		 * b) Multiple statements ending with a break/return 
		 * 
		 * Consecutive Switch-case statements should be grouped together.
		 * If a switch-case statement is not followed by a consecutive Switch-Case statement 
		 * but does not contain its own break/return statement before the next switch-case statement
		 * starts, then it is not possible to transform the switch statement to a switch expression. 
		 * 
		 * Switch Statements can be used to compute a value which can be assigned to a local variable or simply returned. 
		 * If each switch-case 'group' is ending with an assignment or a return statement, then the switch expression can be placed on a 
		 * variable initialization/assignment statement or on a return statement respectively. 
		 *  
		 * 
		 * 
		 */
		return true;
	}

}
