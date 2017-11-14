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

}