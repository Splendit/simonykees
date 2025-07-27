package eu.jsparrow.ui.util;

import java.util.List;

import eu.jsparrow.license.api.LicenseValidationResult;
import eu.jsparrow.rules.common.RefactoringRule;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.statistics.RuleApplicationCount;

/**
 * Contains functionalities to calculate the required credit for the changes
 * computed by a list of rules.
 * 
 * @since 4.6.0
 */
public class PayPerUseCreditCalculator {

	private LicenseUtilService licenseUtil = LicenseUtil.get();

	/**
	 * Uses the {@link LicenseUtil} to verify whether the available credit in a
	 * Pay-Per-Use license is bigger than or equal to the credit required for
	 * the changes computed by the given rules.
	 * 
	 * @param rulesWithChanges
	 *            rules used to compute the changes.
	 * @return if the condition above is satisfied.
	 */
	public boolean validateCredit(List<RefactoringRule> rulesWithChanges) {
		LicenseValidationResult result = licenseUtil.getValidationResult();
		int sum = findTotalRequiredCredit(rulesWithChanges);
		int availableCredit = result.getCredit()
			.orElse(sum);
		return sum <= availableCredit;
	}

	/**
	 * 
	 * @param rulesWithChanges
	 *            a list of rules that have computed some changes.
	 * @return the total required credit for the changes computed by the given
	 *         set of rules. Uses {@link RuleApplicationCount} to measure the
	 *         credit for each rule.
	 */
	public int findTotalRequiredCredit(List<RefactoringRule> rulesWithChanges) {
		return rulesWithChanges
			.stream()
			.mapToInt(this::measureCredit)
			.sum();
	}

	/**
	 * 
	 * @param rule
	 * @return the required credit for the changes computed by the given rule.
	 *         Uses {@link RuleApplicationCount} to get the count of issues
	 *         fixed.
	 */
	public int measureCredit(RefactoringRule rule) {
		RuleApplicationCount numIssues = RuleApplicationCount.getFor(rule);
		RuleDescription description = rule.getRuleDescription();
		return numIssues.toInt() * description.getCredit();
	}

}
