package eu.jsparrow.ui.util;

import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import java.time.ZonedDateTime;
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
import eu.jsparrow.license.api.RegistrationService;
import eu.jsparrow.license.api.exception.PersistenceException;
import eu.jsparrow.license.api.exception.ValidationException;
import eu.jsparrow.ui.dialog.BuyLicenseDialog;
import eu.jsparrow.ui.dialog.SimonykeesMessageDialog;
import eu.jsparrow.ui.dialog.SuggestRegistrationDialog;
import eu.jsparrow.ui.preference.SimonykeesPreferenceManager;
import eu.jsparrow.ui.startup.registration.entity.ActivationEntity;
import eu.jsparrow.ui.startup.registration.entity.RegistrationEntity;

/**
 * Implements {@link LicenseUtilService}. The purpose of this class is to wrap
 * some license management functions for the UI. For example, the flow for
 * updating a license.
 * 
 * It uses various services from the License API package.
 */
public class LicenseUtil implements LicenseUtilService, RegistrationUtilService {

	private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup()
		.lookupClass());

	private static LicenseUtil instance;

	private LicenseService licenseService;
	private RegistrationService registrationService;

	private LicensePersistenceService<LicenseModel> persistenceService;
	private LicensePersistenceService<String> registrationPersistenceSerice;

	private LicenseModelFactoryService factoryService;

	private LicenseValidationResult result = null;

	private Scheduler scheduler;
	private SystemInfoWrapper systemInfoWrapper;

	private boolean shouldContinueWithSelectRules = true;

	private LicenseUtil() {
		scheduler = new Scheduler(this);
		systemInfoWrapper = new SystemInfoWrapper();
		scheduler.start();

		BundleContext bundleContext = FrameworkUtil.getBundle(getClass())
			.getBundleContext();

		ServiceReference<LicenseService> licenseReference = bundleContext.getServiceReference(LicenseService.class);
		licenseService = bundleContext.getService(licenseReference);

		ServiceReference<RegistrationService> registrationReference = bundleContext
			.getServiceReference(RegistrationService.class);
		registrationService = bundleContext.getService(registrationReference);

		initPersistenceServices(bundleContext);

		ServiceReference<LicenseModelFactoryService> factoryReference = bundleContext
			.getServiceReference(LicenseModelFactoryService.class);
		factoryService = bundleContext.getService(factoryReference);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void initPersistenceServices(BundleContext bundleContext) {
		try {
			ServiceReference registrationReferences[] = bundleContext
				.getServiceReferences(LicensePersistenceService.class.getName(), "(licenseType=registration)"); //$NON-NLS-1$
			ServiceReference netlicensingReferences[] = bundleContext
				.getServiceReferences(LicensePersistenceService.class.getName(), "(licenseType=default)"); //$NON-NLS-1$

			if (registrationReferences.length != 0 && netlicensingReferences.length != 0) {

				this.registrationPersistenceSerice = (LicensePersistenceService<String>) bundleContext
					.getService(registrationReferences[0]);
				this.persistenceService = (LicensePersistenceService<LicenseModel>) bundleContext
					.getService(netlicensingReferences[0]);
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

		try {
			result = licenseService.validate(licenseModel);
		} catch (ValidationException e) {
			handleStartUpValidationFailure(shell, e);
			return true;
		}

		// When starting with an expired demo license we show the wizard dialog
		if (result.getLicenseType() == LicenseType.DEMO && !result.isValid()) {
			BuyLicenseDialog dialog = new BuyLicenseDialog(shell);
			return dialog.open() == 0;
		}
		// When starting with an demo license we offer to register for free
		// rules if not registered yet
		if (isFreeLicense() && !isActiveRegistration() && !SimonykeesPreferenceManager.getDisableRegisterSuggestion()) {
			setShouldContinueWithSelectRules(true);
			SuggestRegistrationDialog dialog = new SuggestRegistrationDialog(shell);
			return (dialog.open() == 0) && shouldContinueWithSelectRules;
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
	 *         {@link LicenseType#FLOATING} or {@link LicenseType#NODE_LOCKED}.
	 */
	public boolean isProLicense() {
		if (result == null) {
			return false;
		}
		LicenseType type = result.getLicenseType();
		return type == LicenseType.FLOATING || type == LicenseType.NODE_LOCKED;
	}

	@Override
	public LicenseUpdateResult update(String key) {
		if (key == null || key.isEmpty()) {
			return new LicenseUpdateResult(false, Messages.LicenseUtil_EmptyLicense);
		} else if (key.matches(".*[\\/].*")) { //$NON-NLS-1$
			return new LicenseUpdateResult(false, "License contains illegal characters");
		}
		String secret = systemInfoWrapper.createUniqueHardwareId();
		LicenseValidationResult validationResult;
		LicenseModel model;
		try {
			String name = systemInfoWrapper.createNameFromHardware();
			Properties properties = loadProperties();
			String productNr = properties.getProperty("license.productNr"); //$NON-NLS-1$
			String moduleNr = properties.getProperty("license.moduleNr"); //$NON-NLS-1$
			model = factoryService.createNewModel(key, secret, productNr, moduleNr, LicenseType.NONE, name, null);
			validationResult = licenseService.validate(model);
		} catch (ValidationException | IOException e) {
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

	@Override
	public boolean activateRegistration(ActivationEntity activationEntity) {
		String secret = systemInfoWrapper.createUniqueHardwareId();
		String activationKey = activationEntity.getActivationKey();
		try {
			boolean successful = registrationService.activate(activationKey);
			if (successful) {
				registrationPersistenceSerice.saveToPersistence(secret);
				return true;
			}
		} catch (PersistenceException e) {
			logger.warn("Failed to persist registration", e); //$NON-NLS-1$
		} catch (ValidationException e) {
			logger.warn("Cannot activate registration key: '{}'", activationKey, e); //$NON-NLS-1$
		}
		return false;
	}

	@Override
	public boolean register(RegistrationEntity registerEntity) {
		String email = registerEntity.getEmail();
		String firstName = registerEntity.getFirstName();
		String lastName = registerEntity.getLastName();
		String company = registerEntity.getCompany();
		boolean subscribe = registerEntity.isAgreeToNewsletter();
		try {
			return registrationService.register(email, firstName, lastName, company, subscribe);
		} catch (ValidationException e) {
			logger.warn("Failed to register", e); //$NON-NLS-1$
		}
		return false;
	}

	@Override
	public boolean isActiveRegistration() {
		String hardwareId = systemInfoWrapper.createUniqueHardwareId();
		try {
			String secret = registrationPersistenceSerice.loadFromPersistence();
			return registrationService.validate(hardwareId, secret);
		} catch (PersistenceException e) {
			logger.warn("Failed to load registration model", e); //$NON-NLS-1$
		}
		return false;
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

	public boolean isValidProLicensePresentInSecureStore() {
		LicenseModel model = tryLoadModelFromPersistence();
		LicenseType type = model.getType();
		boolean isValid = ZonedDateTime.now().isBefore(model.getExpirationDate());
		
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

	public void setShouldContinueWithSelectRules(boolean shouldContinue) {
		shouldContinueWithSelectRules = shouldContinue;
	}
}
