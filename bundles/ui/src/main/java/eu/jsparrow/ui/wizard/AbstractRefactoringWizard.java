package eu.jsparrow.ui.wizard;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.widgets.Shell;

import eu.jsparrow.ui.Activator;

@SuppressWarnings("nls")
public abstract class AbstractRefactoringWizard extends Wizard {

	private static final String CANCEL_DIALOG_MESSAGE = "Cancelling stops the complete refactoring process!";
	private static final String CANCEL_DIALOG_TITLE = "Confirm Cancel";
	private static final String[] CANCEL_DIALOG_BUTTONS = { "Stop jSparrow", "Continue jSparrow" };

	@Override
	public boolean performCancel() {

		int answer = new CancelDialog(getShell()).open();
		if (answer == 1) {
			return false;
		}
		Activator.setRunning(false);
		return true;
	}

	static class CancelDialog extends MessageDialog {

		CancelDialog(Shell parentShell) {
			super(parentShell, CANCEL_DIALOG_TITLE, null, CANCEL_DIALOG_MESSAGE, MessageDialog.WARNING, -1,
					CANCEL_DIALOG_BUTTONS);
		}

		@Override
		protected boolean canHandleShellCloseEvent() {
			return false;
		}
	}
}
