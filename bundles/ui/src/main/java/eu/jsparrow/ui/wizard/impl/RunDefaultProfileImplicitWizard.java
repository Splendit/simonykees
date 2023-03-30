package eu.jsparrow.ui.wizard.impl;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.osgi.util.NLS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.jsparrow.core.refactorer.RefactoringPipeline;
import eu.jsparrow.i18n.Messages;
import eu.jsparrow.rules.common.RefactoringRule;
import eu.jsparrow.ui.wizard.AbstractRuleWizard;

/**
 * Computes refactoring with a given list of rules and opens the Preview Wizard.
 * Does NOT open the Select Rules wizard.
 * 
 * @since 4.10.0
 *
 */
public class RunDefaultProfileImplicitWizard extends AbstractRuleWizard {

	private static final Logger logger = LoggerFactory.getLogger(RunDefaultProfileImplicitWizard.class);

	private SelectRulesWizardData selectRulesWizardData;

	public RunDefaultProfileImplicitWizard(RefactoringPipeline refactoringPipeline,
			SelectRulesWizardData selectRulesWizardData) {
		this.refactoringPipeline = refactoringPipeline;
		this.selectRulesWizardData = selectRulesWizardData;
	}

	public boolean computeRefactoring(Collection<IJavaProject> javaProjects,
			final List<RefactoringRule> selectedRules) {
		String message = NLS.bind(Messages.SelectRulesWizard_start_refactoring, this.getClass()
			.getSimpleName(),
				javaProjects.stream()
					.map(IJavaProject::getElementName)
					.collect(Collectors.joining(";"))); //$NON-NLS-1$
		logger.info(message);

		proceedToRefactoringPreviewWizard(javaProjects, selectedRules, selectRulesWizardData);
		return true;
	}


	@Override
	public boolean performFinish() {
		/*
		 * We do not actually open this wizard.
		 */
		return false;
	}
}
