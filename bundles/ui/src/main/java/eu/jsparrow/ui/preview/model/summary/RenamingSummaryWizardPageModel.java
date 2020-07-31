package eu.jsparrow.ui.preview.model.summary;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.ltk.core.refactoring.DocumentChange;

import eu.jsparrow.core.refactorer.RefactoringPipeline;
import eu.jsparrow.core.refactorer.RefactoringState;
import eu.jsparrow.core.rule.impl.FieldsRenamingRule;
import eu.jsparrow.core.visitor.renaming.FieldMetaData;
import eu.jsparrow.core.visitor.renaming.JavaAccessModifier;
import eu.jsparrow.rules.common.RefactoringRule;
import eu.jsparrow.ui.preview.model.RefactoringPreviewWizardModel;

public class RenamingSummaryWizardPageModel extends AbstractSummaryWizardPageModel {

	private IObservableList<RenamingPerFileModel> rulesPerFile = new WritableList<>();
	protected IObservableList<ChangedNamesInFileModel> changedFiles = new WritableList<>();

	public RenamingSummaryWizardPageModel(RefactoringPipeline refactoringPipeline,
			RefactoringPreviewWizardModel wizardModel) {
		super(refactoringPipeline, wizardModel);
	}

	private void addRulesPerFile() {
		ChangedNamesInFileModel firstFile = changedFiles.get(0);
		firstFile.getRenamings()
			.forEach(rule -> rulesPerFile.add(rule));
	}

	public IObservableList<ChangedNamesInFileModel> getChangedFiles() {
		return changedFiles;
	}

	@Override
	public void updateData() {
		updateChangedFiles();
		super.updateData();
	}

	protected void updateChangedFiles() {
		changedFiles.clear();
		addModifiedFiles();
		rulesPerFile.clear();
		addRulesPerFile();
	}

	@Override
	protected void initialize() {
		changedFiles = new WritableList<>();
		addModifiedFiles();
		super.initialize();
		rulesPerFile = new WritableList<>();
		addRulesPerFile();
	}

	public void updateRulesPerFile(List<RenamingPerFileModel> rules) {
		this.rulesPerFile.clear();
		this.rulesPerFile.addAll(rules);

	}

	public IObservableList<RenamingPerFileModel> getRulesPerFile() {
		return rulesPerFile;
	}

	protected List<RenamingPerFileModel> computeRuleNames(RefactoringRule rule, ICompilationUnit compUnit) {
		List<RenamingPerFileModel> rulesWithChanges = new ArrayList<>();
		String compUnitName = compUnit.getElementName();
		if (rule instanceof FieldsRenamingRule) {
			FieldsRenamingRule fieldsRenaming = (FieldsRenamingRule) rule;
			List<FieldMetaData> metaDataList = fieldsRenaming.getMetaData();
			rulesWithChanges.addAll(computeEntriesForFields(metaDataList, compUnitName));
			rulesWithChanges.addAll(computeEntriesForExternalReferences(metaDataList, compUnitName));
		}
		return rulesWithChanges;
	}

	private long countReferencesOfExternalFields(List<FieldMetaData> metaDataList, String compilationUnitName,
			JavaAccessModifier modifier) {
		return metaDataList.stream()
			.filter(md -> modifier.equals(md.getFieldModifier()))
			.flatMap(metaData -> {
				return metaData.getTargetICompilationUnits()
					.stream()
					.filter(icu -> !icu.getElementName()
						.equals(metaData.getClassDeclarationName()));
			})
			.filter(md -> compilationUnitName.equals(md.getElementName()))
			.count();
	}

	private List<RenamingPerFileModel> computeEntriesForExternalReferences(List<FieldMetaData> metaDataList,
			String compilationUnitName) {
		List<RenamingPerFileModel> entries = new ArrayList<>();
		Stream.of(JavaAccessModifier.PROTECTED, JavaAccessModifier.PACKAGE_PRIVATE, JavaAccessModifier.PUBLIC)
			.forEach(modifier -> {
				long count = countReferencesOfExternalFields(metaDataList, compilationUnitName, modifier);
				if (count > 0) {
					String value = "References of external " + modifier.toString()
							+ " fields";
					entries.add(new RenamingPerFileModel(value, count));
				}

			});
		return entries;
	}

	private List<RenamingPerFileModel> computeEntriesForFields(List<FieldMetaData> metaDataList, String compUnitName) {
		List<JavaAccessModifier> compUnitAccessModifiers = metaDataList.stream()
			.filter(md -> compUnitName.equals(md.getClassDeclarationName()))
			.map(FieldMetaData::getFieldModifier)
			.collect(Collectors.toList());

		List<RenamingPerFileModel> entries = new ArrayList<>();
		Stream
			.of(JavaAccessModifier.PRIVATE, JavaAccessModifier.PROTECTED, JavaAccessModifier.PACKAGE_PRIVATE,
					JavaAccessModifier.PUBLIC)
			.forEach(modifier -> {
				long count = compUnitAccessModifiers.stream()
					.filter(modifier::equals)
					.count();
				if (count > 0) {
					String name = modifier.toString() + " fields";
					entries.add(new RenamingPerFileModel(name, count));
				}
			});
		return entries;
	}

	private void addModifiedFiles() {
		refactoringPipeline.getInitialSourceMap()
			.entrySet()
			.stream()
			.filter(this::hasChanges)
			.map(Map.Entry::getKey)
			.forEach(state -> changedFiles.add(createModelFromRefactoringState(state)));
	}

	private ChangedNamesInFileModel createModelFromRefactoringState(RefactoringState state) {
		ICompilationUnit compUnit = state.getWorkingCopy();
		String fileName = String.format("%s - %s", compUnit.getElementName(), getPathString(compUnit)); //$NON-NLS-1$
		List<RefactoringRule> rules = refactoringPipeline.getRules();
		List<RenamingPerFileModel> rulesWithChanges = new ArrayList<>();
		for (RefactoringRule rule : rules) {
			DocumentChange change = state.getChangeIfPresent(rule);
			if (change != null) {
				List<RenamingPerFileModel> ruleNamesToDisplay = computeRuleNames(rule, compUnit);
				rulesWithChanges.addAll(ruleNamesToDisplay);
			}
		}

		return new ChangedNamesInFileModel(fileName, rulesWithChanges);
	}

	public String[] getProposalProviderContents() {
		return getChangedFiles()
			.stream()
			.map(ChangedNamesInFileModel::getFileName)
			.toArray(String[]::new);
	}
}
