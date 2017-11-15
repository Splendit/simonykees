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
	 * @param rule
	 *            the rule used for the calculation
	 * @return the eliminated technical debt as {@link Duration}.
	 */
	public static Duration get(RefactoringRuleInterface rule) {
		return get(rule, RuleApplicationCount.getFor(rule)
			.toInt());
	}

	/**
	 * Gets the technical debt for a specific {@link RefactoringRuleInterface}
	 * by multiplying with a given count.
	 * 
	 * @param rule
	 *            rule to count eliminated technical debt for
	 * @param applicationCount
	 *            times rule was applied
	 * @return the eliminated technical debt as duration
	 */
	public static Duration get(RefactoringRuleInterface rule, int applicationCount) {
		return rule.getRuleDescription()
			.getRemediationCost()
			.multipliedBy(applicationCount);
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
			.map(EliminatedTechnicalDebt::get)
			.reduce(Duration.ZERO, (x, y) -> x.plus(y));
	}
}
