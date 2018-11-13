package eu.jsparrow.core.statistic.entity;

public class JsparrowMetric {

	private String uUID;
	private long timestamp;
	private String repoOwner;
	private String repoName;
	private JsparrowData data;

	public String getuUID() {
		return uUID;
	}

	public void setuUID(String uUID) {
		this.uUID = uUID;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public String getRepoOwner() {
		return repoOwner;
	}

	public void setRepoOwner(String repoOwner) {
		this.repoOwner = repoOwner;
	}

	public String getRepoName() {
		return repoName;
	}

	public void setRepoName(String repoName) {
		this.repoName = repoName;
	}

	public JsparrowData getData() {
		return data;
	}

	public void setData(JsparrowData data) {
		this.data = data;
	}
}
