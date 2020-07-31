package eu.jsparrow.ui.preview.model.summary;

import java.util.List;

import eu.jsparrow.ui.preview.model.BaseModel;

public class ChangedNamesInFileModel extends BaseModel {
	
	private String fileName;
	private List<RenamingPerFileModel> renamings;
	
	public ChangedNamesInFileModel(String fileName, List<RenamingPerFileModel> renamings) {
		this.fileName = fileName;
		this.renamings = renamings;
	}
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	public List<RenamingPerFileModel> getRenamings() {
		return renamings;
	}
	public void setRenamings(List<RenamingPerFileModel> renamings) {
		this.renamings = renamings;
	}
	
	

}
