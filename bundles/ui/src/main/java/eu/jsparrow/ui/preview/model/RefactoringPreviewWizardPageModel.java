package eu.jsparrow.ui.preview.model;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.ltk.core.refactoring.DocumentChange;

import eu.jsparrow.core.statistic.StopWatchUtil;
import eu.jsparrow.rules.common.RefactoringRule;
import eu.jsparrow.rules.common.statistics.EliminatedTechnicalDebt;
import eu.jsparrow.rules.common.statistics.RuleApplicationCount;
import eu.jsparrow.ui.util.LicenseUtil;
import eu.jsparrow.ui.util.PayPerUseCreditCalculator;

public class RefactoringPreviewWizardPageModel extends BaseModel {

	private Integer issuesFixed;
	private Duration timeSaved;
	private Integer requiredCredit;
	
	private Long runDuration;
	private Integer totalIssuesFixed;
	private Duration totalTimeSaved;
	private Integer totalRequiredCredit;
	private Integer availableCredit;

	private IObservableList<ChangedFilesModel> changedFiles = new WritableList<>();

	public RefactoringPreviewWizardPageModel(RefactoringRule rule, Map<ICompilationUnit, DocumentChange> changes,
			List<RefactoringRule> allRules) {
		setIssuesFixed(RuleApplicationCount.getFor(rule)
			.toInt());
		setTimeSaved(EliminatedTechnicalDebt.get(rule));
		PayPerUseCreditCalculator payPerUsecalculator = new PayPerUseCreditCalculator();
		int measuredCredit = payPerUsecalculator.measureWeight(rule);
		setRequiredCredit(measuredCredit);
		changedFiles.addAll(changes.entrySet()
			.stream()
			.map(x -> new ChangedFilesModel(x.getKey(), x.getValue()))
			.collect(Collectors.toList()));

		setRunDuration(StopWatchUtil.getTime());
		
		int issuesFixedCount = allRules.stream()
				.map(RuleApplicationCount::getFor)
				.mapToInt(RuleApplicationCount::toInt)
				.sum();
		setTotalIssuesFixed(issuesFixedCount);
		
		Duration timeSaved = allRules.stream()
			.map(EliminatedTechnicalDebt::get)
			.reduce(Duration.ZERO, Duration::plus);
		setTotalTimeSaved(timeSaved);
		

		PayPerUseCreditCalculator calculator = new PayPerUseCreditCalculator();
		int required = calculator.findTotalRequiredCredit(allRules);
		setTotalRequiredCredit(required);
		
		Integer available = LicenseUtil.get().getValidationResult().getCredit().get(); //FIXME
		setAvailableCredit(available);

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

	public Integer getTotalIssuesFixed() {
		return totalIssuesFixed;
	}

	public Long getRunDuration() {
		return runDuration;
	}
	
	public Duration getTotalTimeSaved() {
		return totalTimeSaved;
	}

	public Integer getRequiredCredit() {
		return requiredCredit;
	}

	public Integer getTotalRequiredCredit() {
		return totalRequiredCredit;
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

	public void setRunDuration(Long runDuration) {
		firePropertyChange("runDuration", this.runDuration, this.runDuration = runDuration); //$NON-NLS-1$
	}

	public void setTotalIssuesFixed(Integer totalIssuesFixed) {
		firePropertyChange("totalIssuesFixed", this.totalIssuesFixed, this.totalIssuesFixed = totalIssuesFixed); //$NON-NLS-1$
	}
	
	public void setTotalTimeSaved(Duration totalTimeSaved) {
		firePropertyChange("totalTimeSaved", this.totalTimeSaved, this.totalTimeSaved = totalTimeSaved); //$NON-NLS-1$
	}
	
	public void setTotalRequiredCredit(Integer totalRequiredCredit) {
		firePropertyChange("totalRequiredCredit", this.totalRequiredCredit, this.totalRequiredCredit = totalRequiredCredit); //$NON-NLS-1$
	}
	
	public void setAvailableCredit(Integer availableCredit) {
		firePropertyChange("availableCredit", this.availableCredit, this.availableCredit = availableCredit); //$NON-NLS-1$
	}
}
