package eu.jsparrow.rules.java16.switchexpression;

import java.util.List;
import java.util.Optional;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.SimpleName;
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
	
	public Optional<SimpleName> findAssignedVariable() {
		return Optional.empty();
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
