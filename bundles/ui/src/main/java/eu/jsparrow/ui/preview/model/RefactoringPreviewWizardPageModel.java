package eu.jsparrow.ui.preview.model;

import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.ltk.core.refactoring.DocumentChange;

public class RefactoringPreviewWizardPageModel extends BaseModel {

	private IObservableList<ChangedFilesModel> changedFiles = new WritableList<>();

	public RefactoringPreviewWizardPageModel(Map<ICompilationUnit, DocumentChange> changes) {
		changedFiles.addAll(changes.entrySet()
			.stream()
			.map(x -> new ChangedFilesModel(x.getKey(), x.getValue()))
			.collect(Collectors.toList()));

	}

	public IObservableList<ChangedFilesModel> getChangedFiles() {
		return changedFiles;
	}
}
