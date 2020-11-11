package eu.jsparrow.standalone.report.model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * A model representing the data printed into a jSparrow report.
 * 
 * @since 3.23.0
 *
 */
public class ReportData {

	private String projectName;
	private LocalDate date;
	private int totalIssuesFixed;
	private int totalFilesCount;
	private int totalFilesChanged;
	private long totalTimeSaved;
	private List<RuleDataModel> ruleDataModels;

	public ReportData(String projectName, LocalDate date, int totalIssuesFixed, int totalFilesCount,
			int totalFilesChanged,
			long totalTimeSaved, List<RuleDataModel> ruleDataModels) {
		this.projectName = projectName;
		this.date = date;
		this.totalIssuesFixed = totalIssuesFixed;
		this.totalFilesCount = totalFilesCount;
		this.totalFilesChanged = totalFilesChanged;
		this.totalTimeSaved = totalTimeSaved;
		this.ruleDataModels = ruleDataModels;
	}

	public String getProjectName() {
		return projectName;
	}

	public String getDate() {
		DateTimeFormatter datePattern = DateTimeFormatter.ofPattern("dd.MM.yyyy"); //$NON-NLS-1$
		return date.format(datePattern);
	}

	public int getTotalIssuesFixed() {
		return totalIssuesFixed;
	}

	public int getTotalFilesCount() {
		return totalFilesCount;
	}

	public int getTotalFilesChanged() {
		return totalFilesChanged;
	}

	public long getTotalTimeSaved() {
		return totalTimeSaved;
	}

	public List<RuleDataModel> getRuleDataModels() {
		return ruleDataModels;
	}

}
