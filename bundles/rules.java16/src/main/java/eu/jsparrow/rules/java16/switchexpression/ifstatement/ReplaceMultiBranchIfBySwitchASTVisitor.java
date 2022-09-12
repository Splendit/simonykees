package eu.jsparrow.rules.java16.switchexpression.ifstatement;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.SwitchExpression;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import eu.jsparrow.rules.java16.switchexpression.LabeledBreakStatementsVisitor;
import eu.jsparrow.rules.java16.switchexpression.SwitchCaseBreakStatementsVisitor;
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
 *	} else if (value.equals("b")) {
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

		MultiBranchIfStatement multiBranchIfStatement = MultiBranchIfStatement.toMultiBranchIfStatement(ifStatement)
			.orElse(null);
		if (multiBranchIfStatement == null) {
			return Optional.empty();
		}

		if (containsUnsupportedStatementOrLabel(multiBranchIfStatement)) {
			return Optional.empty();
		}

		SwitchHeaderExpressionData variableDataForSwitch = findSwitchHeaderExpressionData(multiBranchIfStatement)
			.orElse(null);
		if (variableDataForSwitch == null) {
			return Optional.empty();
		}

		List<IfBranch> ifBranches = ReplaceMultiBranchIfBySwitchAnalyzer.collectIfBranchesForSwitch(ifStatement,
				variableDataForSwitch);

		if (ifBranches.isEmpty()) {
			return Optional.empty();
		}

		SimpleName switchHeaderExpression = variableDataForSwitch.getSwitchHeaderExpression();

		Runnable transformingLambda = createTransformingLambda(ifStatement, switchHeaderExpression, ifBranches);
		return Optional.of(transformingLambda);
	}

	private static boolean containsUnsupportedStatementOrLabel(
			MultiBranchIfStatement multiBranchIfStatement) {
		List<Statement> statementsToAnalyze = new ArrayList<>();
		statementsToAnalyze.add(multiBranchIfStatement.getIfStatement()
			.getThenStatement());
		multiBranchIfStatement.getElseIfStatements()
			.forEach(statementsToAnalyze::add);
		multiBranchIfStatement.getLastElseStatement()
			.ifPresent(statementsToAnalyze::add);

		SwitchCaseBreakStatementsVisitor breakStatementVisitor = new SwitchCaseBreakStatementsVisitor();
		ContinueStatementWithinIfVisitor continueStatementVisitor = new ContinueStatementWithinIfVisitor();
		YieldStatementWithinIfVisitor yieldStatementVisitor = new YieldStatementWithinIfVisitor();
		LabeledBreakStatementsVisitor unsupportedLabelsVisitor = new LabeledBreakStatementsVisitor();

		for (Statement statement : statementsToAnalyze) {
			statement.accept(breakStatementVisitor);
			boolean containsBreakStatement = !breakStatementVisitor.getBreakStatements()
				.isEmpty();
			if (containsBreakStatement) {
				return true;
			}

			statement.accept(continueStatementVisitor);
			if (continueStatementVisitor.isContainingContinueStatement()) {
				return true;
			}

			statement.accept(yieldStatementVisitor);
			if (yieldStatementVisitor.isContainingYieldStatement()) {
				return true;
			}

			statement.accept(unsupportedLabelsVisitor);
			if (unsupportedLabelsVisitor.containsLabeledStatements()) {
				return true;
			}
		}
		return false;
	}

	private Optional<SwitchHeaderExpressionData> findSwitchHeaderExpressionData(
			MultiBranchIfStatement multiBranchIfStatement) {
		Expression ifExpression = multiBranchIfStatement.getIfStatement()
			.getExpression();

		EqualsOperationForSwitchVisitor equalsOperationsVisitor = new EqualsOperationForSwitchVisitor();
		ifExpression.accept(equalsOperationsVisitor);
		return equalsOperationsVisitor.getEqualsOperations()
			.stream()
			.findFirst()
			.flatMap(SwitchHeaderExpressionData::findSwitchHeaderExpressionData);
	}

	private Runnable createTransformingLambda(IfStatement ifStatement, SimpleName switchHeaderExpression,
			List<IfBranch> ifBranches) {

		if (isMultiBranchIfEndingWithElse(ifBranches)) {

			Expression variableAssignedByFirstBranch = ifBranches.get(0)
				.findAssignedVariable()
				.orElse(null);

			if (variableAssignedByFirstBranch != null && areAllAssigningToSameVariable(ifBranches)) {
				VariableDeclarationFragment fragment = findDeclaringFragment(variableAssignedByFirstBranch, ifStatement)
					.orElse(null);

				if (fragment != null) {
					return () -> replaceByInitializationWithSwitch(ifStatement, switchHeaderExpression, ifBranches,
							fragment);
				}

				return () -> replaceByAssignmentWithSwitch(variableAssignedByFirstBranch, ifStatement,
						switchHeaderExpression, ifBranches);
			}

			if (areReturningValue(ifBranches)) {
				return () -> replaceByReturnWithSwitch(ifStatement, switchHeaderExpression, ifBranches);
			}
		}

		return () -> replaceBySwitchStatement(ifStatement, switchHeaderExpression, ifBranches);
	}

	private boolean isMultiBranchIfEndingWithElse(List<IfBranch> ifBranches) {
		int lastBranchIndex = ifBranches.size() - 1;
		IfBranch lastBranch = ifBranches.get(lastBranchIndex);
		return lastBranch.getExpressions()
			.isEmpty();
	}
}
