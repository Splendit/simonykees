package at.splendit.simonykees.core.handler;

import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.IJavaElement;
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

import at.splendit.simonykees.core.Activator;
import at.splendit.simonykees.core.exception.SimonykeesException;
import at.splendit.simonykees.core.ui.LicenseUtil;
import at.splendit.simonykees.core.ui.dialog.SimonykeesMessageDialog;
import at.splendit.simonykees.core.ui.wizard.semiautomatic.RenameFieldsRuleWizard;
import at.splendit.simonykees.core.util.WizardHandlerUtil;
import at.splendit.simonykees.i18n.Messages;

/**
 * Handler for semi-automatic rename public fields rule
 * 
 * @author Andreja Sambolec
 * @since 2.1
 *
 */
public class RenameFieldsRuleWizardHandler extends AbstractHandler {

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

					Job job = new Job(Messages.ProgressMonitor_SelectRulesWizard_performFinish_jobName) {

						@Override
						protected IStatus run(IProgressMonitor monitor) {

							synchronizeWithUIShowRenameFieldsRuleWizard(selectedJavaElements);
							return Status.OK_STATUS;
						}
					};

					job.setUser(true);
					job.schedule();

					return true;
				} else {
					synchronizeWithUIShowWarningNoComlipationUnitDialog();
				}
			} else {
				/*
				 * do not display the Wizard if the license is invalid
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
	private void synchronizeWithUIShowRenameFieldsRuleWizard(List<IJavaElement> selectedJavaElements) {
		Display.getDefault().asyncExec(new Runnable() {

			@Override
			public void run() {
				Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
				final WizardDialog dialog = new WizardDialog(shell,
						new RenameFieldsRuleWizard(selectedJavaElements)) {
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
}
