package eu.jsparrow.ui.preview.model;

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
import eu.jsparrow.core.rule.EliminatedTechnicalDebt;
import eu.jsparrow.core.rule.RefactoringRule;
import eu.jsparrow.core.rule.RuleApplicationCount;
import eu.jsparrow.core.visitor.AbstractASTRewriteASTVisitor;
import eu.jsparrow.ui.preview.model.summary.ChangedFilesModel;

public class SummaryWizardPageModel extends BaseModel {

	private final RefactoringPipeline refactoringPipeline;

	private String executionTime;

	private String issuesFixed;

	private String hoursSaved;

	private Boolean isFreeLicense;

	private Map<RefactoringState, String> initialSource = new HashMap<>();

	private Map<RefactoringState, String> finalSource = new HashMap<>();

	private IObservableList<RuleTimesModel> ruleTimes;

	private IObservableList<ChangedFilesModel> changedFiles;

	public SummaryWizardPageModel(RefactoringPipeline refactoringPipeline) {
		this.refactoringPipeline = refactoringPipeline;
		initialSource.putAll(refactoringPipeline.getInitialSourceMap());
		refactoringPipeline.setSourceMap(finalSource);
	}

	public IObservableList<ChangedFilesModel> getChangedFiles() {
		IObservableList<ChangedFilesModel> changedFiles = new WritableList<>();
		refactoringPipeline.getInitialSourceMap()
			.entrySet()
			.stream()
			.filter(this::noChangesOnRefactoringState)
			.forEach(x -> {
				RefactoringState state = x.getKey();
				changedFiles.add(createModelFromRefactoringState(state));
			});
		return changedFiles;
	}

	public IObservableList<RuleTimesModel> getRuleTimes() {
		IObservableList<RuleTimesModel> ruleTimes = new WritableList<>();
		refactoringPipeline.getRules()
			.forEach(x -> {
				String name = x.getRuleDescription()
					.getName();
				int times = RuleApplicationCount.getFor(x)
					.toInt();
				RuleTimesModel ruleTimesModel = new RuleTimesModel(name, times);
				ruleTimes.add(ruleTimesModel);
			});
		return ruleTimes;
	}

	public String getExecutionTime() {
		return "Run Duration: 30 Seconds";
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
		return String.format("Minutes Saved: %s", totalTimeSaved.toMinutes());
	}

	public void setIsFreeLicense(Boolean validLicense) {
		this.isFreeLicense = validLicense;
		firePropertyChange("isFreeLicense", this.isFreeLicense, this.isFreeLicense);
	}

	public Boolean getIsFreeLicense() {
		return isFreeLicense;
	}

	private String getPathString(ICompilationUnit compilationUnit) {
		String temp = compilationUnit.getParent()
			.getPath()
			.toString();
		return StringUtils.startsWith(temp, "/") ? StringUtils.substring(temp, 1) : temp; //$NON-NLS-1$
	}

	private boolean noChangesOnRefactoringState(Entry<RefactoringState, String> entry) {
		RefactoringState state = entry.getKey();
		// Filter out those refactoring states that were deselected or
		// have no changes present.
		if (!state.hasChange()) {
			return true;
		}
		Boolean ignoredRule = refactoringPipeline.getRules()
			.stream()
			.anyMatch(rule -> state.getIgnoredRules()
				.contains(rule));
		Boolean noChangePresent = refactoringPipeline.getRules()
			.stream()
			.noneMatch(rule -> null == state.getChangeIfPresent(rule));
		return !ignoredRule && !noChangePresent;
	}

	private ChangedFilesModel createModelFromRefactoringState(RefactoringState state) {
		ICompilationUnit compUnit = state.getWorkingCopy();
		String fileName = String.format("%s - %s", compUnit.getElementName(), getPathString(compUnit)); //$NON-NLS-1$
		String left = initialSource.get(state) == null ? "" : initialSource.get(state);
		String right = finalSource.get(state) == null ? "" : finalSource.get(state);
		return new ChangedFilesModel(fileName, left, right);
	}

	public class RuleTimesModel extends BaseModel {

		private String name;

		private Integer times;

		public String getName() {
			return name;
		}

		public Integer getTimes() {
			return times;
		}

		public RuleTimesModel(String name, Integer times) {
			this.name = name;
			this.times = times;
		}
	}

}
