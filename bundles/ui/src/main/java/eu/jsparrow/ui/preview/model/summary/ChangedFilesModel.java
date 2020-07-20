package eu.jsparrow.ui.preview.model.summary;

import java.util.ArrayList;
import java.util.List;

import eu.jsparrow.ui.preview.model.BaseModel;

public class ChangedFilesModel extends BaseModel {

	private String name;
	private String sourceLeft;
	private String sourceRight;
	private List<String> rules;

	public ChangedFilesModel(String name) {
		this.name = name;
	}

	public ChangedFilesModel(String name, String sourceLeft, String sourceRight) {
		this(name);
		this.sourceLeft = sourceLeft;
		this.sourceRight = sourceRight;
		this.rules = new ArrayList<>();
	}
	
	public ChangedFilesModel(String name, String sourceLeft, String sourceRight, List<String> rules) {
		this(name);
		this.sourceLeft = sourceLeft;
		this.sourceRight = sourceRight;
		this.rules = rules;
	}


	public String getName() {
		return name;
	}

	public String getSourceLeft() {
		return sourceLeft;
	}

	public String getSourceRight() {
		return sourceRight;
	}

	public void setSourceLeft(String sourceLeft) {
		this.sourceLeft = sourceLeft;
	}

	public void setSourceRight(String sourceRight) {
		firePropertyChange("sourceRight", this.sourceRight, sourceRight); //$NON-NLS-1$
		this.sourceRight = sourceRight;
	}
	
	public List<String> getRules() {
		return rules;
	}

}
