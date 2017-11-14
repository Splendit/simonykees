package eu.jsparrow.ui.preview.model;

import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.ltk.core.refactoring.DocumentChange;

import eu.jsparrow.core.rule.RefactoringRule;
import eu.jsparrow.core.rule.RefactoringRuleInterface;
import eu.jsparrow.core.rule.statistics.EliminatedTechnicalDebt;
import eu.jsparrow.core.rule.statistics.RuleApplicationCount;
import eu.jsparrow.core.visitor.AbstractASTRewriteASTVisitor;

public class RefactoringPreviewWizardPageModel extends BaseModel {

	private WritableValue<String> issuesFixed = new WritableValue<>();

	private WritableValue<String> hoursSaved = new WritableValue<>();

	private IObservableList<ChangedFilesModel> changedFiles = new WritableList<>();

	public IObservableList<ChangedFilesModel> getChangedFiles() {
		return changedFiles;
	}

	private RefactoringRuleInterface rule;

	public RefactoringPreviewWizardPageModel(RefactoringRule<? extends AbstractASTRewriteASTVisitor> rule,
			Map<ICompilationUnit, DocumentChange> changes) {
		this.rule = rule;
		setIssuesFixed(String.format("Issues Fixed: %s", RuleApplicationCount.getFor(rule)
			.toInt()));
		setHoursSaved(String.format("Minutes Saved: %s", EliminatedTechnicalDebt.get(rule)
			.toMinutes()));
		changedFiles.addAll(changes.entrySet()
			.stream()
			.map(x -> new ChangedFilesModel(x.getKey(), x.getValue()))
			.collect(Collectors.toList()));

	}

	public String getIssuesFixed() {
		return issuesFixed.getValue();
	}

	public void setIssuesFixed(String issuesFixed) {
		this.issuesFixed.setValue(issuesFixed);
	}

	public String getHoursSaved() {
		return hoursSaved.getValue();
	}

	public void setHoursSaved(String hoursSaved) {
		this.hoursSaved.setValue(hoursSaved);
	}

}
