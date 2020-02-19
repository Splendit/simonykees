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
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
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
import eu.jsparrow.rules.common.exception.RefactoringException;
import eu.jsparrow.ui.Activator;
import eu.jsparrow.ui.dialog.CompilationErrorsMessageDialog;
import eu.jsparrow.ui.dialog.SimonykeesMessageDialog;
import eu.jsparrow.ui.util.LicenseUtil;
import eu.jsparrow.ui.util.LicenseUtilService;
import eu.jsparrow.ui.util.WizardHandlerUtil;
import eu.jsparrow.ui.wizard.impl.SelectRulesWizard;
import eu.jsparrow.ui.wizard.impl.WizardMessageDialog;

/**
 * Wizard handler called when Select rules is selected from context menu under
 * jSparrow
 * 
 * @author Hannes Schweighofer, Ludwig Werzowa, Andreja Sambolec, Matthias
 *         Webhofer
 * @since 0.9
 */
public class SelectRulesWizardHandler extends AbstractHandler {

	private static final Logger logger = LoggerFactory.getLogger(SelectRulesWizard.class);

	private LicenseUtilService licenseUtil = LicenseUtil.get();

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

				Job job = new Job(Messages.ProgressMonitor_verifying_project_information) {

					@Override
					protected IStatus run(IProgressMonitor monitor) {
						RefactoringPipeline refactoringPipeline = new RefactoringPipeline();

						try {
							List<ICompilationUnit> compilationUnits = new LinkedList<>();
							for (Map.Entry<IJavaProject, List<IJavaElement>> entry : selectedJavaElements.entrySet()) {
								SelectRulesWizard.collectICompilationUnits(compilationUnits, entry.getValue(),
										monitor);
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
								synchronizeWithUIShowSelectRulesWizard(refactoringPipeline, selectedJavaElements);
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
				// SIM-656
				logger.error(Messages.SelectRulesWizardHandler_selectionNotPossible_ubuntuBug);
				Activator.setRunning(false);
			}

		}

		return null;
	}

	/**
	 * Method used to open SelectRulesWizard from non UI thread
	 */
	private void synchronizeWithUIShowSelectRulesWizard(RefactoringPipeline refactoringPipeline,
			Map<IJavaProject, List<IJavaElement>> selectedJavaElements) {

		Display.getDefault()
			.asyncExec(() -> {
				Shell shell = PlatformUI.getWorkbench()
					.getActiveWorkbenchWindow()
					.getShell();
				final WizardDialog dialog = new WizardDialog(shell, new SelectRulesWizard(selectedJavaElements.keySet(),
						refactoringPipeline,
						RulesContainer.getRulesForProjects(selectedJavaElements.keySet(), false))) {
					/*
					 * Removed unnecessary empty space on the bottom of the
					 * wizard intended for ProgressMonitor that is not used
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

					/**
					 * Creates new shell defined for this wizard. The dialog is
					 * made as big enough to show rule description vertically
					 * and horizontally to avoid two scrollers. Minimum size is
					 * set to avoid loosing components from view.
					 * 
					 * @param newShell
					 */
					@Override
					protected void configureShell(Shell newShell) {
						super.configureShell(newShell);
						newShell.setSize(1000, 1000);
						newShell.setMinimumSize(680, 600);
					}

					@Override
					protected void createButtonsForButtonBar(Composite parent) {
						super.createButtonsForButtonBar(parent);

						Button finish = getButton(IDialogConstants.FINISH_ID);
						finish.setText(Messages.SelectRulesWizardHandler_finishButtonText);
						setButtonLayoutData(finish);
					}
				};
				/*
				 * Creates new shell and wizard.
				 */
				dialog.create();
				dialog.open();
			});

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
						synchronizeWithUIShowSelectRulesWizard(refactoringPipeline, selectedJavaElements);
					} else {
						WizardMessageDialog.synchronizeWithUIShowWarningNoComlipationUnitDialog();
					}
				} else {
					Activator.setRunning(false);
				}
			});
	}
}
