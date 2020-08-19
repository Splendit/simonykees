package eu.jsparrow.ui.preview.model.summary;

import eu.jsparrow.ui.preview.model.BaseModel;

public class RenamingPerFileModel extends BaseModel {

	private String name;
	private long times;

	public RenamingPerFileModel(String name, long times) {
		this.name = name;
		this.times = times;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public long getTimes() {
		return times;
	}

	public void setTimes(long times) {
		this.times = times;
	}

}