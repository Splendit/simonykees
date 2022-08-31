package eu.jsparrow.rules.java16.switchexpression.ifstatement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.eclipse.jdt.core.dom.ASTMatcher;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.Statement;

import eu.jsparrow.rules.common.util.ClassRelationUtil;

/**
 * 
 * @since 4.13.0
 *
 */
public class ReplaceMultiBranchIfBySwitchAnalyzer {

	private static final ASTMatcher AST_MATCHER = new ASTMatcher();

	private static final List<String> TYPES_FOR_NUMBER_LITERAL = Collections.unmodifiableList(Arrays.asList(
			int.class.getName(),
			long.class.getName()));

	static List<IfBranch> collectIfBranchesForSwitch(IfStatement ifStatement,
			SwitchHeaderExpressionData variableAnalysisData) {

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
			SwitchHeaderExpressionData variableData) {

		EqualsOperationForSwitchVisitor equalsOperationsVisitor = new EqualsOperationForSwitchVisitor();
		ifStatement.getExpression()
			.accept(equalsOperationsVisitor);
		List<EqualsOperationForSwitch> equalsOperations = equalsOperationsVisitor.getEqualsOperations();
		if (equalsOperations.isEmpty()) {
			return Optional.empty();
		}

		List<Expression> caseExpressions = findCaseExpressions(equalsOperations, variableData);
		if (caseExpressions.isEmpty()) {
			return Optional.empty();
		}

		return Optional.of(new IfBranch(caseExpressions, ifStatement.getThenStatement()));
	}

	private static Optional<Expression> findCaseExpression(EqualsOperationForSwitch equalsOperation,
			SwitchHeaderExpressionData switchHeaderExpressionData) {

		if (!AST_MATCHER.match(switchHeaderExpressionData.getSwitchHeaderExpression(), equalsOperation.getSwitchHeaderExpression())) {
			return Optional.empty();
		}

		Expression caseExpression = equalsOperation.getCaseExpression();

		if (equalsOperation.getOperationNodeType() == ASTNode.INFIX_EXPRESSION) {
			ITypeBinding expectedOperandType = switchHeaderExpressionData.getSwitchHeaderExpressionType();
			if (ClassRelationUtil.isContentOfType(expectedOperandType, char.class.getName())
					&& caseExpression.getNodeType() == ASTNode.CHARACTER_LITERAL) {
				return Optional.of(caseExpression);
			}
			if (caseExpression.getNodeType() == ASTNode.NUMBER_LITERAL) {
				if (!ClassRelationUtil.isContentOfTypes(expectedOperandType, TYPES_FOR_NUMBER_LITERAL)) {
					return Optional.empty();
				}
				ITypeBinding caseExpressionType = equalsOperation.getCaseExpression()
					.resolveTypeBinding();
				if (!ClassRelationUtil.compareITypeBinding(expectedOperandType, caseExpressionType)) {
					return Optional.empty();
				}
				return Optional.of(caseExpression);
			}
		}

		if (equalsOperation.getOperationNodeType() == ASTNode.METHOD_INVOCATION
				&& caseExpression.getNodeType() == ASTNode.STRING_LITERAL) {
			return Optional.of(caseExpression);
		}

		return Optional.empty();
	}

	static List<Expression> findCaseExpressions(List<EqualsOperationForSwitch> equalsOperations,
			SwitchHeaderExpressionData variableData) {
		List<Expression> caseExpressions = new ArrayList<>();
		for (EqualsOperationForSwitch equalsOperation : equalsOperations) {

			Expression caseExpression = findCaseExpression(equalsOperation, variableData).orElse(null);
			if (caseExpression == null) {
				return Collections.emptyList();
			}
			caseExpressions.add(caseExpression);
		}
		return caseExpressions;
	}

	private ReplaceMultiBranchIfBySwitchAnalyzer() {
		// private default constructor hiding implicit public one
	}

}
