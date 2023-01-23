package eu.jsparrow.core.rule;

import java.util.List;
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
}