package eu.jsparrow.ui.preview.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.ICompilationUnit;

import eu.jsparrow.core.rule.RefactoringRuleInterface;
import eu.jsparrow.core.rule.statistics.FileChangeCount;
import eu.jsparrow.core.rule.statistics.RuleApplicationCount;

public class RefactoringPreviewWizardModel extends BaseModel {

	private Map<RefactoringRuleInterface, List<String>> changedFilesPerRule;

	public RefactoringPreviewWizardModel() {
		changedFilesPerRule = new HashMap<>();
	}

	public Map<RefactoringRuleInterface, List<String>> getChangedFilesPerRule() {
		return changedFilesPerRule;
	}

	public void addRule(RefactoringRuleInterface rule) {
		changedFilesPerRule.putIfAbsent(rule, new ArrayList<>());
	}

	public List<String> getFilesForRule(RefactoringRuleInterface rule) {
		return changedFilesPerRule.get(rule);
	}

	public void removeFileFromRule(RefactoringRuleInterface rule, String compilationUnitHandle) {
		changedFilesPerRule.get(rule)
			.remove(compilationUnitHandle);
	}

	public void addFileToRule(RefactoringRuleInterface rule, String compilationUnitHandle) {
		changedFilesPerRule.get(rule)
			.add(compilationUnitHandle);
	}

	public void clearCounterForChangedFile(ICompilationUnit newSelection) {
		for (RefactoringRuleInterface rule : changedFilesPerRule.keySet()) {
			if (getFilesForRule(rule).contains(newSelection.getHandleIdentifier())) {
				FileChangeCount count = RuleApplicationCount.getFor(rule)
					.getApplicationsForFile(newSelection.getHandleIdentifier());
				count.clear();
			}
		}
	}

}
