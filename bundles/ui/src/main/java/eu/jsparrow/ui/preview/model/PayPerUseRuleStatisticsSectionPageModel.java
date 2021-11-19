package eu.jsparrow.ui.preview.model;

import eu.jsparrow.rules.common.RefactoringRule;
import eu.jsparrow.ui.util.PayPerUseCreditCalculator;

public class PayPerUseRuleStatisticsSectionPageModel extends RuleStatisticsSectionPageModel {

	public PayPerUseRuleStatisticsSectionPageModel(RefactoringRule rule) {
		super(rule);

		PayPerUseCreditCalculator payPerUsecalculator = new PayPerUseCreditCalculator();
		int measuredCredit = payPerUsecalculator.measureWeight(rule);
		setRequiredCredit(measuredCredit);
	}

	private Integer requiredCredit;

	public Integer getRequiredCredit() {
		return this.requiredCredit;
	}

	public void setRequiredCredit(Integer requiredCredit) {
		firePropertyChange("requiredCredit", this.requiredCredit, this.requiredCredit = requiredCredit); //$NON-NLS-1$
	}
}
