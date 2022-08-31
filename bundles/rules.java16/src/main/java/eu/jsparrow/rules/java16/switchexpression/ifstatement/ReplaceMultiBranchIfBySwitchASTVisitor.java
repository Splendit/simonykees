package eu.jsparrow.rules.java16.switchexpression.ifstatement;

import java.util.List;

import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.SwitchExpression;
import org.eclipse.jdt.core.dom.SwitchStatement;

import eu.jsparrow.rules.common.visitor.AbstractASTRewriteASTVisitor;

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
public class ReplaceMultiBranchIfBySwitchASTVisitor extends AbstractASTRewriteASTVisitor
		implements ReplaceMultiBranchIfBySwitchEvent {

	@Override
	public boolean visit(IfStatement ifStatement) {

		EqualsOperationForSwitchVisitor equalsOperationsVisitor = new EqualsOperationForSwitchVisitor();
		ifStatement.getExpression()
			.accept(equalsOperationsVisitor);
		List<EqualsOperationForSwitch> equalsOperations = equalsOperationsVisitor.getEqualsOperations();
		SwitchHeaderExpressionData variableDataForSwitch = null;
		if (!equalsOperations.isEmpty()) {
			variableDataForSwitch = SwitchHeaderExpressionData.findSwitchHeaderExpressionData(equalsOperations.get(0))
				.orElse(null);
		}

		if (variableDataForSwitch != null) {

			List<IfBranch> ifBranches = ReplaceMultiBranchIfBySwitchAnalyzer.collectIfBranchesForSwitch(ifStatement,
					variableDataForSwitch);
			if (!ifBranches.isEmpty()) {
				transform(ifStatement, variableDataForSwitch, ifBranches);
				return false;
				// ??? would it also be possible to return true ???
				// --> test it !!!
			}
		}
		return true;
	}

	private void transform(IfStatement ifStatement, SwitchHeaderExpressionData variableDataForSwitch,
			List<IfBranch> ifBranches) {
		ifStatement.getExpression();
		variableDataForSwitch.toString();
		ifBranches.toString();
	}
}
