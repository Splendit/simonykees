package eu.jsparrow.rules.java16.switchexpression.ifstatement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.eclipse.jdt.core.dom.ASTMatcher;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CharacterLiteral;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.StringLiteral;

import eu.jsparrow.rules.common.util.ClassRelationUtil;
import eu.jsparrow.rules.java16.switchexpression.LabeledBreakStatementsVisitor;
import eu.jsparrow.rules.java16.switchexpression.SwitchCaseBreakStatementsVisitor;

/**
 * 
 * @since 4.13.0
 *
 */
public class ReplaceMultiBranchIfBySwitchAnalyzer {

	private static final ASTMatcher AST_MATCHER = new ASTMatcher();

	static List<IfBranch> collectIfBranchesForSwitch(IfStatement ifStatement,
			SwitchHeaderExpressionData variableAnalysisData) {

		List<IfStatement> ifStatements = new ArrayList<>();
		ifStatements.add(ifStatement);
		Statement elseStatement = ifStatement.getElseStatement();
		while (elseStatement != null && elseStatement.getNodeType() == ASTNode.IF_STATEMENT) {
			IfStatement eliseIfStatement = (IfStatement) elseStatement;
			ifStatements.add(eliseIfStatement);
			elseStatement = eliseIfStatement.getElseStatement();
		}
		int minimalIfStatementsCount;
		if (elseStatement != null) {
			minimalIfStatementsCount = 2;
		} else {
			minimalIfStatementsCount = 3;
		}
		if (ifStatements.size() < minimalIfStatementsCount) {
			return Collections.emptyList();
		}

		List<Statement> statementsToValidate = new ArrayList<>();
		ifStatements.stream()
			.map(IfStatement::getThenStatement)
			.forEach(statementsToValidate::add);
		if (elseStatement != null) {
			statementsToValidate.add(elseStatement);
		}

		if (containsUnsupportedStatementOrLabel(statementsToValidate)) {
			return Collections.emptyList();
		}

		UniqueLiteralValues uniqueLiteralValuesStore = new UniqueLiteralValues();

		List<IfBranch> ifBranches = ifStatementsToIfBranches(ifStatements, variableAnalysisData,
				uniqueLiteralValuesStore);
		if (ifBranches.isEmpty()) {
			return Collections.emptyList();
		}
		if (elseStatement != null) {
			ifBranches.add(new IfBranch(Collections.emptyList(), elseStatement));
		}

		return ifBranches;
	}

	private static List<IfBranch> ifStatementsToIfBranches(List<IfStatement> ifStatements,
			SwitchHeaderExpressionData variableAnalysisData, UniqueLiteralValues uniqueLiteralValuesStore) {
		List<IfBranch> ifBranches = new ArrayList<>();
		for (IfStatement ifStatement : ifStatements) {
			IfBranch ifBranch = ifStatementToIfBranchForSwitch(ifStatement, variableAnalysisData,
					uniqueLiteralValuesStore)
						.orElse(null);
			if (ifBranch == null) {
				return Collections.emptyList();
			}
			ifBranches.add(ifBranch);
		}
		return ifBranches;
	}

	private static Optional<IfBranch> ifStatementToIfBranchForSwitch(IfStatement ifStatement,
			SwitchHeaderExpressionData variableData, UniqueLiteralValues uniqueLiteralValues) {

		EqualsOperationForSwitchVisitor equalsOperationsVisitor = new EqualsOperationForSwitchVisitor();
		ifStatement.getExpression()
			.accept(equalsOperationsVisitor);
		List<EqualsOperationForSwitch> equalsOperations = equalsOperationsVisitor.getEqualsOperations();
		if (equalsOperations.isEmpty()) {
			return Optional.empty();
		}

		List<Expression> caseExpressions = findCaseExpressions(variableData, equalsOperations, uniqueLiteralValues);
		if (caseExpressions.isEmpty()) {
			return Optional.empty();
		}

		return Optional.of(new IfBranch(caseExpressions, ifStatement.getThenStatement()));
	}

	private static boolean isSupportedCharacter(Expression caseExpression, UniqueLiteralValues uniqueLiteralValues) {
		if (caseExpression.getNodeType() != ASTNode.CHARACTER_LITERAL) {
			return false;
		}
		CharacterLiteral characterLiteral = (CharacterLiteral) caseExpression;
		Character characterValue = Character.valueOf(characterLiteral.charValue());
		return uniqueLiteralValues.isUnique(characterValue);
	}

