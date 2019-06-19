package eu.jsparrow.standalone;

import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import java.util.Properties;
import java.util.Random;

import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.jsparrow.i18n.Messages;
import eu.jsparrow.license.api.LicenseModel;
import eu.jsparrow.license.api.LicenseModelFactoryService;
import eu.jsparrow.license.api.LicenseService;
import eu.jsparrow.license.api.LicenseType;
import eu.jsparrow.license.api.LicenseValidationResult;
import eu.jsparrow.license.api.exception.ValidationException;
import eu.jsparrow.standalone.exceptions.StandaloneException;

public class StandaloneLicenseUtil implements StandaloneLicenseUtilService {

	private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup()
		.lookupClass());

	ServiceReference<LicenseService> licenseReference;
	ServiceReference<LicenseModelFactoryService> factoryReference;

	private LicenseService licenseService;
	private LicenseModelFactoryService factoryService;

	private Random random = new Random(System.currentTimeMillis());
	private LicenseModel model;

	private static final String LINE_SEPARATOR_EQUAL = "================================================================================\n"; //$NON-NLS-1$
	private static final String LINE_SEPARATOR_HIPHEN = "--------------------------------------------------------------------------------\n"; //$NON-NLS-1$

	private static StandaloneLicenseUtil instance;

	public static StandaloneLicenseUtil get() {
		if (instance == null) {
			instance = new StandaloneLicenseUtil();
		}
		return instance;
	}

	private StandaloneLicenseUtil() {
		BundleContext bundleContext = FrameworkUtil.getBundle(getClass())
			.getBundleContext();

		licenseReference = bundleContext.getServiceReference(LicenseService.class);
		licenseService = bundleContext.getService(licenseReference);

		factoryReference = bundleContext.getServiceReference(LicenseModelFactoryService.class);
		factoryService = bundleContext.getService(factoryReference);
	}

	@Override
	public boolean validate(String key, String validationBaseUrl) throws StandaloneException {
		if (key == null || key.isEmpty()) {
			logger.error("No License Key has been specified."); //$NON-NLS-1$
			return false;
		}

		LicenseValidationResult result = tryGetValidationResult(key, validationBaseUrl);
		if (result == null) {
			return false;
		}
		if (result.getLicenseType() != LicenseType.FLOATING) {
			logger.error("Unsupported License Type"); //$NON-NLS-1$
			return false;
		}

		if (result.isValid()) {
			logger.debug("License valid"); //$NON-NLS-1$
			return true;
		}

		logger.error(result.getDetail());
		return false;
	}

	@Override
	public void licenseInfo(String key, String validationBaseUrl) throws StandaloneException {

		if (key == null || key.isEmpty()) {
			logger.error("No License Key has been specified."); //$NON-NLS-1$
			return;
		}

		LicenseValidationResult result = tryGetValidationResult(key, validationBaseUrl);
		if (result == null) {
			return;
		}

		StringBuilder sb = new StringBuilder();
		sb.append("\n"); //$NON-NLS-1$
		sb.append(LINE_SEPARATOR_EQUAL);

		sb.append(Messages.StandaloneLicenseUtil_licenseType);
		sb.append(result.getLicenseType());
		sb.append("\n"); //$NON-NLS-1$
		sb.append(LINE_SEPARATOR_HIPHEN);
		sb.append(Messages.StandaloneLicenseUtil_isValid);
		sb.append(result.isValid());
		sb.append("\n"); //$NON-NLS-1$
		sb.append(LINE_SEPARATOR_HIPHEN);
		sb.append(Messages.StandaloneLicenseUtil_expirationDate);
		sb.append(result.getExpirationDate());
		sb.append("\n"); //$NON-NLS-1$
		sb.append(LINE_SEPARATOR_EQUAL);

		String info = sb.toString();
		logger.info(info);
	}

	private LicenseValidationResult tryGetValidationResult(String key, String validationBaseUrl)
			throws StandaloneException {
		String sessionId = Integer.toString(random.nextInt());
		LicenseValidationResult result = null;
		try {
			Properties properties = loadProperties();
			String productNr = properties.getProperty("license.productNr"); //$NON-NLS-1$
			String moduleNr = properties.getProperty("license.moduleNr"); //$NON-NLS-1$

			model = factoryService.createNewFloatingModel(key, sessionId, productNr, moduleNr);
			result = licenseService.validate(model, validationBaseUrl);
		} catch (ValidationException | IOException e) {
			throw new StandaloneException(e.getMessage(), e);
		}
		return result;
	}

	private Properties loadProperties() throws IOException {
		Properties properties = new Properties();
		try (InputStream input = getClass().getClassLoader()
			.getResourceAsStream("standalone.properties")) { //$NON-NLS-1$
			properties.load(input);
		}
		return properties;

	}

	@Override
	public void stop(String validationBaseUrl) {
		try {
			if (model != null) {
				licenseService.checkIn(model, validationBaseUrl);
			}
		} catch (ValidationException e) {
			logger.debug("Failed to check in License: ", e); //$NON-NLS-1$
			logger.error("Failed to check in license: {}", e.getMessage()); //$NON-NLS-1$
		}
	}
}
