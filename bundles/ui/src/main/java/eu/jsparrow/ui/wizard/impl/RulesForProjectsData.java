package eu.jsparrow.ui.wizard.impl;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.eclipse.jdt.core.IJavaProject;

import eu.jsparrow.rules.common.RefactoringRule;

/**
 * Stores a set of {@link IJavaProject} instances and the corresponding list of
 * all available {@link RefactoringRule} instances.
 */
public class RulesForProjectsData {
	private final List<RefactoringRule> rulesChoice;
	private final Set<IJavaProject> javaProjects;
	private List<RefactoringRule> customRulesSelection;
	private String selectedProfileId;

	RulesForProjectsData(List<RefactoringRule> rulesChoice, Set<IJavaProject> javaProjects) {
		this.rulesChoice = rulesChoice;
		this.javaProjects = javaProjects;
	}

	public List<RefactoringRule> getRulesChoice() {
		return rulesChoice;
	}

	public Set<IJavaProject> getJavaProjects() {
		return javaProjects;
	}

	public Optional<String> getSelectedProfileId() {
		return Optional.ofNullable(selectedProfileId);
	}

	public void setSelectedProfileId(String selectedProfileId) {
		this.selectedProfileId = selectedProfileId;
		this.customRulesSelection = null;
	}

	public List<RefactoringRule> getCustomRulesSelection() {
		if(customRulesSelection != null) {
			return customRulesSelection;
		}
		return Collections.emptyList();
	}

	public void setCustomRulesSelection(List<RefactoringRule> customRulesSelection) {
		this.selectedProfileId = null;
		this.customRulesSelection = customRulesSelection;
	}

}