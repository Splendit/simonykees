package eu.jsparrow.ui.preview.model;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipse.jdt.core.ICompilationUnit;

import eu.jsparrow.core.refactorer.RefactoringPipeline;
import eu.jsparrow.core.refactorer.RefactoringState;
import eu.jsparrow.core.rule.EliminatedTechnicalDebt;
import eu.jsparrow.core.rule.RuleApplicationCount;

public class SummaryWizardPageModel extends ModelObject {

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
			.forEach(x -> {
				RefactoringState state = x.getKey();
				ICompilationUnit compUnit = state.getWorkingCopy();
				String fileName = String.format("%s - %s", compUnit.getElementName(), getPathString(compUnit)); //$NON-NLS-1$
				String left = initialSource.get(state) == null ? "" : initialSource.get(state);
				String right = finalSource.get(state) == null ? "" : finalSource.get(state);
				ChangedFilesModel model = new ChangedFilesModel(fileName, left, right);
				changedFiles.add(model);
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

	public class RuleTimesModel extends ModelObject {

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

	public class ChangedFilesModel extends ModelObject {

		private String name;

		private String sourceLeft;

		private String sourceRight;

		public ChangedFilesModel(String name) {
			this.name = name;
		}

		public ChangedFilesModel(String name, String sourceLeft, String sourceRight) {
			this(name);
			this.sourceLeft = sourceLeft;
			this.sourceRight = sourceRight;
		}

		public String getName() {
			return name;
		}

		public String getSourceLeft() {
			return sourceLeft;
		}

		public String getSourceRight() {
			return sourceRight;
		}

		public void setSourceLeft(String sourceLeft) {
			this.sourceLeft = sourceLeft;
		}

		public void setSourceRight(String sourceRight) {
			firePropertyChange("sourceRight", this.name, this.name = name);
		}

	}

}
