package eu.jsparrow.ui.preview.model.summary;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.ltk.core.refactoring.DocumentChange;

import eu.jsparrow.core.refactorer.RefactoringPipeline;
import eu.jsparrow.core.refactorer.RefactoringState;
import eu.jsparrow.core.statistic.StopWatchUtil;
import eu.jsparrow.rules.common.RefactoringRule;
import eu.jsparrow.rules.common.statistics.EliminatedTechnicalDebt;
import eu.jsparrow.rules.common.statistics.RuleApplicationCount;
import eu.jsparrow.ui.preview.model.BaseModel;
import eu.jsparrow.ui.preview.model.DurationFormatUtil;
import eu.jsparrow.ui.preview.model.RefactoringPreviewWizardModel;


public abstract class AbstractSummaryWizardPageModel extends BaseModel {

	private final RefactoringPipeline refactoringPipeline;

	private Long runDuration;

	private Integer issuesFixed;

	private Duration timeSaved;

	private IObservableList<RuleTimesModel> ruleTimes = new WritableList<>();
	protected IObservableList<ChangedFilesModel> changedFiles = new WritableList<>();

	private RefactoringPreviewWizardModel wizardModel;

	public AbstractSummaryWizardPageModel(RefactoringPipeline refactoringPipeline,
			RefactoringPreviewWizardModel wizardModel) {
		this.refactoringPipeline = refactoringPipeline;
		this.wizardModel = wizardModel;
		initialize();
	}

	public Long getRunDuration() {
		return runDuration;
	}

	public void setRunDuration(Long runDuration) {
		firePropertyChange("runDuration", this.runDuration, this.runDuration = runDuration); //$NON-NLS-1$
	}

	public abstract IObservableList<RulesPerFileModel> getRulesPerFile();

	public IObservableList<ChangedFilesModel> getChangedFiles() {
		return changedFiles;
	}

	public IObservableList<RuleTimesModel> getRuleTimes() {
		return ruleTimes;
	}

	public Integer getIssuesFixed() {
		return this.issuesFixed;
	}

	public void setIssuesFixed(Integer issuesFixed) {
		firePropertyChange("issuesFixed", this.issuesFixed, this.issuesFixed = issuesFixed); //$NON-NLS-1$
	}

	public Duration getTimeSaved() {
		return timeSaved;
	}

	public void setTimeSaved(Duration timeSaved) {
		firePropertyChange("timeSaved", this.timeSaved, this.timeSaved = timeSaved); //$NON-NLS-1$
	}

	// Needed because we don't have full databinding/models yet, so we need to
	// update the data manually :(
	public void updateData() {
		updateChangedFiles();
		// Fields depend on contents of RuleTimes list, so we update that first!
		updateRuleTimes();
		updateTimeSaved();
		updateIssuesFixed();
	}

	protected void initialize() {
		addModifiedFiles();
		addRuleTimes();

		/*
		 * Set initial values to something big so labels have enough size This
		 * is easiert hat resizing/layouting labels dynamically based on their
		 * contents
		 */
		setRunDuration(StopWatchUtil.getTime());
		setIssuesFixed(99999);
		setTimeSaved(Duration.ofSeconds(999999999));
	}

	private void addModifiedFiles() {
		refactoringPipeline.getInitialSourceMap()
			.entrySet()
			.stream()
			.filter(this::hasChanges)
			.map(Map.Entry::getKey)
			.forEach(state -> changedFiles.add(createModelFromRefactoringState(state)));
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
				Duration savedTime = EliminatedTechnicalDebt.get(rule, times);
				String timeSavedString = DurationFormatUtil.formatTimeSaved(savedTime);
				RuleTimesModel ruleTimesModel = new RuleTimesModel(name, times, timeSavedString);
				ruleTimesModel.setTimeSavedDuration(savedTime);
				ruleTimes.add(ruleTimesModel);
			});
	}

	private int getApplicationTimesForRule(RefactoringRule rule) {
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
		boolean allRulesIgnored = refactoringPipeline.getRules()
			.stream()
			.allMatch(rule -> state.getIgnoredRules()
				.contains(rule));
		if (allRulesIgnored) {
			return false;
		}
		boolean noChangePresent = refactoringPipeline.getRules()
			.stream()
			.allMatch(rule -> null == state.getChangeIfPresent(rule));
		return !noChangePresent;
	}

	private ChangedFilesModel createModelFromRefactoringState(RefactoringState state) {
		ICompilationUnit compUnit = state.getWorkingCopy();
		String fileName = String.format("%s - %s", compUnit.getElementName(), getPathString(compUnit)); //$NON-NLS-1$
		List<RefactoringRule> rules = refactoringPipeline.getRules();
		List<String> rulesWithChanges = new ArrayList<>();
		for (RefactoringRule rule : rules) {
			DocumentChange change = state.getChangeIfPresent(rule);
			if (change != null) {
				List<String> ruleNamesToDisplay = computeRuleNames(rule, compUnit);
				rulesWithChanges.addAll(ruleNamesToDisplay);
			}
		}

		return new ChangedFilesModel(fileName, rulesWithChanges);
	}

	private void updateIssuesFixed() {
		int totalIssuesFixed = ruleTimes.stream()
			.mapToInt(RuleTimesModel::getTimes)
			.sum();
		setIssuesFixed(totalIssuesFixed);
	}

	private void updateTimeSaved() {
		Duration totalTimeSaved = ruleTimes.stream()
			.map(RuleTimesModel::getTimeSavedDuration)
			.reduce(Duration.ZERO, Duration::plus);
		setTimeSaved(totalTimeSaved);
	}

	private void updateRuleTimes() {
		ruleTimes.clear();
		addRuleTimes();
	}

	protected void updateChangedFiles() {
		changedFiles.clear();
		addModifiedFiles();
	}

	public RefactoringPipeline getRefactoringPipeline() {
		return refactoringPipeline;
	}

	/**
	 * 
	 * @return an array of all the rules and the file names in the summary page.
	 */
	public String[] getProposalProviderContents() {
		return Stream.concat(
				getRuleTimes()
					.stream()
					.map(RuleTimesModel::getName),
				getChangedFiles()
					.stream()
					.map(ChangedFilesModel::getName))
			.toArray(String[]::new);
	}

	public abstract void updateRulesPerFile(List<String> rules);

	protected abstract List<String> computeRuleNames(RefactoringRule rule, ICompilationUnit compUnit);
}
