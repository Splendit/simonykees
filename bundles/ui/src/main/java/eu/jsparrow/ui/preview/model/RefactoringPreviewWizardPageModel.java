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
import eu.jsparrow.ui.util.LicenseUtil;
import eu.jsparrow.ui.util.PayPerUseCreditCalculator;

public class RefactoringPreviewWizardPageModel extends BaseModel {

	private Integer issuesFixed;

	private Duration timeSaved;
	
	private Integer availableCredit;
	private Integer requiredCredit;

	private IObservableList<ChangedFilesModel> changedFiles = new WritableList<>();

	public RefactoringPreviewWizardPageModel(RefactoringRule rule, Map<ICompilationUnit, DocumentChange> changes) {
		setIssuesFixed(RuleApplicationCount.getFor(rule)
			.toInt());
		setTimeSaved(EliminatedTechnicalDebt.get(rule));
		PayPerUseCreditCalculator payPerUsecalculator = new PayPerUseCreditCalculator();
		int measuredCredit = payPerUsecalculator.measureWeight(rule);
		Integer credit = LicenseUtil.get().getValidationResult().getCredit().get();
		setRequiredCredit(measuredCredit);
		setAvailableCredit(credit);
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

	public Integer getRequiredCredit() {
		return requiredCredit;
	}

	public Integer getAvailableCredit() {
		return availableCredit;
	}

	public void setTimeSaved(Duration timeSaved) {
		firePropertyChange("timeSaved", this.timeSaved, this.timeSaved = timeSaved); //$NON-NLS-1$
	}

	public void setRequiredCredit(Integer requiredCredit) {
		firePropertyChange("requiredCredit", this.requiredCredit, this.requiredCredit = requiredCredit); //$NON-NLS-1$
	}
	
	public void setAvailableCredit(Integer availableCredit) {
		firePropertyChange("availableCredit", this.availableCredit, this.availableCredit = availableCredit); //$NON-NLS-1$
	}
}
