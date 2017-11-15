package eu.jsparrow.ui.preview.model.summary;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipse.jdt.core.ICompilationUnit;

import eu.jsparrow.core.refactorer.RefactoringPipeline;
import eu.jsparrow.core.refactorer.RefactoringState;
import eu.jsparrow.core.rule.RefactoringRule;
import eu.jsparrow.core.rule.RefactoringRuleInterface;
import eu.jsparrow.core.rule.statistics.EliminatedTechnicalDebt;
import eu.jsparrow.core.rule.statistics.RuleApplicationCount;
import eu.jsparrow.core.visitor.AbstractASTRewriteASTVisitor;
import eu.jsparrow.i18n.Messages;
import eu.jsparrow.ui.preview.model.BaseModel;
import eu.jsparrow.ui.preview.model.DurationFormatUtil;
import eu.jsparrow.ui.preview.model.RefactoringPreviewWizardModel;

public class RefactoringSummaryWizardPageModel extends BaseModel {

	private final RefactoringPipeline refactoringPipeline;

	private Long runDuration;

	private Boolean isFreeLicense;

	private String issuesFixed;

	private String timeSaved;

	private Map<RefactoringState, String> initialSource = new HashMap<>();

	private Map<RefactoringState, String> finalSource = new HashMap<>();

	private IObservableList<RuleTimesModel> ruleTimes = new WritableList<>();

	private IObservableList<ChangedFilesModel> changedFiles = new WritableList<>();

	private RefactoringPreviewWizardModel wizardModel;

	public RefactoringSummaryWizardPageModel(RefactoringPipeline refactoringPipeline,
			RefactoringPreviewWizardModel wizardModel) {
		this.refactoringPipeline = refactoringPipeline;
		this.wizardModel = wizardModel;
		initialize();
		isFreeLicense = false;
	}

	public Long getRunDuration() {
		return runDuration;
	}

	public void setRunDuration(Long runDuration) {
		firePropertyChange("runDuration", this.runDuration, this.runDuration = runDuration); //$NON-NLS-1$
	}

	public IObservableList<ChangedFilesModel> getChangedFiles() {
		return changedFiles;
	}


	public IObservableList<RuleTimesModel> getRuleTimes() {
		return ruleTimes;
	}

	public String getIssuesFixed() {
		return this.issuesFixed;
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

	public Boolean getIsFreeLicense() {
		return isFreeLicense;
	}

	public void setIsFreeLicense(Boolean validLicense) {
		firePropertyChange("isFreeLicense", this.isFreeLicense, isFreeLicense = validLicense); //$NON-NLS-1$
	}

	// Needed because we don't have full databinding/models yet, so we need to
	// update the data manually :(
	public void updateData() {
		updateChangedFiles();
		updateIssuesFixed();
		updateTimeSaved();
		updateRuleTimes();
	}

	private void initialize() {
		initialSource.putAll(refactoringPipeline.getInitialSourceMap());
		refactoringPipeline.setSourceMap(finalSource);
		addModifiedFiles();
		addRuleTimes();
		setRunDuration(0L); //$NON-NLS-1$
		setIssuesFixed("");
		setTimeSaved("");
	}

	private void addModifiedFiles() {
		refactoringPipeline.getInitialSourceMap()
			.entrySet()
			.stream()
			.filter(this::hasChanges)
			.forEach(x -> {
				RefactoringState state = x.getKey();
				changedFiles.add(createModelFromRefactoringState(state));
			});
	}

	private void addRuleTimes() {
		refactoringPipeline.getRules()
			.stream()
			.filter(rule -> RuleApplicationCount.getFor(rule)
				.toInt() > 0)
			.forEach(rule -> {
				String name = rule.getRuleDescription()
					.getName();
				int times = getApplicationTimesForRule(rule);
				String timeSavedString = DurationFormatUtil.formatTimeSaved(EliminatedTechnicalDebt.get(rule));
				RuleTimesModel ruleTimesModel = new RuleTimesModel(name, times, timeSavedString);
				ruleTimes.add(ruleTimesModel);
			});
	}

	private int getApplicationTimesForRule(RefactoringRuleInterface rule) {
		return RuleApplicationCount.getFor(rule)
			.getApplicationsForFiles(wizardModel.getFilesForRule(rule));
	}

	private String getPathString(ICompilationUnit compilationUnit) {
		String temp = compilationUnit.getParent()
			.getPath()
			.toString();
		return StringUtils.startsWith(temp, "/") ? StringUtils.substring(temp, 1) : temp; //$NON-NLS-1$
	}

	private boolean hasChanges(Entry<RefactoringState, String> entry) {
		RefactoringState state = entry.getKey();
		// Filter out those refactoring states that were deselected or
		// have no changes present.
		if (!state.hasChange()) {
			return false;
		}
		Boolean allRulesIgnored = refactoringPipeline.getRules()
			.stream()
			.allMatch(rule -> state.getIgnoredRules()
				.contains(rule));
		if (allRulesIgnored) {
			return false;
		}
		Boolean noChangePresent = refactoringPipeline.getRules()
			.stream()
			.allMatch(rule -> null == state.getChangeIfPresent(rule));
		return !noChangePresent;
	}

	private ChangedFilesModel createModelFromRefactoringState(RefactoringState state) {
		ICompilationUnit compUnit = state.getWorkingCopy();
		String fileName = String.format("%s - %s", compUnit.getElementName(), getPathString(compUnit)); //$NON-NLS-1$
		String left = initialSource.get(state) == null ? "" : initialSource.get(state); //$NON-NLS-1$
		String right = finalSource.get(state) == null ? "" : finalSource.get(state); //$NON-NLS-1$
		return new ChangedFilesModel(fileName, left, right);
	}

	private void updateIssuesFixed() {
		String result;
		int totalIssuesFixed = refactoringPipeline.getRules()
			.stream()
			.mapToInt(x -> RuleApplicationCount.getFor(x)
				.toInt())
			.sum();
		result = String.format(Messages.SummaryWizardPageModel_IssuesFixed, totalIssuesFixed);
		setIssuesFixed(result);
	}

	private void updateTimeSaved() {
		Duration totalTimeSaved = EliminatedTechnicalDebt.getTotalFor(refactoringPipeline.getRules());
		setTimeSaved(String.format(Messages.DurationFormatUtil_TimeSaved,
				DurationFormatUtil.formatTimeSaved(totalTimeSaved)));
	}

	private void updateRuleTimes() {
		ruleTimes.clear();
		addRuleTimes();
	}
	
	private void updateChangedFiles() {
		changedFiles.clear();
		addModifiedFiles();
	}

}
