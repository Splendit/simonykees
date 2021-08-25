package eu.jsparrow.rules.java16.switchexpression;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTMatcher;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.SwitchCase;
import org.eclipse.jdt.core.dom.SwitchStatement;

import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.visitor.AbstractASTRewriteASTVisitor;

public class UseSwitchExpressionASTVisitor extends AbstractASTRewriteASTVisitor {
	
	@Override
	public boolean visit(SwitchStatement switchStatement) {
		List<Statement> statements = ASTNodeUtil.convertToTypedList(switchStatement.statements(), Statement.class);
		
		/*
		 * Group the statements into switch-case expressions. 
		 */
		List<List<Statement>> switchCaseBucks = splitIntoSwitchCaseBucks(statements);
		

		if(!areTransformableBucks(switchCaseBucks)) {
			return true;
		}
		
		List<SwitchCaseClause> clauses = createClauses(switchCaseBucks);
		
		if(areAssigningValue(clauses)) {
			SwitchCaseClause firstClause = clauses.get(0);
			Expression assigned = firstClause.findAssignedVariable().orElse(null);
			if(assigned == null) {
				return true;
			}
			ITypeBinding typeBinding = assigned.resolveTypeBinding();
			int kind = typeBinding.getKind();
			if (kind == ITypeBinding.VARIABLE) {
				ASTNode declaringNode = getCompilationUnit().findDeclaringNode(typeBinding);
				/*
				 * the only question now is: should we put the switch expression on a 
				 * assignment or on a variable declaration node. 
				 */
			}
			
			/*
			 * 1. Find the variable declaration. 
			 * 
			 * 2. If it is declared right before the 
			 * switch statement, then create a switch expression with yield statements and
			 * use it as initializer for the variable declaration.
			 * 
			 * 3. If it is declared somewhere else, then make an assignment expression and put the expression statement as an initializer
			 * in the assignment expression. Put the entire thing on an ExpressionStatement. 
			 * 
			 */
			
		} else if (areReturningValue(clauses)) {
			/*
			 * Similar to above. But everything has to be wrapped on a return statement. 
			 */
		} else {
			AST ast = switchStatement.getAST();
			Expression switchHeaderExpression = switchStatement.getExpression();
			SwitchStatement newSwitchStatement = createSwitchStatement(ast, switchHeaderExpression, clauses);
			astRewrite.replace(switchStatement, newSwitchStatement, null);
			onRewrite();
		}

		
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
	
	private boolean areReturningValue(List<SwitchCaseClause> clauses) {
		return clauses.stream()
				.map(SwitchCaseClause::findReturnedValue)
				.allMatch(Optional::isPresent);
	}

	private boolean areAssigningValue(List<SwitchCaseClause> clauses) {
		List<Expression> assignedExpressions = clauses.stream()
			.map(SwitchCaseClause::findAssignedVariable)
			.filter(Optional::isPresent)
			.map(Optional::get)
			.collect(Collectors.toList());
		
		if(assignedExpressions.size() != clauses.size()) {
			return false;
		}
		
		Expression firstAssigned = assignedExpressions.get(0);
		ASTMatcher matcher = new ASTMatcher();
		for(int i = 1; i < assignedExpressions.size(); i++) {
			Expression assigned = assignedExpressions.get(i);
			if(!firstAssigned.subtreeMatch(matcher, assigned)) {
				return false;
			}
		}
		return true;
	}

	@SuppressWarnings("unchecked")
	private SwitchStatement createSwitchStatement(AST ast, Expression switchHeaderExpression,
			List<SwitchCaseClause> clauses) {
		SwitchStatement newSwitchStatement = ast.newSwitchStatement();
		Expression newHeaderExpression = (Expression) astRewrite.createCopyTarget(switchHeaderExpression);
		newSwitchStatement.setExpression(newHeaderExpression);
		List<Statement>statements = newSwitchStatement.statements();
		for(SwitchCaseClause clause : clauses) {
			List<Expression> clauseExpressions = clause.getExpressions();
			SwitchCase switchCase = ast.newSwitchCase();
			switchCase.setSwitchLabeledRule(true);
			for(Expression expression : clauseExpressions) {
				switchCase.expressions()
					.add((Expression) astRewrite.createCopyTarget(expression));
			}
			statements.add(switchCase);
			
			List<Statement> clauseStatements = clause.getStatements();
			if(clauseStatements.size() == 1) {
				Statement clauseStatement = clauseStatements.get(0);
				if(clauseStatement.getNodeType() == ASTNode.EXPRESSION_STATEMENT) {
					ExpressionStatement newExpStatement = (ExpressionStatement) astRewrite.createCopyTarget(clauseStatement);
					statements.add(newExpStatement);
				} else {
					Block block = ast.newBlock();
					Statement newStatement = (Statement) astRewrite.createCopyTarget(clauseStatement);
					block.statements().add(newStatement);
					statements.add(block);
				}
			} else {
				Block block = ast.newBlock();
				for(Statement clauseStatement : clauseStatements) {
					Statement newStatement = (Statement) astRewrite.createCopyTarget(clauseStatement);
					block.statements().add(newStatement);
				}
				statements.add(block);
			}
			
		}
		return newSwitchStatement;
	}

	private boolean areTransformableBucks(List<List<Statement>> switchCaseBucks) {
		for(List<Statement> buck : switchCaseBucks) {
			if(containsNonConsecutiveSwitchCases(buck)) {
				return false;
			}
			
			if(containsMultipleBreakStatements(buck)) {
				return false;
			}
			
			if(containsSwitchCaseAndDefaultStatement(buck)) {
				return false;
			}
		}
		return true;
	}

	private boolean containsSwitchCaseAndDefaultStatement(List<Statement> buck) {
		List<SwitchCase> switchCases = filterSwitchCaseStatements(buck);
		if(switchCases.isEmpty()) {
			return false;
		}
		SwitchCase caseStatement = switchCases.get(0);
		boolean isDefault = caseStatement.isDefault();
		return switchCases.size() > 1 && switchCases.stream()
				.anyMatch(node -> node.isDefault() != isDefault);
	}

	private List<SwitchCase> filterSwitchCaseStatements(List<Statement> buck) {
		return buck.stream()
				.filter(node -> node.getNodeType() == ASTNode.SWITCH_CASE)
				.map(SwitchCase.class::cast)
				.collect(Collectors.toList());
	}

	private boolean containsMultipleBreakStatements(List<Statement> buck) {
		/*
		 * TODO: implement me!
		 */
		return false;
	}

	private boolean containsNonConsecutiveSwitchCases(List<Statement> buck) {
		List<Integer> caseIndexes = new ArrayList<>();
		for(int i = 0; i < buck.size(); i++) {
			Statement statement = buck.get(i);
			if(statement.getNodeType() == ASTNode.SWITCH_CASE) {
				caseIndexes.add(i);
			}
		}
		if(!caseIndexes.contains(0)) {
			return true;
		}
		for(int index : caseIndexes) {
			if(index != 0 && !caseIndexes.contains(index - 1)) {
				return true;
			}
		}
		return false;
	}

	private List<List<Statement>> splitIntoSwitchCaseBucks(List<Statement> statements) {
		List<List<Statement>> switchCaseBucks = new ArrayList<>();
		List<Integer> breakIndexes = new ArrayList<>();
		for(int i = 0; i<statements.size(); i++) {
			Statement statement = statements.get(i);
			if(statement.getNodeType() == ASTNode.BREAK_STATEMENT 
					|| statement.getNodeType() == ASTNode.RETURN_STATEMENT) {
				breakIndexes.add(i);
			}
		}
		
		List<Statement> buck = new ArrayList<>();
		
		for(int i = 0; i < statements.size(); i++) {
			buck.add(statements.get(i));
			if(breakIndexes.contains(i)) {
				switchCaseBucks.add(buck);
				buck = new ArrayList<>();
			}
		}
		if(!buck.isEmpty()) {
			switchCaseBucks.add(buck);
		}
		return switchCaseBucks;
	}

	private List<SwitchCaseClause> createClauses(List<List<Statement>> switchCaseBucks) {
		List<SwitchCaseClause> clauses = new ArrayList<>();
		for(List<Statement> buck : switchCaseBucks) {
			SwitchCaseClause clause = createClause(buck);
			clauses.add(clause);
		}
		return clauses;
	}

	private SwitchCaseClause createClause(List<Statement> buck) {
		List<SwitchCase> switchCases = filterSwitchCaseStatements(buck);
		@SuppressWarnings("unchecked")
		List<Expression> caseExpressions = switchCases.stream()
			.flatMap(node -> ((List<Expression>)node.expressions()).stream())
			.collect(Collectors.toList());
		List<Statement> blockStatements = buck.stream()
				.filter(node -> node.getNodeType() != ASTNode.SWITCH_CASE)
				.filter(node -> node.getNodeType() != ASTNode.BREAK_STATEMENT)
				.collect(Collectors.toList());
		boolean isDefault = switchCases.get(0).isDefault();
		Statement breakStatement = buck.get(buck.size() - 1);
		return new SwitchCaseClause(caseExpressions, blockStatements, breakStatement, isDefault);
	}

}
