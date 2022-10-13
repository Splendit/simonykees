package eu.jsparrow.rules.java16.switchexpression.ifstatement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.SwitchExpression;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import eu.jsparrow.rules.java16.switchexpression.LabeledBreakStatementsVisitor;
import eu.jsparrow.rules.java16.switchexpression.SwitchCaseBreakStatementsVisitor;
import eu.jsparrow.rules.java16.switchexpression.SwitchCaseClause;
import eu.jsparrow.rules.java16.switchexpression.UseSwitchExpressionASTVisitor;

/**
 * A visitor for replacing {@link IfStatement}s by {@link SwitchExpression}s or
 * rule-labeled {@link SwitchStatement}s. For example, the following code:
 * 
 * 
 * <pre>
 * <code>
 *	if (value.equals("a")) {
 *		System.out.println(1);
 *	} else if (value.equalsList<Expression> caseExpressions = equalsOperationsVisitor.getCaseExpressions();("b")) {
 *		System.out.println(2);
 *	} else {
 *		System.out.println(3);
 *	}
 * </code>
 * </pre>
 * 
 * is transformed to:
 * 
 * <pre>
 * <code> 
 *	switch(value) {
 *	case "a" -> System.out.println(1);
 *	case "b" -> System.out.println(2);
 *	default -> System.out.println(3);
}
 * </code>
 * </pre>
 * 
 * 
 * @since 4.13.0
 *
 */
public class ReplaceMultiBranchIfBySwitchASTVisitor extends UseSwitchExpressionASTVisitor
		implements ReplaceMultiBranchIfBySwitchEvent {

	@Override
	public boolean visit(SwitchStatement switchStatement) {
		return true;
	}

	@Override
	public boolean visit(IfStatement ifStatement) {

		Runnable transformingLambda = findTransformingLambda(ifStatement).orElse(null);

		if (transformingLambda != null) {
			transformingLambda.run();
			addMarkerEvent(ifStatement);
			onRewrite();
			return false;
		}

		return true;
	}

	private Optional<Runnable> findTransformingLambda(IfStatement ifStatement) {

		SwitchHeaderExpressionVisitor switchHeaderExpressionVisitor = new SwitchHeaderExpressionVisitor();
		ifStatement.getExpression()
			.accept(switchHeaderExpressionVisitor);

		SimpleName expectedSwitchHeaderExpression = switchHeaderExpressionVisitor.getSwitchHeaderExpression()
			.orElse(null);
		ITypeBinding expectedOperandType = switchHeaderExpressionVisitor.getSwitchHeaderExpressionType()
			.orElse(null);

		if (expectedSwitchHeaderExpression == null || expectedOperandType == null) {
			return Optional.empty();
		}

		List<IfBranch> ifBranches = collectIfBranchesForSwitch(ifStatement,
				expectedSwitchHeaderExpression, expectedOperandType);

		if (ifBranches.isEmpty()) {
			return Optional.empty();
		}

		Runnable transformingLambda = createTransformingLambda(ifStatement, expectedSwitchHeaderExpression, ifBranches);
		return Optional.of(transformingLambda);
	}

	private static List<IfBranch> collectIfBranchesForSwitch(IfStatement ifStatement,
			SimpleName expectedSwitchHeaderExpression,
			ITypeBinding expectedOperandType) {

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

		List<IfBranch> ifBranches = ifStatementsToIfBranches(ifStatements, expectedSwitchHeaderExpression,
				expectedOperandType);
		if (ifBranches.isEmpty()) {
			return Collections.emptyList();
		}
		if (elseStatement != null) {
			ifBranches.add(new IfBranch(Collections.emptyList(), elseStatement));
		}

		return ifBranches;
	}

	private static List<IfBranch> ifStatementsToIfBranches(List<IfStatement> ifStatements,
			SimpleName expectedSwitchHeaderExpression,
			ITypeBinding expectedOperandType) {
		List<IfBranch> ifBranches = new ArrayList<>();
		final UniqueLiteralValueStore uniqueLiteralValueStore = new UniqueLiteralValueStore();
		for (IfStatement ifStatement : ifStatements) {
			IfBranch ifBranch = ifStatementToIfBranchForSwitch(ifStatement, expectedSwitchHeaderExpression,
					expectedOperandType, uniqueLiteralValueStore)
						.orElse(null);
			if (ifBranch == null) {
				return Collections.emptyList();
			}
			ifBranches.add(ifBranch);
		}
		return ifBranches;
	}

	private static Optional<IfBranch> ifStatementToIfBranchForSwitch(IfStatement ifStatement,
			SimpleName expectedSwitchHeaderExpression, ITypeBinding expectedOperandType,
			UniqueLiteralValueStore uniqueLiteralValueStore) {

		SwitchCaseExpressionsVisitor equalsOperationsVisitor = new SwitchCaseExpressionsVisitor(
				expectedSwitchHeaderExpression, expectedOperandType, uniqueLiteralValueStore);
		ifStatement.getExpression()
			.accept(equalsOperationsVisitor);

		List<Expression> caseExpressions = equalsOperationsVisitor.getCaseExpressions();
		if (caseExpressions.isEmpty()) {
			return Optional.empty();
		}

		return Optional.of(new IfBranch(caseExpressions, ifStatement.getThenStatement()));
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
			if (continueStatementVisitor.isContainingUnsupportedContinueStatement()) {
				return true;
			}

			statementToValidate.accept(yieldStatementVisitor);
			if (yieldStatementVisitor.isContainingUnsupportedYieldStatement()) {
				return true;
			}

			statementToValidate.accept(unsupportedLabelsVisitor);
			if (unsupportedLabelsVisitor.containsLabeledStatements()) {
				return true;
			}
		}
		return false;
	}

	private Runnable createTransformingLambda(Statement statementToReplace, SimpleName switchHeaderExpression,
			List<? extends SwitchCaseClause> clauses) {

		boolean hasDefaultClause = containsDefaultClause(clauses);
		if (hasDefaultClause) {
			Expression variableToAssignSwitchExpression = findVariableToAssignSwitchExpression(clauses)
				.orElse(null);

			if (variableToAssignSwitchExpression != null) {
				VariableDeclarationFragment fragment = findDeclaringFragment(variableToAssignSwitchExpression,
						statementToReplace)
							.orElse(null);

				if (fragment != null) {
					return () -> replaceByInitializationWithSwitch(statementToReplace, switchHeaderExpression, clauses,
							fragment);
				}

				return () -> replaceByAssignmentWithSwitch(variableToAssignSwitchExpression, statementToReplace,
						switchHeaderExpression, clauses);
			}

			if (areReturningValue(clauses)) {
				return () -> replaceByReturnWithSwitch(statementToReplace, switchHeaderExpression, clauses);
			}
		}
		return () -> replaceBySwitchStatement(statementToReplace, switchHeaderExpression, clauses);
	}
}
