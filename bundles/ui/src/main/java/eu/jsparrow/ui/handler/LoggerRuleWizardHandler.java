package eu.jsparrow.ui.handler;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.commands.AbstractHandler;
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

import eu.jsparrow.core.refactorer.RefactoringPipeline;
import eu.jsparrow.core.rule.impl.logger.StandardLoggerRule;
import eu.jsparrow.i18n.ExceptionMessages;
import eu.jsparrow.i18n.Messages;
import eu.jsparrow.rules.common.exception.RefactoringException;
import eu.jsparrow.ui.Activator;
import eu.jsparrow.ui.dialog.CompilationErrorsMessageDialog;
import eu.jsparrow.ui.dialog.SimonykeesMessageDialog;
import eu.jsparrow.ui.util.LicenseUtil;
import eu.jsparrow.ui.util.LicenseUtilService;
import eu.jsparrow.ui.util.WizardHandlerUtil;
import eu.jsparrow.ui.wizard.impl.SelectRulesWizard;
import eu.jsparrow.ui.wizard.impl.WizardMessageDialog;
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

	private LicenseUtilService licenseUtil = LicenseUtil.get();

	public LoggerRuleWizardHandler() {

	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {

		if (Activator.isRunning()) {
			SimonykeesMessageDialog.openMessageDialog(Display.getDefault()
				.getActiveShell(), Messages.SelectRulesWizardHandler_allready_running, MessageDialog.INFORMATION);
		} else {
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
			if (!selectedJavaElements.isEmpty()) {
				if (selectedJavaElements.size() != 1) {
					synchronizeWithUIShowSelectionErrorMessage();
					return false;
				}

				List<IJavaElement> selectedElements = selectedJavaElements.entrySet()
					.iterator()
					.next()
					.getValue();
				IJavaProject selectedJavaProjekt = selectedElements.get(0)
					.getJavaProject();
				StandardLoggerRule loggerRule = new StandardLoggerRule();

				if (null != selectedJavaProjekt) {
					loggerRule.calculateEnabledForProject(selectedJavaProjekt);
					if (loggerRule.isEnabled()) {

						if (SimonykeesMessageDialog.openConfirmDialog(HandlerUtil.getActiveShell(event),
								NLS.bind(Messages.LoggerRuleWizardHandler_info_supportedFrameworkFound,
										loggerRule.getAvailableLoggerType()))) {
							RefactoringPipeline refactoringPipeline = new RefactoringPipeline();

							Job job = new Job(Messages.ProgressMonitor_verifying_project_information) {

								@Override
								protected IStatus run(IProgressMonitor monitor) {

									try {
										List<ICompilationUnit> compilationUnits = new LinkedList<>();
										SelectRulesWizard.collectICompilationUnits(compilationUnits,
												selectedElements, monitor);
										List<ICompilationUnit> containingErrorList = refactoringPipeline
											.prepareRefactoring(compilationUnits, monitor);
										if (monitor.isCanceled()) {
											/*
											 * Workaround that prevents
											 * selection of multiple projects in
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
											synchronizeWithUIShowCompilationErrorMessage(containingErrorList, event,
													refactoringPipeline, selectedElements, loggerRule,
													selectedJavaProjekt);
										} else {
											synchronizeWithUIShowLoggerRuleWizard(event, refactoringPipeline,
													selectedElements, loggerRule, selectedJavaProjekt);
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
	 * Method used to open CompilationErrorsMessageDialog from non UI thread to
	 * list all Java files that will be skipped because they contain compilation
	 * errors.
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
						WizardMessageDialog.synchronizeWithUIShowWarningNoComlipationUnitDialog();
					}
				} else {
					Activator.setRunning(false);
				}
			});
	}

	private void synchronizeWithUIShowSelectionErrorMessage() {
		Display.getDefault()
			.syncExec(() -> {
				Shell shell = PlatformUI.getWorkbench()
					.getActiveWorkbenchWindow()
					.getShell();
				MessageDialog.openError(shell, Messages.LoggerRuleWizardHandler_multipleProjectsSelected,
						Messages.LoggerRuleWizardHandler_loggerRuleOnOneProjectOnly);

				Activator.setRunning(false);
			});
	}
}
