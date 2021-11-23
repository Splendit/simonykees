package eu.jsparrow.ui.preview.model;

import java.time.Duration;

/**
 * Encapsulates overall statistics of running jSparrow on some Java sources.
 * These values are shown in the statistics section of the summary page. In case
 * a Pay-Per-Use license is present, these values are shown in each page of the
 * preview wizard.
 * 
 * @since 4.6.0
 *
 */
public class StatisticsSectionPageModel extends BaseModel {

	private Long runDuration;
	private Integer totalIssuesFixed;
	private Duration totalTimeSaved;
	private Integer totalRequiredCredit;
	private Integer availableCredit;

	public StatisticsSectionPageModel(Long runDuration, Integer totalIssuesFixed, Duration totalTimeSaved,
			Integer totalRequiredCredit, Integer availableCredit) {
		setRunDuration(runDuration);
		setTotalIssuesFixed(totalIssuesFixed);
		setTotalTimeSaved(totalTimeSaved);
		setTotalRequiredCredit(totalRequiredCredit);
		setAvailableCredit(availableCredit);
	}

	public void setRunDuration(Long runDuration) {
		Long oldValue = this.runDuration;
		this.runDuration = runDuration;
		firePropertyChange("runDuration", oldValue, runDuration); //$NON-NLS-1$
	}

	public void setTotalIssuesFixed(Integer totalIssuesFixed) {
		Integer oldValue = this.totalIssuesFixed;
		this.totalIssuesFixed = totalIssuesFixed;
		firePropertyChange("totalIssuesFixed", oldValue, totalIssuesFixed); //$NON-NLS-1$
	}

	public void setTotalTimeSaved(Duration totalTimeSaved) {
		Duration oldValue = this.totalTimeSaved;
		this.totalTimeSaved = totalTimeSaved;
		firePropertyChange("totalTimeSaved", oldValue, totalTimeSaved); //$NON-NLS-1$
	}

	public void setTotalRequiredCredit(Integer totalRequiredCredit) {
		Integer oldValue = this.totalRequiredCredit;
		this.totalRequiredCredit = totalRequiredCredit;
		firePropertyChange("totalRequiredCredit", oldValue, totalRequiredCredit); //$NON-NLS-1$

	}

	public void setAvailableCredit(Integer availableCredit) {
		Integer oldValue = this.availableCredit;
		this.availableCredit = availableCredit;
		firePropertyChange("availableCredit", oldValue, availableCredit); //$NON-NLS-1$
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
