package eu.jsparrow.core.statistic.entity;


public class JsparrowMetric {
	
	private String uUID;
	private long metricTimestamp;
	private String projectName;
	private JsparrowData data;
	public String getuUID() {
		return uUID;
	}
	public void setuUID(String uUID) {
		this.uUID = uUID;
	}
	public long getTimestamp() {
		return metricTimestamp;
	}
	public void setTimestamp(long metricTimestamp) {
		this.metricTimestamp = metricTimestamp;
	}
	public String getProjectName() {
		return projectName;
	}
	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}
	public JsparrowData getData() {
		return data;
	}
	public void setData(JsparrowData data) {
		this.data = data;
	}
}
