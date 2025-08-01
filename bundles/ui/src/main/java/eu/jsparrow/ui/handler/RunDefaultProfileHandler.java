package eu.jsparrow.ui.handler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.jsparrow.core.refactorer.RefactoringPipeline;
import eu.jsparrow.core.rule.RulesContainer;
import eu.jsparrow.i18n.ExceptionMessages;
import eu.jsparrow.i18n.Messages;
import eu.jsparrow.rules.common.RefactoringRule;
import eu.jsparrow.rules.common.exception.RefactoringException;
import eu.jsparrow.ui.Activator;
import eu.jsparrow.ui.dialog.CompilationErrorsMessageDialog;
import eu.jsparrow.ui.preference.SimonykeesPreferenceManager;
import eu.jsparrow.ui.preference.profile.SimonykeesProfile;
import eu.jsparrow.ui.wizard.impl.RunDefaultProfileImplicitWizard;
import eu.jsparrow.ui.wizard.impl.SelectRulesWizard;
import eu.jsparrow.ui.wizard.impl.SelectRulesWizardData;
import eu.jsparrow.ui.wizard.impl.WizardMessageDialog;

/**
 * Starts the refactoring process with the rules from the selected in default
 * profile.
 * 
 * @since 4.10.0
 *
 */
public class RunDefaultProfileHandler extends AbstractRuleWizardHandler {

	private static final Logger logger = LoggerFactory.getLogger(RunDefaultProfileHandler.class);

	@Override
	protected Optional<Job> createJob(Map<IJavaProject, List<IJavaElement>> selectedJavaElements) {
		Job job = new Job(Messages.RunDefaultProfileHandler_startJSparrowWithDefaultProfile) {

			@Override
			protected IStatus run(IProgressMonitor monitor) {
				RefactoringPipeline refactoringPipeline = new RefactoringPipeline();
				return runDefaultProfile(selectedJavaElements, monitor, refactoringPipeline);
			}
		};
		return Optional.of(job);
	}

	private IStatus runDefaultProfile(Map<IJavaProject, List<IJavaElement>> selectedJavaElements,
			IProgressMonitor monitor, RefactoringPipeline refactoringPipeline) {
		List<ICompilationUnit> compilationUnits = new ArrayList<>();
		try {
			for (List<IJavaElement> elements : selectedJavaElements.values()) {
				SelectRulesWizard.collectICompilationUnits(compilationUnits, elements, monitor);
			}
			List<ICompilationUnit> containingErrorList = refactoringPipeline
				.prepareRefactoring(compilationUnits, monitor);

			if (monitor.isCanceled()) {
				refactoringPipeline.clearStates();
				Activator.setRunning(false);
				return Status.CANCEL_STATUS;
			} else if (null != containingErrorList && !containingErrorList.isEmpty()) {
				synchronizeWithUIShowCompilationErrorMessage(containingErrorList,
						refactoringPipeline, selectedJavaElements);
			} else {
				synchronizeWithUIShowPreviewsWizard(refactoringPipeline, selectedJavaElements);
			}

		} catch (RefactoringException e) {
			logger.error(e.getMessage(), e);
			WizardMessageDialog.synchronizeWithUIShowInfo(e);
			return Status.CANCEL_STATUS;
		} catch (JavaModelException jme) {
			logger.error(jme.getMessage(), jme);
			WizardMessageDialog.synchronizeWithUIShowInfo(new RefactoringException(
					ExceptionMessages.RefactoringPipeline_java_element_resolution_failed,
					ExceptionMessages.RefactoringPipeline_user_java_element_resolution_failed,
					jme));
			return Status.CANCEL_STATUS;
		}
		return Status.OK_STATUS;
	}

	/**
	 * Method used to open Preview wizard from non UI thread
	 */
	private void synchronizeWithUIShowPreviewsWizard(RefactoringPipeline refactoringPipeline,
			Map<IJavaProject, List<IJavaElement>> selectedJavaElements) {

		List<IJavaProject> javaProjects = new ArrayList<>(selectedJavaElements.keySet());
		String currentProfileName = SimonykeesPreferenceManager.getCurrentProfileId();
		List<RefactoringRule> allRules = RulesContainer.getRulesForProjects(selectedJavaElements.keySet(), false);
		SimonykeesPreferenceManager.loadCurrentProfiles();
		List<String> profileRuleIds = SimonykeesPreferenceManager.getProfileFromName(currentProfileName)
			.map(SimonykeesProfile::getEnabledRuleIds)
			.orElse(Collections.emptyList());
		List<RefactoringRule> rules = allRules.stream()
			.filter(rule -> profileRuleIds.contains(rule.getId()))
			.filter(RefactoringRule::isEnabled)
			.collect(Collectors.toList());
		SelectRulesWizardData selectRulesWizardData = SelectRulesWizard
			.createSelectRulesWizardData(selectedJavaElements.keySet());

		RunDefaultProfileImplicitWizard implicitWizard = new RunDefaultProfileImplicitWizard(refactoringPipeline,
				selectRulesWizardData);
		implicitWizard.computeRefactoring(javaProjects, rules);

	}

	/**
	 * Method used to open CompilationErrorsMessageDialog from non UI thread to
	 * list all Java files that will be skipped because they contain compilation
	 * errors.
	 */
	private void synchronizeWithUIShowCompilationErrorMessage(List<ICompilationUnit> containingErrorList,
			RefactoringPipeline refactoringPipeline, Map<IJavaProject, List<IJavaElement>> selectedJavaElements) {
		Display.getDefault()
			.asyncExec(() -> {
				Shell shell = PlatformUI.getWorkbench()
					.getActiveWorkbenchWindow()
					.getShell();
				CompilationErrorsMessageDialog dialog = new CompilationErrorsMessageDialog(shell);
				dialog.create();
				dialog.setTableViewerInput(containingErrorList);
				dialog.open();
				if (dialog.getReturnCode() == IDialogConstants.OK_ID) {
					if (refactoringPipeline.hasRefactoringStates()) {
						synchronizeWithUIShowPreviewsWizard(refactoringPipeline, selectedJavaElements);
					} else {
						WizardMessageDialog.synchronizeWithUIShowWarningNoComlipationUnitWithoutErrorsDialog();
					}
				} else {
					Activator.setRunning(false);
				}
			});
	}
}
