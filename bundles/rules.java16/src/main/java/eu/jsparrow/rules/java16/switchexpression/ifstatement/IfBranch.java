package eu.jsparrow.rules.java16.switchexpression.ifstatement;

import java.util.List;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.Statement;

class IfBranch {
	private final List<Expression> expressionsForSwitchCase;
	private final Statement statement;

	public IfBranch(List<Expression> expressionsForSwitchCase, Statement statement) {
		this.expressionsForSwitchCase = expressionsForSwitchCase;
		this.statement = statement;
	}

	public List<Expression> getExpressionsForSwitchCase() {
		return expressionsForSwitchCase;
	}

	Statement getStatement() {
		return statement;
	}
}