	private static boolean isSupportedInteger(Expression caseExpression, UniqueLiteralValues uniqueLiteralValues) {
		Integer integerValue;
		try {
			integerValue = ExpressionToNumericToken.expressionToInteger(caseExpression)
				.orElse(null);
		} catch (NumberFormatException exc) {
			integerValue = null;
		}
		if (integerValue != null) {
			return uniqueLiteralValues.isUnique(integerValue);
		}
		return false;
	}

	private static boolean isSupportedString(Expression caseExpression, UniqueLiteralValues uniqueLiteralValues) {
		if (caseExpression.getNodeType() != ASTNode.STRING_LITERAL) {
			return false;
		}
		StringLiteral stringLiteral = (StringLiteral) caseExpression;
		return uniqueLiteralValues.isUnique(stringLiteral.getLiteralValue());
	}

	private static Optional<Expression> findCaseExpression(
			SwitchHeaderExpressionData switchHeaderExpressionData, EqualsOperationForSwitch equalsOperation,
			UniqueLiteralValues uniqueLiteralValues) {

		ITypeBinding expectedOperandType = switchHeaderExpressionData.getSwitchHeaderExpressionType();
		Optional<Expression> optionalReturnValue = Optional.of(equalsOperation.getCaseExpression());
		if (equalsOperation.getOperationNodeType() == ASTNode.INFIX_EXPRESSION) {

			if (ClassRelationUtil.isContentOfType(expectedOperandType, char.class.getName())) {
				return optionalReturnValue.filter(expression -> isSupportedCharacter(expression, uniqueLiteralValues));
			}
			if (ClassRelationUtil.isContentOfType(expectedOperandType, int.class.getName())) {
				return optionalReturnValue.filter(expression -> isSupportedInteger(expression, uniqueLiteralValues));
			}
		}
		if (equalsOperation.getOperationNodeType() == ASTNode.METHOD_INVOCATION &&
				ClassRelationUtil.isContentOfType(expectedOperandType, java.lang.String.class.getName())) {
			return optionalReturnValue.filter(expression -> isSupportedString(expression, uniqueLiteralValues));
		}
		return Optional.empty();
	}

	static List<Expression> findCaseExpressions(SwitchHeaderExpressionData variableData,
			List<EqualsOperationForSwitch> equalsOperations,
			UniqueLiteralValues uniqueLiteralValues) {

		SimpleName expectedSwitchHeaderExpression = variableData.getSwitchHeaderExpression();
		boolean allVariableNamesMatching = equalsOperations.stream()
			.map(EqualsOperationForSwitch::getSwitchHeaderExpression)
			.allMatch(simpleName -> AST_MATCHER.match(expectedSwitchHeaderExpression, simpleName));

		if (!allVariableNamesMatching) {
			return Collections.emptyList();
		}

		List<Expression> caseExpressions = new ArrayList<>();
		for (EqualsOperationForSwitch equalsOperation : equalsOperations) {

			Expression caseExpression = findCaseExpression(variableData, equalsOperation, uniqueLiteralValues)
				.orElse(null);
			if (caseExpression == null) {
				return Collections.emptyList();
			}
			caseExpressions.add(caseExpression);
		}
		return caseExpressions;
	}

	private static boolean containsUnsupportedStatementOrLabel(List<Statement> statementsToValidate) {
		SwitchCaseBreakStatementsVisitor breakStatementVisitor = new SwitchCaseBreakStatementsVisitor();
		ContinueStatementWithinIfVisitor continueStatementVisitor = new ContinueStatementWithinIfVisitor();
		YieldStatementWithinIfVisitor yieldStatementVisitor = new YieldStatementWithinIfVisitor();
		LabeledBreakStatementsVisitor unsupportedLabelsVisitor = new LabeledBreakStatementsVisitor();

		for (Statement statementToValidate : statementsToValidate) {
			statementToValidate.accept(breakStatementVisitor);
			boolean containsBreakStatement = !breakStatementVisitor.getBreakStatements()
				.isEmpty();
			if (containsBreakStatement) {
				return true;
			}
			statementToValidate.accept(continueStatementVisitor);
			if (continueStatementVisitor.isContainingContinueStatement()) {
				return true;
			}

			statementToValidate.accept(yieldStatementVisitor);
			if (yieldStatementVisitor.isContainingYieldStatement()) {
				return true;
			}

			statementToValidate.accept(unsupportedLabelsVisitor);
			if (unsupportedLabelsVisitor.containsLabeledStatements()) {
				return true;
			}
		}
		return false;
	}

	private ReplaceMultiBranchIfBySwitchAnalyzer() {
		// private default constructor hiding implicit public one
	}

}
