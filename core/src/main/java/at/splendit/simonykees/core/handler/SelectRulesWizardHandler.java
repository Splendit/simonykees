package at.splendit.simonykees.core.handler;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;

import at.splendit.simonykees.core.Activator;
import at.splendit.simonykees.core.ui.LicenseUtil;
import at.splendit.simonykees.core.ui.SelectRulesWizard;
import at.splendit.simonykees.core.ui.dialog.SimonykeesMessageDialog;

/**
 * TODO SIM-103 add class description
 * 
 * @author Hannes Schweighofer, Ludwig Werzowa
 * @since 0.9
 */
public class SelectRulesWizardHandler extends AbstractSimonykeesHandler {


	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {

		if (Activator.isRunning()) {
			SimonykeesMessageDialog.openMessageDialog(Display.getDefault().getActiveShell(), "Allready running",
					MessageDialog.INFORMATION);
		} else {
			Activator.setRunning(true);
			if (LicenseUtil.isValid()) {
				final WizardDialog dialog = new WizardDialog(HandlerUtil.getActiveShell(event),
						new SelectRulesWizard(getSelectedJavaElements(event)));

				/*
				 * the dialog is made as smaller than necessary horizontally (we
				 * want line breaks for rule descriptions)
				 */
				dialog.setPageSize(750, 500);

				dialog.open();
			} else {
				// do not display the SelectRulesWizard if the license is
				// invalid
				final Shell shell = HandlerUtil.getActiveShell(event);
				LicenseUtil.displayLicenseErrorDialog(shell);
			}
		}

		return null;
	}

}
