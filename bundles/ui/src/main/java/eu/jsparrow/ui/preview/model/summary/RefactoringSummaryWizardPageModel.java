package eu.jsparrow.ui.preview.model.summary;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.ltk.core.refactoring.DocumentChange;

import eu.jsparrow.core.refactorer.RefactoringPipeline;
import eu.jsparrow.core.refactorer.RefactoringState;
import eu.jsparrow.rules.common.RefactoringRule;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.ui.preview.model.RefactoringPreviewWizardModel;

public class RefactoringSummaryWizardPageModel extends AbstractSummaryWizardPageModel {

	protected IObservableList<ChangedFilesModel> changedFiles;
	private IObservableList<RulesPerFileModel> rulesPerFile;

	public RefactoringSummaryWizardPageModel(RefactoringPipeline refactoringPipeline,
			RefactoringPreviewWizardModel wizardModel) {
		super(refactoringPipeline, wizardModel);
	}

	public IObservableList<RulesPerFileModel> getRulesPerFile() {
		return rulesPerFile;
	}

	public IObservableList<ChangedFilesModel> getChangedFiles() {
		return changedFiles;
	}

	@Override
	public void updateData() {
		updateChangedFiles();
		super.updateData();
	}

	@Override
	protected void initialize() {
		changedFiles = new WritableList<>();
		addModifiedFiles();
		super.initialize();
		rulesPerFile = new WritableList<>();
		addRulesPerFile();
	}

	private void addRulesPerFile() {
		ChangedFilesModel firstFile = changedFiles.get(0);
		firstFile.getRules()
			.forEach(rule -> rulesPerFile.add(rule));

	}

	protected void updateChangedFiles() {
		changedFiles.clear();
		addModifiedFiles();
		rulesPerFile.clear();
		addRulesPerFile();
	}

	public void updateRulesPerFile(List<RulesPerFileModel> rules) {
		this.rulesPerFile.clear();
		this.rulesPerFile.addAll(rules);
	}

	private void addModifiedFiles() {
		refactoringPipeline.getInitialSourceMap()
			.entrySet()
			.stream()
			.filter(this::hasChanges)
			.map(Map.Entry::getKey)
			.forEach(state -> changedFiles.add(createModelFromRefactoringState(state)));
	}

	private ChangedFilesModel createModelFromRefactoringState(RefactoringState state) {
		ICompilationUnit compUnit = state.getWorkingCopy();
		String fileName = String.format("%s - %s", compUnit.getElementName(), getPathString(compUnit)); //$NON-NLS-1$
		List<RefactoringRule> rules = refactoringPipeline.getRules();
		List<RulesPerFileModel> rulesWithChanges = new ArrayList<>();
		for (RefactoringRule rule : rules) {
			DocumentChange change = state.getChangeIfPresent(rule);
			if (change != null) {
				RuleDescription ruleDescription = rule.getRuleDescription();
				RulesPerFileModel model = new RulesPerFileModel(ruleDescription.getName());
				rulesWithChanges.add(model);
			}
		}

		return new ChangedFilesModel(fileName, rulesWithChanges);
	}

	@Override
	public String[] getProposalProviderContents() {
		return Stream.concat(
				getRuleTimes()
					.stream()
					.map(RuleTimesModel::getName),
				getChangedFiles()
					.stream()
					.map(ChangedFilesModel::getName))
			.sorted()
			.toArray(String[]::new);
	}
}
