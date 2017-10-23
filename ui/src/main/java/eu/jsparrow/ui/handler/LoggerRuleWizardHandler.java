package eu.jsparrow.ui.handler;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
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
import org.eclipse.jface.wizard.ProgressMonitorPart;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.jsparrow.core.exception.RefactoringException;
import eu.jsparrow.core.exception.SimonykeesException;
import eu.jsparrow.core.refactorer.RefactoringPipeline;
import eu.jsparrow.core.rule.impl.logger.StandardLoggerRule;
import eu.jsparrow.i18n.ExceptionMessages;
import eu.jsparrow.i18n.Messages;
import eu.jsparrow.ui.Activator;
import eu.jsparrow.ui.dialog.CompilationErrorsMessageDialog;
import eu.jsparrow.ui.dialog.SimonykeesMessageDialog;
import eu.jsparrow.ui.util.LicenseUtil;
import eu.jsparrow.ui.util.WizardHandlerUtil;
import eu.jsparrow.ui.wizard.impl.SelectRulesWizard;
import eu.jsparrow.ui.wizard.semiautomatic.LoggerRuleWizard;

/**
 * Handler for semi-automatic logging rule
 * 
 * @author Andreja Sambolec, Matthias Webhofer
 * @since 1.2
 *
 */
public class LoggerRuleWizardHandler extends AbstractHandler {

	private static final Logger logger = LoggerFactory.getLogger(LoggerRuleWizardHandler.class);
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {

		if (Activator.isRunning()) {
			SimonykeesMessageDialog.openMessageDialog(Display.getDefault()
				.getActiveShell(), Messages.SelectRulesWizardHandler_allready_running, MessageDialog.INFORMATION);
		} else {
			Activator.setRunning(true);

			if (!LicenseUtil.getInstance()
				.isValid()) {
				/*
				 * do not display the Wizard if the license is invalid
				 */
				final Shell shell = HandlerUtil.getActiveShell(event);
				if (!LicenseUtil.getInstance()
					.displayLicenseErrorDialog(shell)) {
					Activator.setRunning(false);
					return null;
				}
			}
			List<IJavaElement> selectedJavaElements = WizardHandlerUtil.getSelectedJavaElements(event);
			if (!selectedJavaElements.isEmpty()) {
				IJavaProject selectedJavaProjekt = selectedJavaElements.get(0)
					.getJavaProject();
				StandardLoggerRule loggerRule = new StandardLoggerRule();

				if (null != selectedJavaProjekt) {
					loggerRule.calculateEnabledForProject(selectedJavaProjekt);
					if (loggerRule.isEnabled()) {

						if (SimonykeesMessageDialog.openConfirmDialog(HandlerUtil.getActiveShell(event),
								NLS.bind(Messages.LoggerRuleWizardHandler_info_supportedFrameworkFound,
										loggerRule.getAvailableLoggerType()))) {
							RefactoringPipeline refactoringPipeline = new RefactoringPipeline();

							Job job = new Job(Messages.ProgressMonitor_SelectRulesWizard_performFinish_jobName) {

								@Override
								protected IStatus run(IProgressMonitor monitor) {

									try {
										List<ICompilationUnit> compilationUnits = new LinkedList<>();
										SelectRulesWizard.collectICompilationUnits(compilationUnits,
												selectedJavaElements, monitor);
										List<ICompilationUnit> containingErrorList = refactoringPipeline
											.prepareRefactoring(compilationUnits, monitor);
										if (monitor.isCanceled()) {
											/*
											 * Workaround that prevents selection of multiple projects in the Package
											 * Explorer.
											 * 
											 * See SIM-496
											 */
											if (refactoringPipeline.isMultipleProjects()) {
												synchronizeWithUIShowMultiprojectMessage();
											}
											refactoringPipeline.clearStates();
											Activator.setRunning(false);
											return Status.CANCEL_STATUS;
										} else if (null != containingErrorList && !containingErrorList.isEmpty()) {
											synchronizeWithUIShowCompilationErrorMessage(containingErrorList, event,
													refactoringPipeline, selectedJavaElements, loggerRule,
													selectedJavaProjekt);
										} else {
											synchronizeWithUIShowLoggerRuleWizard(event, refactoringPipeline,
													selectedJavaElements, loggerRule, selectedJavaProjekt);
										}

									} catch (RefactoringException e) {
										logger.error(e.getMessage(), e);
										synchronizeWithUIShowInfo(e);
										return Status.CANCEL_STATUS;
									} catch (JavaModelException jme) {
										logger.error(jme.getMessage(), jme);
										synchronizeWithUIShowInfo(new RefactoringException(
												ExceptionMessages.RefactoringPipeline_java_element_resolution_failed,
												ExceptionMessages.RefactoringPipeline_user_java_element_resolution_failed,
												jme));
										return Status.CANCEL_STATUS;
									}

									return Status.OK_STATUS;
								}
							};

							job.setUser(true);
							job.schedule();

							return true;

						} else {
							Activator.setRunning(false);
						}
					} else {
						SimonykeesMessageDialog.openMessageDialog(HandlerUtil.getActiveShell(event),
								Messages.LoggerRuleWizardHandler_noLogger, MessageDialog.WARNING);
						Activator.setRunning(false);
					}

				}
			}

		}
		return null;

	}

