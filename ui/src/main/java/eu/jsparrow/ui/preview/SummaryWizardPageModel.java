package eu.jsparrow.ui.preview;

import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.list.WritableList;

import eu.jsparrow.core.refactorer.RefactoringPipeline;

public class SummaryWizardPageModel extends ModelObject {

	private final RefactoringPipeline refactoringPipeline;

	private String executionTime;

	private String issuesFixed;

	private String hoursSaved;
	
	private IObservableList<RuleTimesModel> ruleTimes;
	
	private IObservableList<ChangedFilesModel> changedFiles;

	public IObservableList<ChangedFilesModel> getChangedFiles() {
		IObservableList<ChangedFilesModel> changedFiles = new WritableList<>();
		changedFiles.add(new ChangedFilesModel("file1.java"));
		changedFiles.add(new ChangedFilesModel("file1.java"));
		changedFiles.add(new ChangedFilesModel("file1.java"));
		changedFiles.add(new ChangedFilesModel("file1.java"));
		changedFiles.add(new ChangedFilesModel("file1.java"));
		changedFiles.add(new ChangedFilesModel("file1.java"));
		return changedFiles;
	}

	public IObservableList<RuleTimesModel> getRuleTimes() {
		IObservableList<RuleTimesModel> ruleTimes = new WritableList<>();
		ruleTimes.add(new RuleTimesModel("test", 1));
		ruleTimes.add(new RuleTimesModel("test", 1));
		ruleTimes.add(new RuleTimesModel("test", 1));
		ruleTimes.add(new RuleTimesModel("test", 1));
		ruleTimes.add(new RuleTimesModel("test", 1));
		ruleTimes.add(new RuleTimesModel("test", 1));
		ruleTimes.add(new RuleTimesModel("test", 1));
		ruleTimes.add(new RuleTimesModel("test", 1));
		ruleTimes.add(new RuleTimesModel("test", 1));
		ruleTimes.add(new RuleTimesModel("test", 1));
		ruleTimes.add(new RuleTimesModel("test", 1));
		ruleTimes.add(new RuleTimesModel("test", 1));
		ruleTimes.add(new RuleTimesModel("test", 1));
		ruleTimes.add(new RuleTimesModel("test", 1));
		ruleTimes.add(new RuleTimesModel("test", 1));
		ruleTimes.add(new RuleTimesModel("test", 1));
		ruleTimes.add(new RuleTimesModel("test", 1));
		ruleTimes.add(new RuleTimesModel("test", 1));
		ruleTimes.add(new RuleTimesModel("test", 1));
		ruleTimes.add(new RuleTimesModel("test", 1));
		ruleTimes.add(new RuleTimesModel("test", 1));
		ruleTimes.add(new RuleTimesModel("test", 1));
		ruleTimes.add(new RuleTimesModel("test", 1));
		ruleTimes.add(new RuleTimesModel("test", 1));
		ruleTimes.add(new RuleTimesModel("test", 1));
		ruleTimes.add(new RuleTimesModel("test", 1));
		ruleTimes.add(new RuleTimesModel("test", 1));
		ruleTimes.add(new RuleTimesModel("test", 1));
		return ruleTimes;
	}

	public SummaryWizardPageModel(RefactoringPipeline refactoringPipeline) {
		this.refactoringPipeline = refactoringPipeline;
	}
	
	public String getExecutionTime() {
		return "Run Duration: 30 Seconds";
	}

	public String getIssuesFixed() {
		return "230 Issues fixed";
	}

	public String getHoursSaved() {
		return "1003 Hours Saved";
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
		
		public ChangedFilesModel(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}
	}


}
