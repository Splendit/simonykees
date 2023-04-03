package eu.jsparrow.ui.wizard.semiautomatic;

import org.eclipse.jdt.core.IJavaProject;

import eu.jsparrow.core.rule.impl.logger.StandardLoggerRule;

/**
 * @since 4.17.0
 */
public class LoggerRuleWizardData {

	private final IJavaProject selectedJavaProject;
	private final StandardLoggerRule rule;

	public LoggerRuleWizardData(IJavaProject selectedJavaProject, StandardLoggerRule rule) {
		this.selectedJavaProject = selectedJavaProject;
		this.rule = rule;
	}

	public IJavaProject getSelectedJavaProject() {
		return selectedJavaProject;
	}

	public StandardLoggerRule getRule() {
		return rule;
	}
}
