package eu.jsparrow.ui.preview.model;

import java.time.Duration;

public class StatisticsAreaPageModel extends BaseModel {

	private Long runDuration;
	private Integer totalIssuesFixed;
	private Duration totalTimeSaved;
	private Integer totalRequiredCredit;
	private Integer availableCredit;

	public StatisticsAreaPageModel(Long runDuration, Integer totalIssuesFixed, Duration totalTimeSaved,
			Integer totalRequiredCredit, Integer availableCredit) {
		setRunDuration(runDuration);
		setTotalIssuesFixed(totalIssuesFixed);
		setTotalTimeSaved(totalTimeSaved);
		setTotalRequiredCredit(totalRequiredCredit);
		setAvailableCredit(availableCredit);
	}

	public void setRunDuration(Long runDuration) {
		firePropertyChange("runDuration", this.runDuration, this.runDuration = runDuration); //$NON-NLS-1$
	}

	public void setTotalIssuesFixed(Integer totalIssuesFixed) {
		firePropertyChange("totalIssuesFixed", this.totalIssuesFixed, this.totalIssuesFixed = totalIssuesFixed); //$NON-NLS-1$
	}

	public void setTotalTimeSaved(Duration totalTimeSaved) {
		firePropertyChange("totalTimeSaved", this.totalTimeSaved, this.totalTimeSaved = totalTimeSaved); //$NON-NLS-1$
	}

	public void setTotalRequiredCredit(Integer totalRequiredCredit) {
		firePropertyChange("totalRequiredCredit", this.totalRequiredCredit, //$NON-NLS-1$
				this.totalRequiredCredit = totalRequiredCredit);
	}

	public void setAvailableCredit(Integer availableCredit) {
		firePropertyChange("availableCredit", this.availableCredit, this.availableCredit = availableCredit); //$NON-NLS-1$
	}

	public Long getRunDuration() {
		return runDuration;
	}

	public Integer getTotalIssuesFixed() {
		return totalIssuesFixed;
	}

	public Duration getTotalTimeSaved() {
		return totalTimeSaved;
	}

	public Integer getTotalRequiredCredit() {
		return totalRequiredCredit;
	}

	public Integer getAvailableCredit() {
		return availableCredit;
	}
}
