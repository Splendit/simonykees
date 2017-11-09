package eu.jsparrow.ui.preview.model;

import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.ltk.core.refactoring.DocumentChange;

import eu.jsparrow.core.rule.EliminatedTechnicalDebt;
import eu.jsparrow.core.rule.RefactoringRule;
import eu.jsparrow.core.rule.RefactoringRuleInterface;
import eu.jsparrow.core.rule.RuleApplicationCount;
import eu.jsparrow.core.visitor.AbstractASTRewriteASTVisitor;
import eu.jsparrow.ui.preview.model.summary.RuleTimesModel;

public class RefactoringPreviewWizardModel extends BaseModel {

	private WritableValue<String> issuesFixed = new WritableValue<>();

	private WritableValue<String> hoursSaved = new WritableValue<>();

	private IObservableList<RuleTimesModel> changedFiles;

	private RefactoringRuleInterface rule;

	public RefactoringPreviewWizardModel(RefactoringRule<? extends AbstractASTRewriteASTVisitor> rule) {
		this.rule = rule;
		setIssuesFixed(String.format("Issues Fixed: %s", RuleApplicationCount.getFor(rule)
			.toInt()));
		setHoursSaved(String.format("Minutes Saved: %s", EliminatedTechnicalDebt.get(rule)
			.toMinutes()));
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

	public class ChangedFilesModel {

		private boolean changeSelected;

		private ICompilationUnit compilationUnit;

		private DocumentChange change;

		public ChangedFilesModel() {

		}

	}

}
