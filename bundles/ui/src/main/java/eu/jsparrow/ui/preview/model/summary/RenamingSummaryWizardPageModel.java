package eu.jsparrow.ui.preview.model.summary;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipse.jdt.core.ICompilationUnit;

import eu.jsparrow.core.refactorer.RefactoringPipeline;
import eu.jsparrow.core.rule.impl.FieldsRenamingRule;
import eu.jsparrow.core.visitor.renaming.FieldMetaData;
import eu.jsparrow.core.visitor.renaming.JavaAccessModifier;
import eu.jsparrow.rules.common.RefactoringRule;
import eu.jsparrow.ui.preview.model.RefactoringPreviewWizardModel;

public class RenamingSummaryWizardPageModel extends AbstractSummaryWizardPageModel {

	private IObservableList<RulesPerFileModel> rulesPerFile = new WritableList<>();

	public RenamingSummaryWizardPageModel(RefactoringPipeline refactoringPipeline,
			RefactoringPreviewWizardModel wizardModel) {
		super(refactoringPipeline, wizardModel);
	}

	private void addRulesPerFile() {
		ChangedFilesModel firstFile = changedFiles.get(0);
		firstFile.getRules()
			.forEach(rule -> {
				rulesPerFile.add(new RulesPerFileModel(rule));
			});

	}

	public void updateData() {
		updateChangedFiles();

		super.updateData();
	}

	@Override
	protected void updateChangedFiles() {
		super.updateChangedFiles();
		rulesPerFile.clear();
		addRulesPerFile();
	}

	@Override
	protected void initialize() {
		super.initialize();
		rulesPerFile = new WritableList<>();
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

	public IObservableList<RulesPerFileModel> getRulesPerFile() {
		return rulesPerFile;
	}

	@Override
	protected List<String> computeRuleNames(RefactoringRule rule, ICompilationUnit compUnit) {
		List<String> rulesWithChanges = new ArrayList<>();
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

	private List<String> computeEntriesForExternalReferences(List<FieldMetaData> metaDataList,
			String compilationUnitName) {
		List<String> entries = new ArrayList<>();
		Stream.of(JavaAccessModifier.PROTECTED, JavaAccessModifier.PACKAGE_PRIVATE, JavaAccessModifier.PUBLIC)
			.forEach(modifier -> {
				long count = countReferencesOfExternalFields(metaDataList, compilationUnitName, modifier);
				if (count > 0) {
					String value = count + " References of renamed " + modifier.toString()
							+ " fields declared in other classes";
					entries.add(value);
				}

			});
		return entries;
	}

	private List<String> computeEntriesForFields(List<FieldMetaData> metaDataList, String compUnitName) {
		List<JavaAccessModifier> compUnitAccessModifiers = metaDataList.stream()
			.filter(md -> compUnitName.equals(md.getClassDeclarationName()))
			.map(FieldMetaData::getFieldModifier)
			.collect(Collectors.toList());

		List<String> entries = new ArrayList<>();
		Stream
			.of(JavaAccessModifier.PRIVATE, JavaAccessModifier.PROTECTED, JavaAccessModifier.PACKAGE_PRIVATE,
					JavaAccessModifier.PUBLIC)
			.forEach(modifier -> {
				long count = compUnitAccessModifiers.stream()
					.filter(modifier::equals)
					.count();
				if (count > 0) {
					entries.add(count + " " + modifier.toString() + " Fields");
				}
			});
		return entries;
	}

}
