package eu.jsparrow.ui.util;

import java.lang.invoke.MethodHandles;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.jsparrow.i18n.Messages;
import eu.jsparrow.license.api.LicenseModel;
import eu.jsparrow.license.api.LicenseService;
import eu.jsparrow.license.api.LicenseType;
import eu.jsparrow.license.api.LicenseValidationResult;
import eu.jsparrow.license.api.exception.PersistenceException;
import eu.jsparrow.license.api.exception.ValidationException;
import eu.jsparrow.license.netlicensing.LicenseModelFactory;
import eu.jsparrow.license.netlicensing.model.DemoLicenseModel;
import eu.jsparrow.ui.dialog.BuyLicenseDialog;
import eu.jsparrow.ui.dialog.SimonykeesMessageDialog;
import oshi.SystemInfo;
import oshi.hardware.HWDiskStore;
import oshi.hardware.HardwareAbstractionLayer;

@Component
public class LicenseUtil {

	private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup()
		.lookupClass());

	@Reference(cardinality = ReferenceCardinality.MANDATORY)
	private LicenseService licenseService;

	private LicenseValidationResult result = null;

	private Scheduler scheduler;

	private static LicenseUtil instance;

	@Activate
	public void activate() {
		scheduler = new Scheduler();
		scheduler.start();
		instance = this;
	}

	public static LicenseUtil get() {
		return instance;
	}

	/**
	 * Performs a license check when running a wizard.
	 * 
	 * @param shell
	 *            shell to use for displaying messages
	 * @return true if client should continue, false if not
	 */
	public boolean checkAtStartUp(Shell shell) {
		LicenseModel model = null;
		try {
			model = licenseService.loadFromPersistence();
		} catch (PersistenceException e) {
			handleStartUpPersistenceFailure(shell, e);
			model = new LicenseModelFactory().createDemoLicenseModel();
		}
		try {
			result = licenseService.validate(model);
		} catch (ValidationException e) {
			handleStartUpValidationFailure(shell, e);
			return true;
		}
		if (result.getModel().getType() == LicenseType.DEMO && !result.isValid()) {
			BuyLicenseDialog dialog = new BuyLicenseDialog(shell, "Your free license has expired.");
			return dialog.open() == 0;
		}
		return true;
	}

	public boolean isFreeLicense() {
		if (result == null) {
			return true;
		}
		return result.getModel().getType() == LicenseType.DEMO;
	}

	public LicenseUpdateResult update(String key) {
		String secret = createSecretFromHardware();
		LicenseModel model = new LicenseModelFactory().createNewFloatingModel(key, secret);
		LicenseValidationResult validationResult = null;
		try {
			validationResult = licenseService.validate(model);
		} catch (ValidationException e) {
			logger.error("Could not validate license", e);
			return new LicenseUpdateResult(false, "Could not validate license.\n" + e.getMessage());
		}

		if (!validationResult.isValid()) {
			String message = String.format("License with key '%s' is not valid. License not saved.", key);
			logger.warn(message);
			return new LicenseUpdateResult(false, message);

		}
		return trySaveToPersistence(validationResult);
	}

	public void stop() {
		LicenseModel model = tryLoadModelFromPersistence();
		try {
			licenseService.checkIn(model);
		} catch (ValidationException e) {
			logger.error("Failed to check in license.", e);
		}
		scheduler.shutDown();
	}

	public LicenseValidationResult getValidationResult() {
		LicenseModel model = tryLoadModelFromPersistence();
		try {
			result = licenseService.validate(model);
		} catch (ValidationException e) {
			logger.error("Failed to validate license", e);
		}
		return result;
	}

	private LicenseModel tryLoadModelFromPersistence() {
		LicenseModel model = null;
		try {
			model = licenseService.loadFromPersistence();
		} catch (PersistenceException e) {
			logger.error("Error while loading stored license, using default demo license", e);
			model = new LicenseModelFactory().createDemoLicenseModel();
		}
		return model;
	}

	private void handleStartUpPersistenceFailure(Shell shell, PersistenceException e) {
		String partMessage = "Failed to load stored license. Falling back to free license";
		logger.error(partMessage, e);
		String message = partMessage + "\nPlease view the jSparrow logs for more information.";
		SimonykeesMessageDialog.openMessageDialog(shell, message, MessageDialog.ERROR);
	}

	private void handleStartUpValidationFailure(Shell shell, ValidationException e) {
		String partMessage = "Failed to validate license. " + e.getMessage();
		logger.error(partMessage, e);
		String message = partMessage + "\nPlease view the jSparrow logs for more information.";
		SimonykeesMessageDialog.openMessageDialog(shell, message, MessageDialog.ERROR);
	}

	private LicenseUpdateResult trySaveToPersistence(LicenseValidationResult validationResult) {
		try {
			licenseService.saveToPersistence(validationResult.getModel());
		} catch (PersistenceException e) {
			String message = "License is valid but could not be persisted";
			logger.error(message, e);
			return new LicenseUpdateResult(false, message + ".\nPlease see the log for details.");
		}

		return new LicenseUpdateResult(true, Messages.SimonykeesUpdateLicenseDialog_license_updated_successfully);
	}

	private String createSecretFromHardware() {

		String diskSerial = ""; //$NON-NLS-1$
		SystemInfo systemInfo = new SystemInfo();

		HardwareAbstractionLayer hal = systemInfo.getHardware();
		HWDiskStore[] diskStores = hal.getDiskStores();

		if (diskStores.length > 0) {
			diskSerial = diskStores[0].getSerial();
		}

		return diskSerial;
	}

	public class LicenseUpdateResult {

		private boolean wasSuccessful;

		private String detailMessage;

		public LicenseUpdateResult(boolean successful, String message) {
			wasSuccessful = successful;
			detailMessage = message;
		}

		public String getDetailMessage() {
			return detailMessage;
		}

		public boolean wasSuccessful() {
			return wasSuccessful;
		}

	}
}
