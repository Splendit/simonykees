package eu.jsparrow.core.statistic.entity;

import java.time.Instant;

public class JsparrowMetric {
	
	private String uUID;
	private Instant timestamp;
	private String projectName;
	private JsparrowData data;
	public String getuUID() {
		return uUID;
	}
	public void setuUID(String uUID) {
		this.uUID = uUID;
	}
	public Instant getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(Instant timestamp) {
		this.timestamp = timestamp;
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
