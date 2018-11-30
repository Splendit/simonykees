package eu.jsparrow.ui.util;

import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import java.net.InetAddress;
import java.net.UnknownHostException;
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
import eu.jsparrow.license.api.RegistrationModel;
import eu.jsparrow.license.api.RegistrationModelFactoryService;
import eu.jsparrow.license.api.RegistrationService;
import eu.jsparrow.license.api.exception.PersistenceException;
import eu.jsparrow.license.api.exception.ValidationException;
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
public class LicenseUtil implements LicenseUtilService, RegistrationUtilService {

	private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup()
		.lookupClass());

	private static LicenseUtil instance;

	private LicenseService licenseService;
	private RegistrationService registrationService;

	private LicensePersistenceService<LicenseModel> persistenceService;
	private LicensePersistenceService<RegistrationModel> registrationPersistenceSerice;

	private LicenseModelFactoryService factoryService;
	private RegistrationModelFactoryService registrationModelFactoryService;

	private LicenseValidationResult result = null;

	private Scheduler scheduler;

	private LicenseUtil() {
		scheduler = new Scheduler(this);
		scheduler.start();

		BundleContext bundleContext = FrameworkUtil.getBundle(getClass())
			.getBundleContext();

		ServiceReference<LicenseService> licenseReference = bundleContext.getServiceReference(LicenseService.class);
		licenseService = bundleContext.getService(licenseReference);
		
		ServiceReference<RegistrationService> registrationReference = bundleContext.getServiceReference(RegistrationService.class);
		registrationService = bundleContext.getService(registrationReference);

		ServiceReference<LicensePersistenceService> persistenceReference = bundleContext
			.getServiceReference(LicensePersistenceService.class);
		persistenceService = bundleContext.getService(persistenceReference);
		
		initPersistenceServices();

		ServiceReference<LicenseModelFactoryService> factoryReference = bundleContext
			.getServiceReference(LicenseModelFactoryService.class);
		factoryService = bundleContext.getService(factoryReference);
		
		ServiceReference<RegistrationModelFactoryService> registrationFactoryReference = bundleContext.getServiceReference(RegistrationModelFactoryService.class);
		registrationModelFactoryService = bundleContext.getService(registrationFactoryReference);
	}
	
	private void initPersistenceServices() {
		BundleContext bundleContext = FrameworkUtil.getBundle(LicenseUtil.class).getBundleContext();
		try {
			ServiceReference<?>[] serviceReferences = bundleContext.getServiceReferences(LicensePersistenceService.class.getName(), null);
			for(ServiceReference<?> service : serviceReferences) {
				LicensePersistenceService<?> persistenceService = (LicensePersistenceService) bundleContext.getService(service);
				if(persistenceService.getClass().getName().contains("NetlicensingLicense")) {
					this.persistenceService = (LicensePersistenceService<LicenseModel>) persistenceService;
				} else if(persistenceService.getClass().getName().contains("CustomerRegistrationPersistence")) {
					this.registrationPersistenceSerice =  (LicensePersistenceService<RegistrationModel>) persistenceService;
				}
				
			}
		} catch (InvalidSyntaxException e) {
			logger.error("Failed to load license persistence service", e); //$NON-NLS-1$
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
		RegistrationModel registrationModel = null;
		try {
			licenseModel = persistenceService.loadFromPersistence();
		} catch (PersistenceException e) {
			handleStartUpPersistenceFailure(shell, e);
			licenseModel = factoryService.createDemoLicenseModel();
		}
		
		try {
			registrationModel = registrationPersistenceSerice.loadFromPersistence();
		} catch (PersistenceException e) {
			handleStartUpPersistenceFailure(shell, e);
			registrationModel = registrationModelFactoryService.createRegistrationMode();
		}
		
		try {
			result = licenseService.validate(licenseModel);
		} catch (ValidationException e) {
			handleStartUpValidationFailure(shell, e);
			return true;
		}
		
		try {
			boolean isValidRegistration = registrationService.validate(registrationModel);
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
			String name = createNameFromHardware();
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
	public void activateRegistration(String key, String email) {
		try {
			RegistrationModel registrationModel = registrationPersistenceSerice.loadFromPersistence();
			
			RegistrationModel model = registrationModelFactoryService.createRegistrationModel(key, email, 
					registrationModel.getFirstName(), registrationModel.getLastName(), registrationModel.getCompany(), registrationModel.hasSubscribed());
			boolean successful = registrationService.register(model);
			if(successful) {
				registrationPersistenceSerice.saveToPersistence(model);
			} else {
				registrationPersistenceSerice.saveToPersistence(registrationModelFactoryService.createRegistrationMode());
			}
			
		} catch (PersistenceException e) {
			// TODO Auto-generated catch block
		} catch (ValidationException e) {
			// TODO Auto-generated catch block
		}
	}
	
	@Override
	public void register(String email, String firstName, String lastName, String company, boolean subscribe) {
		RegistrationModel model = registrationModelFactoryService.createRegistrationModel("", email, firstName, lastName, company, subscribe);
		try {
			boolean successful = registrationService.register(model);
			if(successful) {
				registrationPersistenceSerice.saveToPersistence(model);
			} else {
				registrationPersistenceSerice.saveToPersistence(registrationModelFactoryService.createRegistrationMode());
			}
		} catch (ValidationException e) {
			// TODO Auto-generated catch block
			
		} catch (PersistenceException e) {
			// TODO Auto-generated catch block
		}
	}
	
	@Override
	public boolean isActiveRegistration() {
		try {
			RegistrationModel model = registrationPersistenceSerice.loadFromPersistence();
			return !model.getKey().isEmpty();
		} catch (PersistenceException e) {
			// TODO Auto-generated catch block
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
}
