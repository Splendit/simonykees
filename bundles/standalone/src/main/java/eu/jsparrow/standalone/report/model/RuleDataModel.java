package eu.jsparrow.standalone.report.model;

/**
 * 
 * @since 3.23.0
 *
 */
public class RuleDataModel {

	private String ruleId;
	private String ruleName;
	private String ruleLink;
	private int issuesFixed;
	private int filesChanged;
	private long remediationCost;

	public RuleDataModel(String ruleId, String ruleName, String ruleLink, int issuesFixed, int filesChanged,
			long remediationCost) {
		this.ruleId = ruleId;
		this.ruleName = ruleName;
		this.ruleLink = ruleLink;
		this.issuesFixed = issuesFixed;
		this.filesChanged = filesChanged;
		this.remediationCost = remediationCost;
	}

	public String getRuleId() {
		return ruleId;
	}

	public void setRuleId(String ruleId) {
		this.ruleId = ruleId;
	}

	public String getRuleName() {
		return ruleName;
	}

	public void setRuleName(String ruleName) {
		this.ruleName = ruleName;
	}

	public String getRuleLink() {
		return ruleLink;
	}

	public void setRuleLink(String ruleLink) {
		this.ruleLink = ruleLink;
	}

	public int getIssuesFixed() {
		return issuesFixed;
	}

	public void setIssuesFixed(int issuesFixed) {
		this.issuesFixed = issuesFixed;
	}

	public int getFilesChanged() {
		return filesChanged;
	}

	public void setFilesChanged(int filesChanged) {
		this.filesChanged = filesChanged;
	}

	public long getRemediationCost() {
		return remediationCost;
	}

	public void setRemediationCost(long remediationCost) {
		this.remediationCost = remediationCost;
	}

}
