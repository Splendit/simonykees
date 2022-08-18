package eu.jsparrow.rules.java16.switchexpression;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.Statement;

/**
 * 
 * @since 4.13.0
 *
 */
public class ReplaceMultiBranchIfBySwitchAnalyzer {

	static List<IfBranch> collectIfBranchesForSwitch(IfStatement ifStatement,
			VariableForSwitchAnalysisData variableAnalysisData) {

		List<IfBranch> ifBranches = new ArrayList<>();
		IfBranch ifBranch = ifStatementToIfBranchForSwitch(ifStatement, variableAnalysisData).orElse(null);
		if (ifBranch == null) {
			return Collections.emptyList();
		}
		ifBranches.add(ifBranch);

		Statement elseStatement = ifStatement.getElseStatement();
		while (elseStatement != null && elseStatement.getNodeType() == ASTNode.IF_STATEMENT) {

			IfStatement eliseIfStatement = (IfStatement) elseStatement;
			ifBranch = ifStatementToIfBranchForSwitch(eliseIfStatement, variableAnalysisData).orElse(null);
			if (ifBranch == null) {
				return Collections.emptyList();
			}
			ifBranches.add(ifBranch);
			elseStatement = eliseIfStatement.getElseStatement();
		}

		if (elseStatement != null) {
			ifBranches.add(new IfBranch(Collections.emptyList(), elseStatement));
		}

		int countOfBranches = ifBranches.size();
		if (countOfBranches < 3) {
			return Collections.emptyList();
		}
		return ifBranches;
	}

	private static Optional<IfBranch> ifStatementToIfBranchForSwitch(IfStatement ifStatement,
			VariableForSwitchAnalysisData variableData) {
		List<Expression> expressionsForSwitchCase = IfExpressionAnalyzer.findCaseExpressions(variableData, ifStatement);
		if (expressionsForSwitchCase.isEmpty()) {
			return Optional.empty();
		}
		return Optional.of(new IfBranch(expressionsForSwitchCase, ifStatement.getThenStatement()));
	}

	private ReplaceMultiBranchIfBySwitchAnalyzer() {
		// private default constructor hiding implicit public one
	}

}
