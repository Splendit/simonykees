package eu.jsparrow.ui.preview.model.summary;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipse.jdt.core.ICompilationUnit;

import eu.jsparrow.core.refactorer.RefactoringPipeline;
import eu.jsparrow.rules.common.RefactoringRule;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.ui.preview.model.RefactoringPreviewWizardModel;

public class RefactoringSummaryWizardPageModel extends AbstractSummaryWizardPageModel {

	private IObservableList<RulesPerFileModel> rulesPerFile = new WritableList<>();

	public RefactoringSummaryWizardPageModel(RefactoringPipeline refactoringPipeline,
			RefactoringPreviewWizardModel wizardModel) {
		super(refactoringPipeline, wizardModel);
	}

	public IObservableList<RulesPerFileModel> getRulesPerFile() {
		return rulesPerFile;
	}

	@Override
	public void updateData() {
		updateChangedFiles();
		super.updateData();
	}

	@Override
	protected void initialize() {
		super.initialize();
		rulesPerFile = new WritableList<>();
		addRulesPerFile();
	}

	private void addRulesPerFile() {
		ChangedFilesModel firstFile = changedFiles.get(0);
		firstFile.getRules()
			.forEach(rule -> {
				rulesPerFile.add(new RulesPerFileModel(rule));
			});

	}

	@Override
	protected void updateChangedFiles() {
		super.updateChangedFiles();
		rulesPerFile.clear();
		addRulesPerFile();
	}

	@Override
	public void updateRulesPerFile(List<String> rules) {
		List<RulesPerFileModel> newRules = rules.stream()
			.map(RulesPerFileModel::new)
			.collect(Collectors.toList());
		this.rulesPerFile.clear();
		this.rulesPerFile.addAll(newRules);

	}

	protected List<String> computeRuleNames(RefactoringRule rule, ICompilationUnit compUnit) {
		List<String> rulesWithChanges = new ArrayList<>();
		RuleDescription ruleDescription = rule.getRuleDescription();
		rulesWithChanges.add(ruleDescription.getName());
		return rulesWithChanges;
	}
}
