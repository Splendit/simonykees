package eu.jsparrow.ui.handler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.CoreException;
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
import org.eclipse.ui.handlers.HandlerUtil;
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
import eu.jsparrow.ui.util.LicenseUtil;
import eu.jsparrow.ui.util.LicenseUtilService;
import eu.jsparrow.ui.util.WizardHandlerUtil;
import eu.jsparrow.ui.wizard.impl.RunDefaultProfileImplicitWizard;
import eu.jsparrow.ui.wizard.impl.SelectRulesWizard;
import eu.jsparrow.ui.wizard.impl.WizardMessageDialog;

public class RunDefaultProfileHandler extends AbstractRuleWizardHandler {

	private static final Logger logger = LoggerFactory.getLogger(RunDefaultProfileHandler.class);
	private LicenseUtilService licenseUtil = LicenseUtil.get();
	private RunDefaultProfileImplicitWizard implicitWizard = new RunDefaultProfileImplicitWizard();

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {

		if (Activator.isRunning()) {
			super.openAlreadyRunningDialog();
			return null;
		}
		Activator.setRunning(true);
		final Shell shell = HandlerUtil.getActiveShell(event);
		if (!licenseUtil.checkAtStartUp(shell)) {
			Activator.setRunning(false);
			return null;
		}

		Map<IJavaProject, List<IJavaElement>> selectedJavaElements;
		try {
			selectedJavaElements = WizardHandlerUtil.getSelectedJavaElements(event);
		} catch (CoreException e) {
			logger.error(e.getMessage(), e);
			WizardMessageDialog.synchronizeWithUIShowError(new RefactoringException(
					Messages.SelectRulesWizardHandler_getting_selected_resources_failed + e.getMessage(),
					Messages.SelectRulesWizardHandler_user_getting_selected_resources_failed, e));
			return null;
		}

		if (selectedJavaElements.isEmpty()) {
			WizardMessageDialog.synchronizedWithUIShowWarningNoCompilationUnitDialog();
			logger.error(Messages.WizardMessageDialog_selectionDidNotContainAnyJavaFiles);
			Activator.setRunning(false);
			return null;
		}

		Job job = new Job(Messages.RunDefaultProfileHandler_startJSparrowWithDefaultProfile) {

			@Override
			protected IStatus run(IProgressMonitor monitor) {
				RefactoringPipeline refactoringPipeline = new RefactoringPipeline();
				return runDefaultProfile(selectedJavaElements, monitor, refactoringPipeline);
			}
		};

		job.setUser(true);
		job.schedule();

		return true;
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
		implicitWizard.computeRefactoring(refactoringPipeline, javaProjects, rules);

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
