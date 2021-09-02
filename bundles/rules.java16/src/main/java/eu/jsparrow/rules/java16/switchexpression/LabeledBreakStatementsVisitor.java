package eu.jsparrow.rules.java16.switchexpression;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.BreakStatement;
import org.eclipse.jdt.core.dom.LabeledStatement;
import org.eclipse.jdt.core.dom.Statement;

/**
 * A visitor to find {@link LabeledStatement}s and {@link BreakStatement}s with
 * a label. For example: 
 * 
 * {@code label_1: {Statement}} or {@code break label_1;}
 * 
 * @since 4.3.0
 *
 */
class LabeledBreakStatementsVisitor extends ASTVisitor {

	private List<Statement> statements = new ArrayList<>();

	@Override
	public boolean visit(LabeledStatement statement) {
		this.statements.add(statement);
		return true;
	}

	@Override
	public boolean visit(BreakStatement breakStatement) {
		if (breakStatement.getLabel() != null) {
			this.statements.add(breakStatement);
		}
		return true;
	}

	public boolean containsLabeledStatements() {
		return !this.statements.isEmpty();
	}
}
