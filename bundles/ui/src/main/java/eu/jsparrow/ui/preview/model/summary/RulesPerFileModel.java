package eu.jsparrow.ui.preview.model.summary;

import eu.jsparrow.ui.preview.model.BaseModel;

public class RulesPerFileModel extends BaseModel {

	private String name;

	public RulesPerFileModel(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}