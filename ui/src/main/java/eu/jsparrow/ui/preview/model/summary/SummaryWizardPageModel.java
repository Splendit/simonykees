package eu.jsparrow.ui.preview.model.summary;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipse.jdt.core.ICompilationUnit;

import eu.jsparrow.core.refactorer.RefactoringPipeline;
import eu.jsparrow.core.refactorer.RefactoringState;
import eu.jsparrow.core.rule.statistics.EliminatedTechnicalDebt;
import eu.jsparrow.core.rule.statistics.RuleApplicationCount;
import eu.jsparrow.ui.preview.model.BaseModel;

public class SummaryWizardPageModel extends BaseModel {

	private final RefactoringPipeline refactoringPipeline;

	private String runDuration;

	public String getRunDuration() {
		return runDuration;
	}

	public void setRunDuration(String runDuration) {
		firePropertyChange("runDuration", this.runDuration, runDuration);
		this.runDuration = runDuration;
	}

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
		setRunDuration("HH 'Hours' mm 'Minutes' ss 'Seconds'");
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
		return String.format("Issues Fixed: %d", totalIssuesFixed);
	}

	public String getHoursSaved() {
		Duration totalTimeSaved = EliminatedTechnicalDebt.getTotalFor(refactoringPipeline.getRules());
		String formatted = DurationFormatUtils.formatDuration(totalTimeSaved.toMillis(),
				"dd 'Days' HH 'Hours' mm 'Minutes'", false);
		formatted = formatted.replaceAll("(^0 Days\\s)", "");
		formatted = formatted.replaceAll("(^0 Hours\\s)", "");
		formatted = formatted.replaceAll("(^0 Minutes\\s)", "");
		return formatted;
	}

	public void setIsFreeLicense(Boolean validLicense) {
		firePropertyChange("isFreeLicense", isFreeLicense, validLicense);
		isFreeLicense = validLicense;
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
			.forEach(rule -> {
				String name = rule.getRuleDescription()
					.getName();
				int times = RuleApplicationCount.getFor(rule)
					.toInt();
				Duration timeSaved = EliminatedTechnicalDebt.get(rule);
				String timeSavedString = String.format("%s Minutes", timeSaved.toMinutes());
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
		String left = initialSource.get(state) == null ? "" : initialSource.get(state);
		String right = finalSource.get(state) == null ? "" : finalSource.get(state);
		return new ChangedFilesModel(fileName, left, right);
	}

}
