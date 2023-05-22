package eu.jsparrow.ui.handler;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.jsparrow.core.refactorer.RefactoringPipeline;
import eu.jsparrow.core.rule.impl.logger.StandardLoggerRule;
import eu.jsparrow.i18n.ExceptionMessages;
import eu.jsparrow.i18n.Messages;
import eu.jsparrow.rules.common.exception.RefactoringException;
import eu.jsparrow.ui.Activator;
import eu.jsparrow.ui.dialog.CompilationErrorsMessageDialog;
import eu.jsparrow.ui.dialog.SimonykeesMessageDialog;
import eu.jsparrow.ui.wizard.impl.SelectRulesWizard;
import eu.jsparrow.ui.wizard.impl.WizardMessageDialog;
import eu.jsparrow.ui.wizard.semiautomatic.LoggerRuleWizard;
import eu.jsparrow.ui.wizard.semiautomatic.LoggerRuleWizardData;

/**
 * Handler for semi-automatic logging rule
 * 
 * @author Andreja Sambolec, Matthias Webhofer
 * @since 1.2
 *
 */
public class LoggerRuleWizardHandler extends AbstractRuleWizardHandler {

	private static final Logger logger = LoggerFactory.getLogger(LoggerRuleWizardHandler.class);

	@Override
	protected Optional<Job> createJob(Map<IJavaProject, List<IJavaElement>> selectedJavaElements) {
		Shell shell = Display.getDefault()
			.getActiveShell();
		if (selectedJavaElements.size() != 1) {
			String title = Messages.LoggerRuleWizardHandler_multipleProjectsSelected;
			String message = Messages.LoggerRuleWizardHandler_loggerRuleOnOneProjectOnly;

			synchronizeWithUIShowSelectionErrorMessage(shell, title, message);
			return Optional.empty();
		}

		List<IJavaElement> selectedElements = selectedJavaElements.entrySet()
			.iterator()
			.next()
			.getValue();
		IJavaProject selectedJavaProjekt = selectedElements.get(0)
			.getJavaProject();
		StandardLoggerRule loggerRule = new StandardLoggerRule();

		if (null == selectedJavaProjekt) {
			SimonykeesMessageDialog.openMessageDialog(shell,
					"The Java Project of the selected sources could not be found.", MessageDialog.WARNING); //$NON-NLS-1$
			return Optional.empty();
		}

		loggerRule.calculateEnabledForProject(selectedJavaProjekt);
		if (!loggerRule.isEnabled()) {
			SimonykeesMessageDialog.openMessageDialog(shell, Messages.LoggerRuleWizardHandler_noLogger,
					MessageDialog.WARNING);
			return Optional.empty();
		}

		boolean confirmed = SimonykeesMessageDialog.openConfirmDialog(shell,
				NLS.bind(Messages.LoggerRuleWizardHandler_info_supportedFrameworkFound,
						loggerRule.getAvailableLoggerType()));
		if (!confirmed) {
			return Optional.empty();
		}

		RefactoringPipeline refactoringPipeline = new RefactoringPipeline();
		Job job = new Job(Messages.ProgressMonitor_verifying_project_information) {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				return startLoggerRuleWizard(selectedElements, selectedJavaProjekt, loggerRule,
						refactoringPipeline, monitor);
			}
		};

		return Optional.of(job);
	}

	private IStatus startLoggerRuleWizard(List<IJavaElement> selectedElements,
			IJavaProject selectedJavaProjekt, StandardLoggerRule loggerRule,
			RefactoringPipeline refactoringPipeline, IProgressMonitor monitor) {
		try {
			List<ICompilationUnit> compilationUnits = new LinkedList<>();
			SelectRulesWizard.collectICompilationUnits(compilationUnits,
					selectedElements, monitor);
			List<ICompilationUnit> containingErrorList = refactoringPipeline
				.prepareRefactoring(compilationUnits, monitor);
			if (monitor.isCanceled()) {
				/*
				 * Workaround that prevents selection of multiple projects in
				 * the Package Explorer.
				 * 
				 * See SIM-496
				 */
				if (refactoringPipeline.isMultipleProjects()) {
					WizardMessageDialog.synchronizeWithUIShowMultiprojectMessage();
				}
				refactoringPipeline.clearStates();
				Activator.setRunning(false);
				return Status.CANCEL_STATUS;
			} else if (null != containingErrorList && !containingErrorList.isEmpty()) {
				synchronizeWithUIShowCompilationErrorMessage(containingErrorList,
						refactoringPipeline, loggerRule,
						selectedJavaProjekt);
			} else {
				LoggerRuleWizardData loggerRuleWizardData = new LoggerRuleWizardData(selectedJavaProjekt, loggerRule);
				LoggerRuleWizard.synchronizeWithUIShowLoggerRuleWizard(refactoringPipeline, loggerRuleWizardData);
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
	 * Method used to open CompilationErrorsMessageDialog from non UI thread to
	 * list all Java files that will be skipped because they contain compilation
	 * errors.
	 */
	private void synchronizeWithUIShowCompilationErrorMessage(List<ICompilationUnit> containingErrorList,
			RefactoringPipeline refactoringPipeline,
			StandardLoggerRule loggerRule, IJavaProject selectedJavaProjekt) {
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
						LoggerRuleWizardData loggerRuleWizardData = new LoggerRuleWizardData(selectedJavaProjekt,
								loggerRule);
						LoggerRuleWizard.synchronizeWithUIShowLoggerRuleWizard(refactoringPipeline,
								loggerRuleWizardData);
					} else {
						WizardMessageDialog.synchronizeWithUIShowWarningNoComlipationUnitWithoutErrorsDialog();
					}
				} else {
					Activator.setRunning(false);
				}
			});
	}
}