	/**
	 * Method used to open SelectRulesWizard from non UI thread
	 */
	private void synchronizeWithUIShowLoggerRuleWizard(ExecutionEvent event, RefactoringPipeline refactoringPipeline,
			List<IJavaElement> selectedJavaElements, StandardLoggerRule loggerRule, IJavaProject selectedJavaProjekt) {
		Display.getDefault()
			.asyncExec(() -> {
				Shell shell = PlatformUI.getWorkbench()
					.getActiveWorkbenchWindow()
					.getShell();
				// HandlerUtil.getActiveShell(event)
				final WizardDialog dialog = new WizardDialog(shell,
						new LoggerRuleWizard(selectedJavaProjekt, loggerRule, refactoringPipeline)) {
					/*
					 * Removed unnecessary empty space on the bottom of the
					 * wizard intended for ProgressMonitor that is not
					 * used(non-Javadoc)
					 * 
					 * @see org.eclipse.jface.wizard.WizardDialog#
					 * createDialogArea(org.eclipse.swt.widgets. Composite)
					 */
					@Override
					protected Control createDialogArea(Composite parent) {
						Control ctrl = super.createDialogArea(parent);
						getProgressMonitor();
						return ctrl;
					}

					@Override
					protected IProgressMonitor getProgressMonitor() {
						ProgressMonitorPart monitor = (ProgressMonitorPart) super.getProgressMonitor();
						GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
						gridData.heightHint = 0;
						monitor.setLayoutData(gridData);
						monitor.setVisible(false);
						return monitor;
					}
				};

				dialog.open();
			});
	}

	/**
	 * Method used to open CompilationErrorsMessageDialog from non UI thread to list
	 * all Java files that will be skipped because they contain compilation errors.
	 */
	private void synchronizeWithUIShowCompilationErrorMessage(List<ICompilationUnit> containingErrorList,
			ExecutionEvent event, RefactoringPipeline refactoringPipeline, List<IJavaElement> selectedJavaElements,
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
						synchronizeWithUIShowLoggerRuleWizard(event, refactoringPipeline, selectedJavaElements,
								loggerRule, selectedJavaProjekt);
					} else {
						synchronizeWithUIShowWarningNoComlipationUnitDialog();
					}
				} else {
					Activator.setRunning(false);
				}
			});
	}

	/**
	 * Method used to open InformationDialog from non UI thread RefactoringException
	 * is thrown if java element does not exist or if an exception occurs while
	 * accessing its corresponding resource, or if no working copies were found to
	 * apply
	 */
	private void synchronizeWithUIShowInfo(SimonykeesException exception) {
		Display.getDefault()
			.asyncExec(() -> {
				Shell shell = PlatformUI.getWorkbench()
					.getActiveWorkbenchWindow()
					.getShell();
				SimonykeesMessageDialog.openMessageDialog(shell, exception.getUiMessage(), MessageDialog.INFORMATION);

				Activator.setRunning(false);
			});
	}

	/**
	 * Method used to open MessageDialog informing the user that selection contains
	 * no Java files without compilation error from non UI thread
	 */
	private void synchronizeWithUIShowWarningNoComlipationUnitDialog() {
		Display.getDefault()
			.asyncExec(() -> {
				Shell shell = PlatformUI.getWorkbench()
					.getActiveWorkbenchWindow()
					.getShell();
				SimonykeesMessageDialog.openMessageDialog(shell, Messages.SelectRulesWizardHandler_noFileWithoutError,
						MessageDialog.INFORMATION);

				Activator.setRunning(false);
			});
	}

	private void synchronizeWithUIShowMultiprojectMessage() {
		Display.getDefault()
			.asyncExec(() -> {
				Shell shell = PlatformUI.getWorkbench()
					.getActiveWorkbenchWindow()
					.getShell();
				SimonykeesMessageDialog.openMessageDialog(shell,
						Messages.SelectRulesWizardHandler_multipleProjectsWarning, MessageDialog.WARNING);
			});
	}
}
