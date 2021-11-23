package eu.jsparrow.ui.preview.model;

import eu.jsparrow.rules.common.RefactoringRule;
import eu.jsparrow.ui.util.PayPerUseCreditCalculator;

/**
 * Adds required credit by one rule to the values encapsulated in {@link RuleStatisticsSectionPageModel}. 
 * 
 * @since 4.6.0
 *
 */
public class PayPerUseRuleStatisticsSectionPageModel extends RuleStatisticsSectionPageModel {

	private Integer requiredCredit;

	public PayPerUseRuleStatisticsSectionPageModel(RefactoringRule rule) {
		super(rule);

		PayPerUseCreditCalculator payPerUsecalculator = new PayPerUseCreditCalculator();
		int measuredCredit = payPerUsecalculator.measureWeight(rule);
		setRequiredCredit(measuredCredit);
	}

	public Integer getRequiredCredit() {
		return this.requiredCredit;
	}

	public void setRequiredCredit(Integer requiredCredit) {
		Integer oldValue = this.requiredCredit;
		this.requiredCredit = requiredCredit;
		firePropertyChange("requiredCredit", oldValue, requiredCredit); //$NON-NLS-1$
	}
}
