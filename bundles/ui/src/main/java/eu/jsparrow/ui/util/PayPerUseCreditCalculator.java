package eu.jsparrow.ui.util;

import java.util.List;

import eu.jsparrow.license.api.LicenseValidationResult;
import eu.jsparrow.rules.common.RefactoringRule;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.statistics.RuleApplicationCount;

public class PayPerUseCreditCalculator {

	private LicenseUtil licenseUtil = LicenseUtil.get();

	public boolean validateCredit(List<RefactoringRule> rulesWithChanges) {
		LicenseValidationResult result = licenseUtil.getValidationResult();
		int sum = findTotalRequiredCredit(rulesWithChanges);
		int availableCredit = result.getCredit()
			.orElse(sum);
		return sum <= availableCredit;
	}

	public int findTotalRequiredCredit(List<RefactoringRule> rulesWithChanges) {
		return rulesWithChanges
			.stream()
			.mapToInt(this::measureWeight)
			.sum();
	}

	public int measureWeight(RefactoringRule rule) {
		RuleApplicationCount numIssues = RuleApplicationCount.getFor(rule);
		RuleDescription description = rule.getRuleDescription();
		return numIssues.toInt() * description.getCredit();
	}

}
