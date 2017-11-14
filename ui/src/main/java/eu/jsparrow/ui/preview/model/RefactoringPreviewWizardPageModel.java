package eu.jsparrow.ui.preview.model;

import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.ltk.core.refactoring.DocumentChange;

import eu.jsparrow.core.rule.RefactoringRule;
import eu.jsparrow.core.rule.RefactoringRuleInterface;
import eu.jsparrow.core.rule.statistics.EliminatedTechnicalDebt;
import eu.jsparrow.core.rule.statistics.RuleApplicationCount;
import eu.jsparrow.core.visitor.AbstractASTRewriteASTVisitor;
import eu.jsparrow.i18n.Messages;

public class RefactoringPreviewWizardPageModel extends BaseModel {

	private String issuesFixed;

	private String timeSaved;

	private IObservableList<ChangedFilesModel> changedFiles = new WritableList<>();

	public IObservableList<ChangedFilesModel> getChangedFiles() {
		return changedFiles;
	}

	private RefactoringRuleInterface rule;

	public RefactoringPreviewWizardPageModel(RefactoringRule<? extends AbstractASTRewriteASTVisitor> rule,
			Map<ICompilationUnit, DocumentChange> changes) {
		this.rule = rule;
		setIssuesFixed(String.format(Messages.SummaryWizardPageModel_IssuesFixed, RuleApplicationCount.getFor(rule)
			.toInt()));
		setTimeSaved(String.format(Messages.DurationFormatUtil_TimeSaved,
				DurationFormatUtil.formatTimeSaved(EliminatedTechnicalDebt.get(rule))));
		changedFiles.addAll(changes.entrySet()
			.stream()
			.map(x -> new ChangedFilesModel(x.getKey(), x.getValue()))
			.collect(Collectors.toList()));

	}

	public String getIssuesFixed() {
		return issuesFixed;
	}

	public void setIssuesFixed(String issuesFixed) {
		firePropertyChange("issuesFixed", this.issuesFixed, this.issuesFixed = issuesFixed);
	}

	public String getTimeSaved() {
		return timeSaved;
	}

	public void setTimeSaved(String timeSaved) {
		firePropertyChange("timeSaved", this.timeSaved, this.timeSaved = timeSaved);
	}

}
