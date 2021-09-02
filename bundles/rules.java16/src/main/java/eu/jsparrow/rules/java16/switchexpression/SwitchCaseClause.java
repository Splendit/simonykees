package eu.jsparrow.rules.java16.switchexpression;

import java.util.List;
import java.util.Optional;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.BreakStatement;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.ThrowStatement;

public class SwitchCaseClause {

	private List<Expression> expressions;
	private List<Statement> statements;
	private List<BreakStatement> breakStatements;

	public SwitchCaseClause(List<Expression> expressions, List<Statement> statements, List<BreakStatement> breakStatements) {
		this.expressions = expressions;
		this.statements = statements;
		this.breakStatements = breakStatements;
	}

	public Optional<Expression> findAssignedVariable() {
		if (this.statements.isEmpty()) {
			return Optional.empty();
		}
		Statement last = statements.get(statements.size() - 1);
		if (last.getNodeType() != ASTNode.EXPRESSION_STATEMENT) {
			return Optional.empty();
		}
		ExpressionStatement expressionStatement = (ExpressionStatement) last;
		Expression expression = expressionStatement.getExpression();
		if (expression.getNodeType() != ASTNode.ASSIGNMENT) {
			return Optional.empty();
		}
		Assignment assignment = (Assignment) expression;
		if(assignment.getOperator() != Assignment.Operator.ASSIGN) {
			return Optional.empty();
		}
		return Optional.of(assignment.getLeftHandSide());
	}

	public Optional<Expression> findReturnedValue() {
		if (this.statements.isEmpty()) {
			return Optional.empty();
		}
		Statement last = statements.get(statements.size() - 1);
		if (last.getNodeType() != ASTNode.RETURN_STATEMENT) {
			return Optional.empty();
		}
		ReturnStatement returnStatement = (ReturnStatement) last;
		return Optional.ofNullable(returnStatement.getExpression());
	}

	public Optional<ThrowStatement> findThrowsStatement() {
		if (this.statements.isEmpty()) {
			return Optional.empty();
		}
		Statement last = statements.get(statements.size() - 1);
		if (last.getNodeType() != ASTNode.THROW_STATEMENT) {
			return Optional.empty();
		}
		ThrowStatement throwStatement = (ThrowStatement) last;
		return Optional.of(throwStatement);
	}

	public Expression findYieldExpression() {
		return findAssignedVariable()
			.map(Expression::getParent)
			.map(Assignment.class::cast)
			.map(Assignment::getRightHandSide)
			.orElse(findReturnedValue().orElse(null));
	}

	public List<Expression> getExpressions() {
		return expressions;
	}

	public boolean hasInternalBreakStatements() {
		return this.breakStatements.size() > 1;
	}

	public List<Statement> getStatements() {
		return statements;
	}

	public boolean isReturningValue() {
		if (findReturnedValue().isPresent()) {
			return true;
		}
		
		return findThrowsStatement().isPresent();
	}

}
