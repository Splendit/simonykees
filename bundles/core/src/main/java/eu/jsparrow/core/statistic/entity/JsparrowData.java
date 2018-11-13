package eu.jsparrow.core.statistic.entity;

import java.util.List;

public class JsparrowData {

	private String projectName;
	private long timestampGitHubStart;
	private long timestampJSparrowEnd;
	private long totalTimeSaved;
	private int totalIssuesFixed;
	private int filesChanged;
	private int fileCount;
	private List<JsparrowRuleData> rulesData;

	public String getProjectName() {
		return projectName;
	}
	
	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}
	
	public long getTimestampGitHubStart() {
		return timestampGitHubStart;
	}

	public void setTimestampGitHubStart(long timestampGitHubStart) {
		this.timestampGitHubStart = timestampGitHubStart;
	}

	public long getTimestampJSparrowEnd() {
		return timestampJSparrowEnd;
	}

	public void setTimestampJSparrowEnd(long timestampJSparrowEnd) {
		this.timestampJSparrowEnd = timestampJSparrowEnd;
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
