package eu.jsparrow.ui.util;

import java.lang.invoke.MethodHandles;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.jsparrow.i18n.Messages;
import eu.jsparrow.license.netlicensing.cleanslate.*;
import eu.jsparrow.license.netlicensing.cleanslate.exception.PersistenceException;
import eu.jsparrow.license.netlicensing.cleanslate.model.DemoLicenseModel;
import eu.jsparrow.license.netlicensing.cleanslate.model.LicenseModel;
import eu.jsparrow.ui.dialog.BuyLicenseDialog;
import eu.jsparrow.ui.dialog.SimonykeesMessageDialog;
import oshi.SystemInfo;
import oshi.hardware.HWDiskStore;
import oshi.hardware.HardwareAbstractionLayer;

public class NewLicenseUtil {

	private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup()
		.lookupClass());

	private static NewLicenseUtil instance;

	private LicenseService service;

	private LicenseValidationResult result = null;

	private NewLicenseUtil() {
		service = new NetlicensingLicenseService();
	}

	public static NewLicenseUtil get() {
		if (instance == null) {
			instance = new NewLicenseUtil();
		}
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
			model = service.loadFromPersistence();
		} catch (PersistenceException e) {
			handleStartUpPersistenceFailure(shell, e);
			model = new LicenseModelFactory().createDemoLicenseModel();
		}
		result = service.validateLicense(model);
		if (result.getModel() instanceof DemoLicenseModel && !result.getStatus()
			.isValid()) {
			BuyLicenseDialog dialog = new BuyLicenseDialog(shell, "Your free license has expired.");
			return dialog.open() == 0;
		}
		return true;
	}

	public boolean isFreeLicense() {
		LicenseModel model = tryLoadModelFromPersistence();
		result = service.validateLicense(model);
		return result.getModel() instanceof DemoLicenseModel;
	}

	public LicenseUpdateResult update(String key) {
		String secret = createSecretFromHardware();
		LicenseModel model = new LicenseModelFactory().createNewNodeLockedModel(key, secret);
		LicenseValidationResult validationResult = service.validateLicense(model);

		if (validationResult.getStatus()
			.isValid()) {
			try {
				service.saveToPersistence(validationResult.getModel());
			} catch (PersistenceException e) {
				String message = "License is valid but could not be persisted";
				logger.error(message, e);
				return new LicenseUpdateResult(false, message + ".\nPlease see the log for details.");
			}
		} else {
			String message = String.format("License with key '%s' is not valid. License not saved.", key);
			logger.warn(message);
			return new LicenseUpdateResult(false, message);
		}
		return new LicenseUpdateResult(true, Messages.SimonykeesUpdateLicenseDialog_license_updated_successfully);
	}

	public LicenseValidationResult getValidationResult() {
		LicenseModel model = null;
		try {
			model = service.loadFromPersistence();
		} catch (PersistenceException e) {
			logger.error("Error while loading stored license, using demo license", e);
			model = new LicenseModelFactory().createDemoLicenseModel();
		}
		result = service.validateLicense(model);
		return result;
	}

	private LicenseModel tryLoadModelFromPersistence() {
		LicenseModel model = null;
		try {
			model = service.loadFromPersistence();
		} catch (PersistenceException e) {
			logger.error("Error while loading stored license, using demo license", e);
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
