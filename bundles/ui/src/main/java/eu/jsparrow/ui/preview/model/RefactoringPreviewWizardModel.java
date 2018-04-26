package eu.jsparrow.ui.preview.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import eu.jsparrow.rules.common.RefactoringRule;

public class RefactoringPreviewWizardModel extends BaseModel {

	private Map<RefactoringRule, List<String>> changedFilesPerRule;

	public RefactoringPreviewWizardModel() {
		changedFilesPerRule = new HashMap<>();
	}

	public Map<RefactoringRule, List<String>> getChangedFilesPerRule() {
		return changedFilesPerRule;
	}

	public void addRule(RefactoringRule rule) {
		changedFilesPerRule.putIfAbsent(rule, new ArrayList<>());
	}

	public List<String> getFilesForRule(RefactoringRule rule) {
		return changedFilesPerRule.get(rule);
	}

	public void removeFileFromRule(RefactoringRule rule, String compilationUnitHandle) {
		changedFilesPerRule.get(rule)
			.remove(compilationUnitHandle);
	}

	public void addFileToRule(RefactoringRule rule, String compilationUnitHandle) {
		changedFilesPerRule.get(rule)
			.add(compilationUnitHandle);
	}
	
}
