package eu.jsparrow.core.statistic.entity;

/**
 * Entity class for jSparrow statistics rule data
 * 
 * @since 2.7.0
 *
 */
public class JsparrowRuleData {

	private String ruleId;
	private int issuesFixed;
	private long remediationCost;
	private int filesChanged;

	public JsparrowRuleData(String ruleId, int issuesFixed, long remediationCost, int filesChanged) {
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

	public long getRemediationCost() {
		return remediationCost;
	}

	public void setRemediationCost(long remediationCost) {
		this.remediationCost = remediationCost;
	}

	public int getFilesChanged() {
		return filesChanged;
	}

	public void setFilesChanged(int filesChanged) {
		this.filesChanged = filesChanged;
	}

}
