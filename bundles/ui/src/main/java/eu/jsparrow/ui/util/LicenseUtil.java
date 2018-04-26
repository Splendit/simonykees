package eu.jsparrow.ui.util;

import java.lang.invoke.MethodHandles;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Shell;
import org.osgi.framework.ServiceReference;
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
import eu.jsparrow.ui.Activator;
import eu.jsparrow.ui.dialog.BuyLicenseDialog;
import eu.jsparrow.ui.dialog.SimonykeesMessageDialog;
import oshi.SystemInfo;
import oshi.hardware.HWDiskStore;
import oshi.hardware.HardwareAbstractionLayer;

/**
 * Implements {@link LicenseUtilService}. The purpose of this class is to wrap
 * some license management functions for the UI. For example, the flow for
 * updating a license.
 * 
 * It uses various services from the License API package.
 */
public class LicenseUtil implements LicenseUtilService {

	private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup()
		.lookupClass());

	private static LicenseUtil instance;

	private LicenseService licenseService;

	private LicensePersistenceService persistenceService;

	private LicenseModelFactoryService factoryService;

	private LicenseValidationResult result = null;

	private Scheduler scheduler;

	private LicenseUtil() {
		scheduler = new Scheduler(this);
		scheduler.start();
		ServiceReference<LicenseService> licenseReference = Activator.getBundleContext()
			.getServiceReference(LicenseService.class);
		licenseService = Activator.getBundleContext()
			.getService(licenseReference);
		ServiceReference<LicensePersistenceService> persistenceReference = Activator.getBundleContext()
			.getServiceReference(LicensePersistenceService.class);
		persistenceService = Activator.getBundleContext()
			.getService(persistenceReference);
		ServiceReference<LicenseModelFactoryService> factoryReference = Activator.getBundleContext()
			.getServiceReference(LicenseModelFactoryService.class);
		factoryService = Activator.getBundleContext()
			.getService(factoryReference);
	}

	public static LicenseUtil get() {
		if (instance == null) {
			instance = new LicenseUtil();
		}
		return instance;
	}

	@Override
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
		// When starting with an expired demo license we show the wizard dialog
		if (result.getLicenseType() == LicenseType.DEMO && !result.isValid()) {
			BuyLicenseDialog dialog = new BuyLicenseDialog(shell);
			return dialog.open() == 0;
		}
		return true;
	}

	@Override
	public boolean isFreeLicense() {
		if (result == null || !result.isValid()) {
			return true;
		}
		return result.getLicenseType() == LicenseType.DEMO;
	}

	@Override
	public LicenseUpdateResult update(String key) {
		String secret = createSecretFromHardware();
		LicenseValidationResult validationResult;
		LicenseModel model;
		try {
			validationResult = licenseService.verifyKey(key, secret);
			String name = createNameFromHardware();
			/*
			 * The validation result can only be trusted when the validation
			 * request is based on a license model. The verify step is only used
			 * for finding out the license model.
			 */
			model = factoryService.createNewModel(key, secret, validationResult.getLicenseType(), name,
					validationResult.getExpirationDate());
			validationResult = licenseService.validate(model);
		} catch (ValidationException e) {
			logger.error("Could not validate license", e); //$NON-NLS-1$
			return new LicenseUpdateResult(false,
					NLS.bind(Messages.UpdateLicenseDialog_error_couldNotValidate, e.getMessage()));
		}

		if (!validationResult.isValid()) {
			logger.warn("License with key '{}' is not valid. License not saved.", key); //$NON-NLS-1$
			return new LicenseUpdateResult(false, NLS.bind(Messages.UpdateLicenseDialog_error_licenseInvalid, key));

		}

		return trySaveToPersistence(model);
	}

	@Override
	public void stop() {
		LicenseModel model = tryLoadModelFromPersistence();
		try {
			licenseService.checkIn(model);
		} catch (ValidationException e) {
			logger.error("Failed to check in license.", e); //$NON-NLS-1$
		}
		scheduler.shutDown();
	}

	@Override
	public LicenseValidationResult getValidationResult() {
		updateValidationResult();
		return result;
	}

	public void updateValidationResult() {
		LicenseModel model = tryLoadModelFromPersistence();
		try {
			result = licenseService.validate(model);
		} catch (ValidationException e) {
			logger.error("Failed to validate license", e); //$NON-NLS-1$
			result = new LicenseValidationResult(model.getType(), "", false, //$NON-NLS-1$
					Messages.MessageDialog_licensingError_failedToValidate, model.getExpirationDate());
		}
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

	private LicenseUpdateResult trySaveToPersistence(LicenseModel model) {
		try {
			persistenceService.saveToPersistence(model);
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

	private String createNameFromHardware() {
		InetAddress addr;
		try {
			addr = InetAddress.getLocalHost();
			return addr.getHostName();
		} catch (UnknownHostException e) {
			logger.warn("Error while reading the host name", e); //$NON-NLS-1$
			return ""; //$NON-NLS-1$
		}
	}

	/**
	 * This is a helper class. Only used to transport the result of an update
	 * license and a detailed message if necessary.
	 */
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
