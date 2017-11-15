package eu.jsparrow.ui.preview.model;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.ltk.core.refactoring.DocumentChange;

public class ChangedFilesModel {

	private boolean changeSelected;

	private ICompilationUnit compilationUnit;

	private DocumentChange change;


	public ChangedFilesModel(ICompilationUnit compilationUnit, DocumentChange documentChange) {
		this.changeSelected = true;
		this.compilationUnit = compilationUnit;
		this.change = documentChange;
	}
	
	public boolean isChangeSelected() {
		return changeSelected;
	}

	public void setChangeSelected(boolean changeSelected) {
		this.changeSelected = changeSelected;
	}
	
	public ICompilationUnit getCompilationUnit() {
		return compilationUnit;
	}

	public void setCompilationUnit(ICompilationUnit compilationUnit) {
		this.compilationUnit = compilationUnit;
	}
	
	public DocumentChange getChange() {
		return change;
	}

	public void setChange(DocumentChange change) {
		this.change = change;
	}

}