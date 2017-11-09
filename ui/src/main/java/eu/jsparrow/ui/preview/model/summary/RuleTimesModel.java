package eu.jsparrow.ui.preview.model.summary;

import eu.jsparrow.ui.preview.model.BaseModel;

public class RuleTimesModel extends BaseModel {

	private String name;

	private Integer times;

	public String getName() {
		return name;
	}

	public Integer getTimes() {
		return times;
	}

	public RuleTimesModel(String name, Integer times) {
		this.name = name;
		this.times = times;
	}
}
