package eu.jsparrow.core.statistic.entity;

import java.util.List;

public class JsparrowData {

	private long durationOfCalculation;
	private long totalTimeSaved;
	private int totalIssuesFixed;
	private int filesChanged;
	private int fileCount;
	private List<JsparrowRuleData> rulesData;

	public long getDurationOfCalculation() {
		return durationOfCalculation;
	}

	public void setDurationOfCalculation(long durationOfCalculation) {
		this.durationOfCalculation = durationOfCalculation;
	}

	public long getTotalTimeSaved() {
		return totalTimeSaved;
	}

	public void setTotalTimeSaved(long totalTimeSaved) {
		this.totalTimeSaved = totalTimeSaved;
	}

	public int getTotalIssuesFixed() {
		return totalIssuesFixed;
	}

	public void setTotalIssuesFixed(int totalIssuesFixed) {
		this.totalIssuesFixed = totalIssuesFixed;
	}

	public int getFilesChanged() {
		return filesChanged;
	}

	public void setFilesChanged(int filesChanged) {
		this.filesChanged = filesChanged;
	}

	public int getFileCount() {
		return fileCount;
	}

	public void setFileCount(int fileCount) {
		this.fileCount = fileCount;
	}

	public List<JsparrowRuleData> getRulesData() {
		return rulesData;
	}

	public void setRulesData(List<JsparrowRuleData> rulesData) {
		this.rulesData = rulesData;
	}

}
