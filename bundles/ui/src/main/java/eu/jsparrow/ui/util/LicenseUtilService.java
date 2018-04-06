package eu.jsparrow.ui.util;

import org.eclipse.swt.widgets.Shell;

import eu.jsparrow.license.api.LicenseValidationResult;
import eu.jsparrow.ui.util.LicenseUtil.LicenseUpdateResult;

public interface LicenseUtilService {
	/**
	 * Performs a license check when running a wizard.
	 * 
	 * @param shell
	 *            shell to use for displaying messages
	 * @return true if client should continue, false if not
	 */
	boolean checkAtStartUp(Shell shell);

	boolean isFreeLicense();

	LicenseUpdateResult update(String key);

	void stop();

	LicenseValidationResult getValidationResult();
}
