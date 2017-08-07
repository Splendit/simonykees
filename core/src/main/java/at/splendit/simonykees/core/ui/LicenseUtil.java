package at.splendit.simonykees.core.ui;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.splendit.simonykees.core.Activator;
import at.splendit.simonykees.core.ui.dialog.SimonykeesMessageDialog;
import at.splendit.simonykees.i18n.ExceptionMessages;
import at.splendit.simonykees.license.api.LicenseValidationService;

/**
 * GUI related convenience class to check the validity of the license and
 * display appropriate popups if not.
 * 
 * @author Ludwig Werzowa, Andreja Sambolec, Matthias Webhofer
 * @since 1.0
 */
public class LicenseUtil {
	private static final Logger logger = LoggerFactory.getLogger(LicenseUtil.class);

	private static LicenseUtil instance;

	@Inject
	private LicenseValidationService licenseValidationService;
	private boolean isLicenseValidationServiceAvailable = false;

	private LicenseUtil() {
		ContextInjectionFactory.inject(this, Activator.getEclipseContext());
	}

	public static LicenseUtil getInstance() {
		if (instance == null) {
			instance = new LicenseUtil();
		}
		return instance;
	}

	@PostConstruct
	private void postConstruct() {
		if (licenseValidationService != null)
			isLicenseValidationServiceAvailable = true;
	}

	@PreDestroy
	private void preDestroy() {
		isLicenseValidationServiceAvailable = false;
	}

	public boolean isValid() {
		if (isLicenseValidationServiceAvailable)
			return licenseValidationService.isValid();
		return false;
	}

	public void displayLicenseErrorDialog(Shell shell) {

		if (isLicenseValidationServiceAvailable) {
			String userMessage = licenseValidationService.getLicenseStautsUserMessage();

			SimonykeesMessageDialog.openMessageDialog(shell, userMessage, MessageDialog.ERROR);
		} else {
			// TODO: proper error handling
			logger.error(ExceptionMessages.LicenseUtil_license_service_unavailable);
		}
	}

}
