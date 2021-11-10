package eu.jsparrow.ui.preview.model;

import java.time.Duration;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.ltk.core.refactoring.DocumentChange;

import eu.jsparrow.rules.common.RefactoringRule;
import eu.jsparrow.rules.common.statistics.EliminatedTechnicalDebt;
import eu.jsparrow.rules.common.statistics.RuleApplicationCount;

public class RefactoringPreviewWizardPageModel extends BaseModel {

	private Integer issuesFixed;

	private Duration timeSaved;
	
	private Duration availableCredit;
	private Duration requiredCredit;

	private IObservableList<ChangedFilesModel> changedFiles = new WritableList<>();

	public RefactoringPreviewWizardPageModel(RefactoringRule rule, Map<ICompilationUnit, DocumentChange> changes) {
		setIssuesFixed(RuleApplicationCount.getFor(rule)
			.toInt());
		setTimeSaved(EliminatedTechnicalDebt.get(rule));
		setRequiredCredit(EliminatedTechnicalDebt.get(rule));
		setAvailableCredit(EliminatedTechnicalDebt.get(rule));
		changedFiles.addAll(changes.entrySet()
			.stream()
			.map(x -> new ChangedFilesModel(x.getKey(), x.getValue()))
			.collect(Collectors.toList()));

	}

	public IObservableList<ChangedFilesModel> getChangedFiles() {
		return changedFiles;
	}

	public Integer getIssuesFixed() {
		return issuesFixed;
	}

	public void setIssuesFixed(Integer issuesFixed) {
		firePropertyChange("issuesFixed", this.issuesFixed, this.issuesFixed = issuesFixed); //$NON-NLS-1$
	}

	public Duration getTimeSaved() {
		return timeSaved;
	}

	public Duration getRequiredCredit() {
		return requiredCredit;
	}

	public Duration getAvailableCredit() {
		return availableCredit;
	}

	public void setTimeSaved(Duration timeSaved) {
		firePropertyChange("timeSaved", this.timeSaved, this.timeSaved = timeSaved); //$NON-NLS-1$
	}

	public void setRequiredCredit(Duration requiredCredit) {
		firePropertyChange("requiredCredit", this.requiredCredit, this.requiredCredit = requiredCredit); //$NON-NLS-1$
	}
	
	public void setAvailableCredit(Duration availableCredit) {
		firePropertyChange("availableCredit", this.availableCredit, this.availableCredit = availableCredit); //$NON-NLS-1$
	}
}
