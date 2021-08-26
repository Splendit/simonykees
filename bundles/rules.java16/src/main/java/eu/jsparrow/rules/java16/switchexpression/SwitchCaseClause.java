package eu.jsparrow.rules.java16.switchexpression;

import java.util.List;
import java.util.Optional;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.Statement;

public class SwitchCaseClause {
	
	private List<Expression> expressions;
	private List<Statement> statements;
	private Statement breakStatement;
	private boolean isDefaultClause;
	private boolean withBreak;
	private boolean withReturn;
	
	public SwitchCaseClause(List<Expression> expressions, List<Statement> statements, 
			Statement breakStatement, boolean isDefaultClause) {
		this.expressions = expressions;
		this.statements = statements;
		this.breakStatement = breakStatement;
		this.isDefaultClause = isDefaultClause;
	}
	
	public Optional<Expression> findAssignedVariable() {
		if(this.statements.isEmpty()) {
			return Optional.empty();
		}
		Statement last = statements.get(statements.size() - 1);
		if(last.getNodeType() != ASTNode.EXPRESSION_STATEMENT) {
			return Optional.empty();
		}
		ExpressionStatement expressionStatement = (ExpressionStatement)last;
		Expression expression = expressionStatement.getExpression();
		if(expression.getNodeType() != ASTNode.ASSIGNMENT) {
			return Optional.empty();
		}
		Assignment assignment = (Assignment)expression;
		return Optional.of(assignment.getLeftHandSide());
	}
	
	public Optional<Expression> findReturnedValue() {
		if(this.statements.isEmpty()) {
			return Optional.empty();
		}
		Statement last = statements.get(statements.size() - 1);
		if(last.getNodeType() != ASTNode.RETURN_STATEMENT) {
			return Optional.empty();
		}
		ReturnStatement returnStatement = (ReturnStatement)last;
		return Optional.of(returnStatement.getExpression());
	}

	public List<Expression> getExpressions() {
		return expressions;
	}

	public List<Statement> getStatements() {
		return statements;
	}

	public Statement getBreakStatement() {
		return breakStatement;
	}

	public boolean isDefaultClause() {
		return isDefaultClause;
	}

}
