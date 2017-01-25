package at.splendit.simonykees.core.ui;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Shell;

import at.splendit.simonykees.core.i18n.Messages;
import at.splendit.simonykees.core.license.LicenseChecker;
import at.splendit.simonykees.core.license.LicenseManager;

/**
 * GUI related convenience class to check the validity of the license and
 * display appropriate popups if not.
 * 
 * @author Ludwig Werzowa
 * @since 1.0
 */
public class LicenseHelper {

	public static boolean isValid() {
		return LicenseManager.getInstance().getValidationData().isValid();
	}

	public static void displayLicenseErrorDialog(Shell shell) {
		LicenseChecker licenseChecker = LicenseManager.getInstance().getValidationData();
		String userMessage = licenseChecker.getLicenseStatus().getUserMessage();

		MessageDialog dialog = new MessageDialog(shell, Messages.aa_codename, null,
				NLS.bind(Messages.LicenseHelper_licenseProblem, userMessage), MessageDialog.ERROR, 1, Messages.ui_ok);
		dialog.open();
	}

}
