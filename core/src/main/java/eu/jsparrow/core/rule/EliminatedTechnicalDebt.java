package eu.jsparrow.core.rule;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;

import eu.jsparrow.core.visitor.AbstractASTRewriteASTVisitor;

/**
 * This class represents the technical debt that has been eliminated by applying
 * a specific rule to a project.
 * 
 * @author Hans-Jörg Schrödl
 *
 */
public class EliminatedTechnicalDebt {

	private RefactoringRuleInterface rule;

	public EliminatedTechnicalDebt(RefactoringRuleInterface rule) {
		this.rule = rule;
	}

	/**
	 * Gets the eliminated technical debt as {@link Duration} by multiplying the
	 * rule remediation cost with the times the rule has been applied.
	 * 
	 * @return the eliminated technical debt as {@link Duration}.
	 */
	public Duration get() {
		return rule.getRuleDescription()
			.getRemediationCost()
			.multipliedBy(RuleApplicationCount.getFor(rule)
				.toInt());
	}

	/**
	 * Returns the sum of the given technical debts.
	 * 
	 * @param list debts to sum up.
	 * @return the sum of the technical debts as duration.
	 */
	public static Duration getTotalFor(List<? extends RefactoringRuleInterface> list) {
		Duration total =  list
			.stream()
			.map(x -> new EliminatedTechnicalDebt(x).get())
			.reduce(Duration.ZERO, (x, y) -> x.plus(y));
		return total;
	}
}
