package eu.jsparrow.ui.util;

import java.lang.invoke.MethodHandles;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Shell;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.jsparrow.i18n.Messages;
import eu.jsparrow.license.api.LicenseModel;
import eu.jsparrow.license.api.LicenseModelFactoryService;
import eu.jsparrow.license.api.LicensePersistenceService;
import eu.jsparrow.license.api.LicenseService;
import eu.jsparrow.license.api.LicenseType;
import eu.jsparrow.license.api.LicenseValidationResult;
import eu.jsparrow.license.api.exception.PersistenceException;
import eu.jsparrow.license.api.exception.ValidationException;
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
	
	@Reference(cardinality = ReferenceCardinality.MANDATORY)
	private LicensePersistenceService persistenceService;
	
	@Reference(cardinality = ReferenceCardinality.MANDATORY)
	private LicenseModelFactoryService factoryService;

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
			model = persistenceService.loadFromPersistence();
		} catch (PersistenceException e) {
			handleStartUpPersistenceFailure(shell, e);
			model = factoryService.createDemoLicenseModel();
		}
		try {
			result = licenseService.validate(model);
		} catch (ValidationException e) {
			handleStartUpValidationFailure(shell, e);
			return true;
		}
		if (result.getLicenseType() == LicenseType.DEMO && !result.isValid()) {
			BuyLicenseDialog dialog = new BuyLicenseDialog(shell);
			return dialog.open() == 0;
		}
		return true;
	}

	public boolean isFreeLicense() {
		if (result == null) {
			return true;
		}
		return result.getLicenseType() == LicenseType.DEMO;
	}

	public LicenseUpdateResult update(String key) {
		String secret = createSecretFromHardware();
		LicenseModel model = factoryService.createNewFloatingModel(key, secret);
		LicenseValidationResult validationResult = null;
		try {
			validationResult = licenseService.validate(model);
		} catch (ValidationException e) {
			logger.error("Could not validate license", e); //$NON-NLS-1$
			return new LicenseUpdateResult(false, NLS.bind(Messages.UpdateLicenseDialog_error_couldNotValidate, e.getMessage()));
		}

		if (!validationResult.isValid()) {
			logger.warn("License with key '{}' is not valid. License not saved.", key); //$NON-NLS-1$
			return new LicenseUpdateResult(false, NLS.bind(Messages.UpdateLicenseDialog_error_licenseInvalid, key));

		}
		return trySaveToPersistence(validationResult);
	}

	public void stop() {
		LicenseModel model = tryLoadModelFromPersistence();
		try {
			licenseService.checkIn(model);
		} catch (ValidationException e) {
			logger.error("Failed to check in license.", e); //$NON-NLS-1$
		}
		scheduler.shutDown();
	}

	public LicenseValidationResult getValidationResult() {
		LicenseModel model = tryLoadModelFromPersistence();
		try {
			result = licenseService.validate(model);
		} catch (ValidationException e) {
			logger.error("Failed to validate license", e); //$NON-NLS-1$
		}
		return result;
	}

	private LicenseModel tryLoadModelFromPersistence() {
		LicenseModel model = null;
		try {
			model = persistenceService.loadFromPersistence();
		} catch (PersistenceException e) {
			logger.warn("Error while loading stored license, using default demo license", e); //$NON-NLS-1$
			model = factoryService.createDemoLicenseModel();
		}
		return model;
	}

	private void handleStartUpPersistenceFailure(Shell shell, PersistenceException e) {
		logger.error("Failed to load stored license. Falling back to free license.", e); //$NON-NLS-1$
		String message = Messages.MessageDialog_licensingError_failedToLoad;
		SimonykeesMessageDialog.openMessageDialog(shell, message, MessageDialog.ERROR);
	}

	private void handleStartUpValidationFailure(Shell shell, ValidationException e) {
		logger.error("Failed to validate license. ", e); //$NON-NLS-1$
		String message = Messages.MessageDialog_licensingError_failedToValidate;
		SimonykeesMessageDialog.openMessageDialog(shell, message, MessageDialog.ERROR);
	}

	private LicenseUpdateResult trySaveToPersistence(LicenseValidationResult validationResult) {
		try {
			persistenceService.saveToPersistence(validationResult);
		} catch (PersistenceException e) {
			logger.error("License is valid but could not be persisted", e); //$NON-NLS-1$
			return new LicenseUpdateResult(false,
					NLS.bind(Messages.UpdateLicenseDialog_error_couldNotSave, e.getMessage()));
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
