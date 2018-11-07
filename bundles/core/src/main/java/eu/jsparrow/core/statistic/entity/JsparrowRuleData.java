package eu.jsparrow.core.statistic.entity;

import java.time.Duration;

public class JsparrowRuleData {

	private String ruleId;
	private int issuesFixed;
	private Duration remediationCost;
	private int filesChanged;

	public JsparrowRuleData(String ruleId, int issuesFixed, Duration remediationCost, int filesChanged) {
		super();
		this.ruleId = ruleId;
		this.issuesFixed = issuesFixed;
		this.remediationCost = remediationCost;
		this.filesChanged = filesChanged;
	}

	public String getRuleId() {
		return ruleId;
	}

	public void setRuleId(String ruleId) {
		this.ruleId = ruleId;
	}

	public int getIssuesFixed() {
		return issuesFixed;
	}

	public void setIssuesFixed(int issuesFixed) {
		this.issuesFixed = issuesFixed;
	}

	public Duration getRemediationCost() {
		return remediationCost;
	}

	public void setRemediationCost(Duration remediationCost) {
		this.remediationCost = remediationCost;
	}

	public int getFilesChanged() {
		return filesChanged;
	}

	public void setFilesChanged(int filesChanged) {
		this.filesChanged = filesChanged;
	}

}
