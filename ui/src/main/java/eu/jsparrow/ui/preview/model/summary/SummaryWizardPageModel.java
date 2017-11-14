package eu.jsparrow.ui.preview.model.summary;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipse.jdt.core.ICompilationUnit;

import eu.jsparrow.core.refactorer.RefactoringPipeline;
import eu.jsparrow.core.refactorer.RefactoringState;
import eu.jsparrow.core.rule.statistics.EliminatedTechnicalDebt;
import eu.jsparrow.core.rule.statistics.RuleApplicationCount;
import eu.jsparrow.i18n.Messages;
import eu.jsparrow.ui.preview.model.BaseModel;
import eu.jsparrow.ui.preview.model.DurationFormatUtil;

public class SummaryWizardPageModel extends BaseModel {

	private final RefactoringPipeline refactoringPipeline;

	private String runDuration;

	private Boolean isFreeLicense;

	private Map<RefactoringState, String> initialSource = new HashMap<>();

	private Map<RefactoringState, String> finalSource = new HashMap<>();

	private IObservableList<RuleTimesModel> ruleTimes = new WritableList<>();

	private IObservableList<ChangedFilesModel> changedFiles = new WritableList<>();

	public SummaryWizardPageModel(RefactoringPipeline refactoringPipeline) {
		this.refactoringPipeline = refactoringPipeline;
		initialize();
		isFreeLicense = false;
	}

	private void initialize() {
		initialSource.putAll(refactoringPipeline.getInitialSourceMap());
		refactoringPipeline.setSourceMap(finalSource);
		addModifiedFiles();
		addRuleTimes();
		setRunDuration(""); //$NON-NLS-1$
	}

	public String getRunDuration() {
		return runDuration;
	}

	public void setRunDuration(String runDuration) {
		firePropertyChange("runDuration", this.runDuration, this.runDuration = runDuration); //$NON-NLS-1$
	}

	public IObservableList<ChangedFilesModel> getChangedFiles() {
		return changedFiles;
	}

	public void updateFiles() {
		changedFiles.clear();
		addModifiedFiles();
	}

	public IObservableList<RuleTimesModel> getRuleTimes() {
		return ruleTimes;
	}

	public String getIssuesFixed() {
		int totalIssuesFixed = refactoringPipeline.getRules()
			.stream()
			.mapToInt(x -> RuleApplicationCount.getFor(x)
				.toInt())
			.sum();
		return String.format(Messages.SummaryWizardPageModel_IssuesFixed, totalIssuesFixed);
	}

	public String getHoursSaved() {
		Duration totalTimeSaved = EliminatedTechnicalDebt.getTotalFor(refactoringPipeline.getRules());
		return String.format(Messages.DurationFormatUtil_TimeSaved, DurationFormatUtil.formatTimeSaved(totalTimeSaved));
	}

	public void setIsFreeLicense(Boolean validLicense) {
		firePropertyChange("isFreeLicense", this.isFreeLicense, isFreeLicense = validLicense); //$NON-NLS-1$
	}

	public Boolean getIsFreeLicense() {
		return isFreeLicense;
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
				int times = RuleApplicationCount.getFor(rule)
					.toInt();
				Duration timeSaved = EliminatedTechnicalDebt.get(rule);
				String timeSavedString = DurationFormatUtil.formatTimeSaved(timeSaved);
				RuleTimesModel ruleTimesModel = new RuleTimesModel(name, times, timeSavedString);
				ruleTimes.add(ruleTimesModel);
			});
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

}
