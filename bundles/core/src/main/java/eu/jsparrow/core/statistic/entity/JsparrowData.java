package eu.jsparrow.core.statistic.entity;

import java.util.List;

public class JsparrowData {

	private String projectName;
	private long timestampGitHubStart;
	private long timestampJSparrowFinish;
	private long totalTimeSaved;
	private int totalIssuesFixed;
	private int totalFilesChanged;
	private int totalFilesCount;
	private List<JsparrowRuleData> rules;

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

	public long getTimestampJSparrowFinish() {
		return timestampJSparrowFinish;
	}

	public void setTimestampJSparrowFinish(long timestampJSparrowFinish) {
		this.timestampJSparrowFinish = timestampJSparrowFinish;
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

	public int getTotalFilesChanged() {
		return totalFilesChanged;
	}

	public void setTotalFilesChanged(int totalFilesChanged) {
		this.totalFilesChanged = totalFilesChanged;
	}

	public int getTotalFilesCount() {
		return totalFilesCount;
	}

	public void setTotalFilesCount(int totalFilesCount) {
		this.totalFilesCount = totalFilesCount;
	}

	public List<JsparrowRuleData> getRules() {
		return rules;
	}

	public void setRules(List<JsparrowRuleData> rules) {
		this.rules = rules;
	}

}
