package eu.jsparrow.ui.preview.model;

import java.time.Duration;

import eu.jsparrow.rules.common.RefactoringRule;
import eu.jsparrow.rules.common.statistics.EliminatedTechnicalDebt;
import eu.jsparrow.rules.common.statistics.RuleApplicationCount;

public class RuleStatisticsAreaPageModel extends BaseModel {

	private Integer issuesFixed;
	private Duration timeSaved;

	public RuleStatisticsAreaPageModel(RefactoringRule rule) {
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
		firePropertyChange("issuesFixed", this.issuesFixed, this.issuesFixed = issuesFixed); //$NON-NLS-1$
	}

	public void setTimeSaved(Duration timeSaved) {
		firePropertyChange("timeSaved", this.timeSaved, this.timeSaved = timeSaved); //$NON-NLS-1$
	}

}
