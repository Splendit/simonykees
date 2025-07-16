package eu.jsparrow.ui.util;

import org.eclipse.swt.widgets.Shell;

import eu.jsparrow.license.api.LicenseValidationResult;

/**
 * Implementors of this class provide functions for managing licenses. This is a
 * helper interface, implementors are only supposed to be used in the UI.
 */
public interface LicenseUtilService {
	/**
	 * Performs a license check when running a wizard.
	 * 
	 * @param shell
	 *            shell to use for displaying messages
	 * @return true if client should continue, false if not
	 */
	boolean checkAtStartUp(Shell shell);

	/**
	 * Check if the license type of the currently used license is free.
	 * 
	 * @return {@code true} if the license is a free license, {@code false}
	 *         otherwise
	 */
	boolean isFreeLicense();

	/**
	 * Update the stored license using the specified key.
	 * 
	 * @param key
	 *            the key of the license to update to
	 * @return a {@link LicenseUpdateResult} that specifies if the license was
	 *         updated or not
	 */
	LicenseUpdateResult update(String key);

	/**
	 * Stop the {@link Scheduler} that runs periodic license checks.
	 */
	void stop();

	/**
	 * Return the latest {@link LicenseValidationResult}.
	 * 
	 * @return the validation result
	 */
	LicenseValidationResult getValidationResult();
	
	void reserveQuantity(int credit);
	
	void updateValidationResult();
	
	boolean isProLicense();
}
