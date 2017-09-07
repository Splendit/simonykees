package eu.jsparrow.core.handler;

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
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.ProgressMonitorPart;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

import eu.jsparrow.core.Activator;
import eu.jsparrow.core.exception.RefactoringException;
import eu.jsparrow.core.exception.SimonykeesException;
import eu.jsparrow.core.refactorer.RefactoringPipeline;
import eu.jsparrow.core.rule.RulesContainer;
import eu.jsparrow.core.ui.LicenseUtil;
import eu.jsparrow.core.ui.dialog.CompilationErrorsMessageDialog;
import eu.jsparrow.core.ui.dialog.SimonykeesMessageDialog;
import eu.jsparrow.core.ui.wizard.impl.SelectRulesWizard;
import eu.jsparrow.core.util.WizardHandlerUtil;
import eu.jsparrow.i18n.Messages;

/**
 * TODO SIM-103 add class description
 * 
 * @author Hannes Schweighofer, Ludwig Werzowa, Andreja Sambolec
 * @since 0.9
 */
public class SelectRulesWizardHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {

		if (Activator.isRunning()) {
			SimonykeesMessageDialog.openMessageDialog(Display.getDefault().getActiveShell(),
					Messages.SelectRulesWizardHandler_allready_running, MessageDialog.INFORMATION);
		} else {
			Activator.setRunning(true);
			if (LicenseUtil.getInstance().isValid()) {
				List<IJavaElement> selectedJavaElements = WizardHandlerUtil.getSelectedJavaElements(event);
				if (!selectedJavaElements.isEmpty()) {
					IJavaProject selectedJavaProjekt = selectedJavaElements.get(0).getJavaProject();

					if (null != selectedJavaProjekt) {

						RefactoringPipeline refactoringPipeline = new RefactoringPipeline();

						Job job = new Job(Messages.ProgressMonitor_SelectRulesWizard_performFinish_jobName) {

							@Override
							protected IStatus run(IProgressMonitor monitor) {

								try {
									List<ICompilationUnit> containingErrorList = refactoringPipeline
											.prepareRefactoring(selectedJavaElements, monitor);
									if (monitor.isCanceled()) {
										/*
										 * Workaround that prevents selection of
										 * multiple projects in the Package
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
												refactoringPipeline, selectedJavaElements, selectedJavaProjekt);
									} else {
										synchronizeWithUIShowSelectRulesWizard(event, refactoringPipeline,
												selectedJavaElements, selectedJavaProjekt);
									}

								} catch (RefactoringException e) {
									synchronizeWithUIShowInfo(e);
									return Status.CANCEL_STATUS;
								}

								return Status.OK_STATUS;
							}
						};

						job.setUser(true);
						job.schedule();

						return true;

					}
				}
			} else {
				/*
				 * do not display the SelectRulesWizard if the license is
				 * invalid
				 */
				final Shell shell = HandlerUtil.getActiveShell(event);
				LicenseUtil.getInstance().displayLicenseErrorDialog(shell);
				Activator.setRunning(false);
			}
		}

		return null;
	}

	/**
	 * Method used to open SelectRulesWizard from non UI thread
	 */
	private void synchronizeWithUIShowSelectRulesWizard(ExecutionEvent event, RefactoringPipeline refactoringPipeline,
			List<IJavaElement> selectedJavaElements, IJavaProject selectedJavaProjekt) {
		Display.getDefault().asyncExec(new Runnable() {

			@Override
			public void run() {
				Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
				// HandlerUtil.getActiveShell(event)
				final WizardDialog dialog = new WizardDialog(shell, new SelectRulesWizard(selectedJavaElements,
						refactoringPipeline, RulesContainer.getRulesForProject(selectedJavaProjekt))) {
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
				};
				/*
				 * the dialog is made as big enough to show rule description
				 * vertically and horizontally to avoid two scrollers
				 * 
				 * note: if the size is too big, it will be reduced to the
				 * maximum possible size.
				 */
				dialog.setPageSize(800, 700);

				dialog.open();
			}
		});
	}

	/**
	 * Method used to open CompilationErrorsMessageDialog from non UI thread to
	 * list all Java files that will be skipped because they contain compilation
	 * errors.
	 */
	private void synchronizeWithUIShowCompilationErrorMessage(List<ICompilationUnit> containingErrorList,
			ExecutionEvent event, RefactoringPipeline refactoringPipeline, List<IJavaElement> selectedJavaElements,
			IJavaProject selectedJavaProjekt) {
		Display.getDefault().asyncExec(new Runnable() {

			@Override
			public void run() {
				Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
				CompilationErrorsMessageDialog dialog = new CompilationErrorsMessageDialog(shell);
				dialog.create();
				dialog.setTableViewerInput(containingErrorList);
				dialog.open();
				if (dialog.getReturnCode() == IDialogConstants.OK_ID) {
					if (refactoringPipeline.hasRefactoringStates()) {
						synchronizeWithUIShowSelectRulesWizard(event, refactoringPipeline, selectedJavaElements,
								selectedJavaProjekt);
					} else {
						synchronizeWithUIShowWarningNoComlipationUnitDialog();
					}
				} else {
					Activator.setRunning(false);
				}
			}
		});
	}

	/**
	 * Method used to open InformationDialog from non UI thread
	 * RefactoringException is thrown if java element does not exist or if an
	 * exception occurs while accessing its corresponding resource, or if no
	 * working copies were found to apply
	 */
	private void synchronizeWithUIShowInfo(SimonykeesException exception) {
		Display.getDefault().asyncExec(new Runnable() {

			@Override
			public void run() {
				Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
				SimonykeesMessageDialog.openMessageDialog(shell, exception.getUiMessage(), MessageDialog.INFORMATION);

				Activator.setRunning(false);
			}
		});
	}

	/**
	 * Method used to open MessageDialog informing the user that selection
	 * contains no Java files without compilation error from non UI thread
	 */
	private void synchronizeWithUIShowWarningNoComlipationUnitDialog() {
		Display.getDefault().asyncExec(new Runnable() {

			@Override
			public void run() {
				Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
				SimonykeesMessageDialog.openMessageDialog(shell, Messages.SelectRulesWizardHandler_noFileWithoutError,
						MessageDialog.INFORMATION);

				Activator.setRunning(false);
			}
		});
	}

	private void synchronizeWithUIShowMultiprojectMessage() {
		Display.getDefault().asyncExec(new Runnable() {

			@Override
			public void run() {
				Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
				SimonykeesMessageDialog.openMessageDialog(shell,
						Messages.SelectRulesWizardHandler_multipleProjectsWarning, MessageDialog.WARNING);
			}
		});
	}
}
