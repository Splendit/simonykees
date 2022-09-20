package eu.jsparrow.rules.java16.switchexpression.ifstatement;

import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.Statement;

import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.java16.switchexpression.SwitchCaseClause;

/**
 * Stores informations about
 * <ul>
 * <li>a then clause of a multi-branch if statement or</li>
 * <li>a then clause of an else if clause of a multi-branch if statement or</li>
 * <li>the else clause of the last else if clause of a a multi-branch if
 * statement</li>
 * </ul>
 * which are necessary for the transformation of a multi-branch if statement to
 * a switch expression or to a switch statement.
 * 
 * @since 4.3.0
 *
 */
class IfBranch extends SwitchCaseClause {

	private static List<Statement> toStatementList(Statement statement) {
		if (statement.getNodeType() == ASTNode.BLOCK) {
			Block block = (Block) statement;
			return ASTNodeUtil.convertToTypedList(block.statements(), Statement.class);
		}
		return Collections.singletonList(statement);
	}

	public IfBranch(List<Expression> expressionsForSwitchCase, Statement statement) {
		super(expressionsForSwitchCase, toStatementList(statement), Collections.emptyList());
	}

	@Override
	public boolean hasInternalBreakStatements() {
		return false;
	}

}