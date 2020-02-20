package eu.jsparrow.ui.wizard.impl;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import eu.jsparrow.i18n.Messages;
import eu.jsparrow.rules.common.exception.SimonykeesException;
import eu.jsparrow.ui.Activator;
import eu.jsparrow.ui.dialog.SimonykeesMessageDialog;

public class WizardMessageDialog {

	private WizardMessageDialog() {
	}

	/**
	 * Method used to open MessageDialog informing the user that no refactorings
	 * are required from non UI thread
	 */
	public static void synchronizeWithUIShowWarningNoRefactoringDialog() {
		Display.getDefault()
			.asyncExec(() -> {
				Shell shell = PlatformUI.getWorkbench()
					.getActiveWorkbenchWindow()
					.getShell();
				SimonykeesMessageDialog.openMessageDialog(shell, Messages.SelectRulesWizard_warning_no_refactorings,
						MessageDialog.INFORMATION);

				Activator.setRunning(false);
			});
	}

	/**
	 * Method used to open ErrorDialog from non UI thread
	 */
	public static void synchronizeWithUIShowError(SimonykeesException exception) {
		Display.getDefault()
			.asyncExec(() -> {
				Shell shell = PlatformUI.getWorkbench()
					.getActiveWorkbenchWindow()
					.getShell();
				SimonykeesMessageDialog.openErrorMessageDialog(shell, exception);

				Activator.setRunning(false);
			});
	}

	/**
	 * Method used to open InformationDialog from non UI thread
	 * RefactoringException is thrown if java element does not exist or if an
	 * exception occurs while accessing its corresponding resource, or if no
	 * working copies were found to apply
	 */
	public static void synchronizeWithUIShowInfo(SimonykeesException exception) {
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
	 * Method used to open MessageDialog informing the user that selection
	 * contains no Java files without compilation error from non UI thread
	 */
	public static void synchronizeWithUIShowWarningNoComlipationUnitWithoutErrorsDialog() {
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

	public static void synchronizeWithUIShowMultiprojectMessage() {
		Display.getDefault()
			.asyncExec(() -> {
				Shell shell = PlatformUI.getWorkbench()
					.getActiveWorkbenchWindow()
					.getShell();
				SimonykeesMessageDialog.openMessageDialog(shell,
						Messages.SelectRulesWizardHandler_multipleProjectsWarning, MessageDialog.WARNING);

				Activator.setRunning(false);
			});
	}

	public static void synchronizedWithUIShowWarningNoCompilationUnitDialog() {
		Display.getDefault()
			.asyncExec(() -> {
				Shell shell = PlatformUI.getWorkbench()
					.getActiveWorkbenchWindow()
					.getShell();
				SimonykeesMessageDialog.openMessageDialog(shell,
						Messages.WizardMessageDialog_selectionDidNotContainAnyJavaFiles,
						MessageDialog.WARNING);

				Activator.setRunning(false);
			});
	}
}
