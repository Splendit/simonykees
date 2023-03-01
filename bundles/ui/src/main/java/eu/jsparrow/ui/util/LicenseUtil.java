package eu.jsparrow.ui.util;

import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.Properties;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Shell;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
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
import eu.jsparrow.ui.dialog.SimonykeesMessageDialog;

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

	private LicensePersistenceService<LicenseModel> persistenceService;
	private LicensePersistenceService<String> endpointPersistenceService;

	private LicenseModelFactoryService factoryService;

	private LicenseValidationResult result = null;

	private Scheduler scheduler;
	private SystemInfoWrapper systemInfoWrapper;

	private EndpointEncryption endpointEncryption;

	private LicenseUtil() {

		scheduler = new Scheduler(this);
		systemInfoWrapper = new SystemInfoWrapper();
		scheduler.start();

		endpointEncryption = new EndpointEncryption();

		BundleContext bundleContext = FrameworkUtil.getBundle(getClass())
			.getBundleContext();

		ServiceReference<LicenseService> licenseReference = bundleContext.getServiceReference(LicenseService.class);
		licenseService = bundleContext.getService(licenseReference);

		initPersistenceServices(bundleContext);

		ServiceReference<LicenseModelFactoryService> factoryReference = bundleContext
			.getServiceReference(LicenseModelFactoryService.class);
		factoryService = bundleContext.getService(factoryReference);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void initPersistenceServices(BundleContext bundleContext) {
		try {
			ServiceReference[] netlicensingReferences = bundleContext
				.getServiceReferences(LicensePersistenceService.class.getName(), "(licenseType=default)"); //$NON-NLS-1$
			ServiceReference[] endpointReferences = bundleContext
				.getServiceReferences(LicensePersistenceService.class.getName(), "(licenseType=endpoint)"); //$NON-NLS-1$

			if (netlicensingReferences.length != 0) {
				this.persistenceService = (LicensePersistenceService<LicenseModel>) bundleContext
					.getService(netlicensingReferences[0]);
			}

			if (endpointReferences.length != 0) {
				this.endpointPersistenceService = (LicensePersistenceService<String>) bundleContext
					.getService(endpointReferences[0]);
			}

		} catch (InvalidSyntaxException ise) {
			logger.debug(ise.getMessage(), ise);
			logger.error(ise.getMessage());
		}
	}

	public static LicenseUtil get() {
		if (instance == null) {
			instance = new LicenseUtil();
		}
		return instance;
	}

	@Override
	public boolean checkAtStartUp(Shell shell) {
		LicenseModel licenseModel = null;
		try {
			licenseModel = persistenceService.loadFromPersistence();
		} catch (PersistenceException e) {
			handleStartUpPersistenceFailure(shell, e);
			licenseModel = factoryService.createDemoLicenseModel();
		}

		// Verify the persisted license' secret matches the hardware ID
		String secret = systemInfoWrapper.createUniqueHardwareId();
		boolean validSecret = licenseService.verifySecretKey(licenseModel, secret);
		if (!validSecret) {
			handleStartUpPersistenceFailure(shell, new PersistenceException("Invalid license data.")); //$NON-NLS-1$
			licenseModel = factoryService.createDemoLicenseModel();
			try {
				persistenceService.saveToPersistence(licenseModel);
			} catch (PersistenceException e) {
				logger.error("License could not be persisted", e); //$NON-NLS-1$
			}
		}

		Optional<String> encryptedEndpointOpt = loadEncryptedEndpointFromPersistence();
		try {
			String endpoint = ""; //$NON-NLS-1$
			if (encryptedEndpointOpt.isPresent()) {
				endpoint = endpointEncryption.decryptEndpoint(encryptedEndpointOpt.get());
			}

			result = licenseService.validate(licenseModel, endpoint);
		} catch (ValidationException | EndpointEncryptionException e) {
			handleStartUpValidationFailure(shell, e);
			return true;
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

	/**
	 * Does NOT check license validity.
	 * 
	 * @return whether the type of the validation result is either
	 *         {@link LicenseType#FLOATING}, {@link LicenseType#NODE_LOCKED}, or
	 *         {@link LicenseType#PAY_PER_USE}.
	 */
	public boolean isProLicense() {
		if (result == null) {
			return false;
		}
		LicenseType type = result.getLicenseType();
		return type == LicenseType.FLOATING || type == LicenseType.NODE_LOCKED || type == LicenseType.PAY_PER_USE;
	}

	@Override
	public LicenseUpdateResult update(String key) {
		if (key == null || key.isEmpty()) {
			return new LicenseUpdateResult(false, Messages.LicenseUtil_EmptyLicense);
		} else if (!endpointEncryption.isEncryptedKey(key) && !key.matches("[A-Z0-9]{9}")) { //$NON-NLS-1$
			return new LicenseUpdateResult(false, Messages.LicenseUtil_invalidLicenseFormat);
		}

		String secret = systemInfoWrapper.createUniqueHardwareId();
		LicenseValidationResult validationResult;
		LicenseModel model;
		String name = systemInfoWrapper.createNameFromHardware();
		Properties properties;
		try {
			properties = loadProperties();
		} catch (IOException e) {
			logger.error("Could not validate license", e); //$NON-NLS-1$
			return new LicenseUpdateResult(false,
					NLS.bind(Messages.UpdateLicenseDialog_error_couldNotValidate, e.getMessage()));
		}
		String productNr = properties.getProperty("license.productNr"); //$NON-NLS-1$
		String moduleNr = properties.getProperty("license.floatingModuleNr"); //$NON-NLS-1$

		String licenseKey = key;
		String endpoint = ""; //$NON-NLS-1$
		if (endpointEncryption.isEncryptedKey(key)) {
			try {
				licenseKey = endpointEncryption.decryptKey(key);
				endpoint = endpointEncryption.decryptEndpoint(key);
			} catch (EndpointEncryptionException e) {
				logger.error(e.getMessage(), e);
				return new LicenseUpdateResult(false,
						NLS.bind(Messages.UpdateLicenseDialog_error_couldNotValidate, e.getMessage()));
			}
		}

		model = factoryService.createNewModel(licenseKey, secret, productNr, moduleNr, LicenseType.NONE, name, null);
		try {
			validationResult = licenseService.validate(model, endpoint);
		} catch (ValidationException e) {
			logger.error("Could not validate license", e); //$NON-NLS-1$
			return new LicenseUpdateResult(false,
					NLS.bind(Messages.UpdateLicenseDialog_error_couldNotValidate, e.getMessage()));
		}

		if (!validationResult.isValid()) {
			logger.warn("License with key '{}' is not valid. License not saved.", key); //$NON-NLS-1$
			return new LicenseUpdateResult(false, NLS.bind(Messages.UpdateLicenseDialog_error_licenseInvalid, key));

		}

		if (validationResult.getLicenseType() == LicenseType.PAY_PER_USE) {
			moduleNr = properties.getProperty("license.payPerUseModuleNr"); //$NON-NLS-1$
		}
		LicenseModel persitModel = factoryService.createNewModel(validationResult.getKey(), secret, productNr, moduleNr,
				validationResult.getLicenseType(), name, validationResult.getExpirationDate());
		if (endpointEncryption.isEncryptedKey(key)) {
			persistEndpoint(key);
		}

		return persistLicense(persitModel);
	}

	@Override
	public void stop() {
		LicenseModel model = tryLoadModelFromPersistence();
		Optional<String> encryptedEndpointOpt = loadEncryptedEndpointFromPersistence();

		try {
			String endpoint = ""; //$NON-NLS-1$
			if (encryptedEndpointOpt.isPresent()) {
				endpoint = endpointEncryption.decryptEndpoint(encryptedEndpointOpt.get());
			}

			licenseService.checkIn(model, endpoint);
		} catch (ValidationException | EndpointEncryptionException e) {
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

		Optional<String> encryptedEndpointOpt = loadEncryptedEndpointFromPersistence();

		try {
			String endpoint = ""; //$NON-NLS-1$
			if (encryptedEndpointOpt.isPresent()) {
				endpoint = endpointEncryption.decryptEndpoint(encryptedEndpointOpt.get());
			}

			result = licenseService.validate(model, endpoint);
		} catch (ValidationException | EndpointEncryptionException e) {
			logger.error("Failed to validate license", e); //$NON-NLS-1$
			result = new LicenseValidationResult(model.getType(), "", false, //$NON-NLS-1$
					NLS.bind(Messages.MessageDialog_licensingError_failedToValidate, e.getMessage()),
					model.getExpirationDate());
		}
	}

	public boolean isValidProLicensePresentInSecureStore() {
		LicenseModel model = tryLoadModelFromPersistence();
		LicenseType type = model.getType();

		ZonedDateTime expireDate = model.getExpirationDate();
		boolean isValid = true;
		if (expireDate != null) {
			isValid = ZonedDateTime.now()
				.isBefore(model.getExpirationDate());
		}

		return isValid && type != LicenseType.DEMO;
	}

	private LicenseModel tryLoadModelFromPersistence() {
		LicenseModel model = null;
		try {
			model = persistenceService.loadFromPersistence();
		} catch (PersistenceException e) {
			logger.warn("Error while loading stored license, using default demo license", e); //$NON-NLS-1$
			model = factoryService.createDemoLicenseModel();
		}

		String secret = systemInfoWrapper.createUniqueHardwareId();
		if (!licenseService.verifySecretKey(model, secret)) {
			model = factoryService.createDemoLicenseModel();
		}
		return model;
	}

	private Optional<String> loadEncryptedEndpointFromPersistence() {
		String encrypted = null;
		try {
			String encryptedPersistence = endpointPersistenceService.loadFromPersistence();
			if (encryptedPersistence != null && !encryptedPersistence.isEmpty()) {
				encrypted = encryptedPersistence;
			}
		} catch (PersistenceException e) {
			logger.warn("Error while loading stored endpoint", e); //$NON-NLS-1$
		}
		return Optional.ofNullable(encrypted);
	}

	private void handleStartUpPersistenceFailure(Shell shell, PersistenceException e) {
		logger.error("Failed to load stored license. Falling back to free license.", e); //$NON-NLS-1$
		String message = NLS.bind(Messages.MessageDialog_licensingError_failedToLoad, e.getMessage());

		SimonykeesMessageDialog.openMessageDialog(shell, message, MessageDialog.ERROR);
	}

	private void handleStartUpValidationFailure(Shell shell, Exception e) {
		logger.error("Failed to validate license. ", e); //$NON-NLS-1$
		String message = NLS.bind(Messages.MessageDialog_licensingError_failedToValidate, e.getMessage());
		SimonykeesMessageDialog.openMessageDialog(shell, message, MessageDialog.ERROR);
	}

	private LicenseUpdateResult persistLicense(LicenseModel persitModel) {

		try {
			persistenceService.saveToPersistence(persitModel);
		} catch (PersistenceException e) {
			logger.error("License is valid but could not be persisted", e); //$NON-NLS-1$
			return new LicenseUpdateResult(false,
					NLS.bind(Messages.UpdateLicenseDialog_error_couldNotSave, e.getMessage()));
		}

		return new LicenseUpdateResult(true, Messages.SimonykeesUpdateLicenseDialog_license_updated_successfully);
	}

	private void persistEndpoint(String encryptedKey) {
		try {
			endpointPersistenceService.saveToPersistence(encryptedKey);
		} catch (PersistenceException e) {
			logger.error("License is valid but endpoint could not be persisted", e); //$NON-NLS-1$
		}

	}

	private Properties loadProperties() throws IOException {
		Properties properties = new Properties();
		try (InputStream input = getClass().getClassLoader()
			.getResourceAsStream("ui.properties")) { //$NON-NLS-1$
			properties.load(input);
		}
		return properties;

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

	public void reserveQuantity(int credit) {
		LicenseModel model = tryLoadModelFromPersistence();

		Optional<String> encryptedEndpointOpt = loadEncryptedEndpointFromPersistence();

		try {
			String endpoint = ""; //$NON-NLS-1$
			if (encryptedEndpointOpt.isPresent()) {
				endpoint = endpointEncryption.decryptEndpoint(encryptedEndpointOpt.get());
			}

			licenseService.reserveQuantity(model, credit, endpoint);
		} catch (ValidationException | EndpointEncryptionException e) {
			logger.error("Failed to validate license", e); //$NON-NLS-1$
			result = new LicenseValidationResult(model.getType(), "", false, //$NON-NLS-1$
					NLS.bind(Messages.MessageDialog_licensingError_failedToValidate, e.getMessage()),
					model.getExpirationDate());
		}
	}
}
