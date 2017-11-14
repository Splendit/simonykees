package eu.jsparrow.core.rule.statistics;

import java.time.Duration;
import java.util.List;

import eu.jsparrow.core.rule.RefactoringRuleInterface;

/**
 * This class represents the technical debt that has been eliminated by applying
 * a specific rule to a project.
 * 
 * @author Hans-Jörg Schrödl
 * @since 2.3.0
 */
public class EliminatedTechnicalDebt {

	private EliminatedTechnicalDebt() {
	}

	/**
	 * Gets the eliminated technical debt as {@link Duration} by multiplying the
	 * rule remediation cost with the times the rule has been applied.
	 * 
	 * @param rule the rule used for the calculation
	 * @return the eliminated technical debt as {@link Duration}.
	 */
	public static Duration get(RefactoringRuleInterface rule) {
		return rule.getRuleDescription()
			.getRemediationCost()
			.multipliedBy(RuleApplicationCount.getFor(rule)
				.toInt());
	}
	
	/**
	 * Returns the sum of the given technical debts.
	 * 
	 * @param list
	 *            debts to sum up.
	 * @return the sum of the technical debts as duration.
	 */
	public static Duration getTotalFor(List<? extends RefactoringRuleInterface> list) {
		return list.stream()
			.map(x -> EliminatedTechnicalDebt.get(x))
			.reduce(Duration.ZERO, (x, y) -> x.plus(y));
	}
}
