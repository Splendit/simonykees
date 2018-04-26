package eu.jsparrow.ui.preview.model;

import java.time.Duration;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.ltk.core.refactoring.DocumentChange;

import eu.jsparrow.rules.common.RefactoringRuleImpl;
import eu.jsparrow.rules.common.RefactoringRule;
import eu.jsparrow.rules.common.statistics.EliminatedTechnicalDebt;
import eu.jsparrow.rules.common.statistics.RuleApplicationCount;
import eu.jsparrow.rules.common.visitor.AbstractASTRewriteASTVisitor;

public class RefactoringPreviewWizardPageModel extends BaseModel {

	private int issuesFixed;

	private Duration timeSaved;

	private IObservableList<ChangedFilesModel> changedFiles = new WritableList<>();

	public IObservableList<ChangedFilesModel> getChangedFiles() {
		return changedFiles;
	}

	public RefactoringPreviewWizardPageModel(RefactoringRule rule,
			Map<ICompilationUnit, DocumentChange> changes) {
		setIssuesFixed(RuleApplicationCount.getFor(rule).toInt());
		setTimeSaved(EliminatedTechnicalDebt.get(rule));
		changedFiles.addAll(changes.entrySet()
			.stream()
			.map(x -> new ChangedFilesModel(x.getKey(), x.getValue()))
			.collect(Collectors.toList()));

	}

	public int getIssuesFixed() {
		return issuesFixed;
	}

	public void setIssuesFixed(int issuesFixed) {
		firePropertyChange("issuesFixed", this.issuesFixed, this.issuesFixed = issuesFixed); //$NON-NLS-1$
	}

	public Duration getTimeSaved() {
		return timeSaved;
	}

	public void setTimeSaved(Duration timeSaved) {
		firePropertyChange("timeSaved", this.timeSaved, this.timeSaved = timeSaved); //$NON-NLS-1$
	}

}
