package eu.jsparrow.ui.preview.model;

import java.time.Duration;

import eu.jsparrow.rules.common.RefactoringRule;
import eu.jsparrow.rules.common.statistics.EliminatedTechnicalDebt;
import eu.jsparrow.rules.common.statistics.RuleApplicationCount;

/**
 * Encapsulates the number of issues fixed and the duration of the time saved by
 * one rule. These values are shown as rule statistics in the preview wizard.
 * 
 * @since 4.6.0
 *
 */
public class RuleStatisticsSectionPageModel extends BaseModel {

	private Integer issuesFixed;
	private Duration timeSaved;

	public RuleStatisticsSectionPageModel(RefactoringRule rule) {
		setIssuesFixed(RuleApplicationCount.getFor(rule)
			.toInt());
		setTimeSaved(EliminatedTechnicalDebt.get(rule));
	}

	public Integer getIssuesFixed() {
		return issuesFixed;
	}

	public Duration getTimeSaved() {
		return timeSaved;
	}

	public void setIssuesFixed(Integer issuesFixed) {
		Integer oldValue = this.issuesFixed;
		this.issuesFixed = issuesFixed;
		firePropertyChange("issuesFixed", oldValue, issuesFixed); //$NON-NLS-1$
	}

	public void setTimeSaved(Duration timeSaved) {
		Duration oldValue = this.timeSaved;
		this.timeSaved = timeSaved;
		firePropertyChange("timeSaved", oldValue, timeSaved); //$NON-NLS-1$
	}

}
