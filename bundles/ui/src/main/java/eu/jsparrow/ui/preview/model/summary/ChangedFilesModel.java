package eu.jsparrow.ui.preview.model.summary;

import java.util.List;

import eu.jsparrow.ui.preview.model.BaseModel;

public class ChangedFilesModel extends BaseModel {

	private String name;
	/*
	 * FIXME We do not need sourceLeft and sourceRight unless we show a summary diff
	 * view in the summary page. The summary diff view was removed in SIM-1735.
	 */
	private String sourceLeft;
	private String sourceRight;
	private List<String> rules;

	public ChangedFilesModel(String name) {
		this.name = name;
	}
	
	public ChangedFilesModel(String name, String sourceLeft, String sourceRight, List<String> rules) {
		this(name);
		this.sourceLeft = sourceLeft;
		this.sourceRight = sourceRight;
		this.rules = rules;
	}
	
	public ChangedFilesModel(String name, List<String> rules) {
		this(name);
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
