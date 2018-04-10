package eu.jsparrow.standalone;

import java.lang.invoke.MethodHandles;
import java.util.Random;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.jsparrow.license.api.LicenseModel;
import eu.jsparrow.license.api.LicenseModelFactoryService;
import eu.jsparrow.license.api.LicenseService;
import eu.jsparrow.license.api.LicenseType;
import eu.jsparrow.license.api.LicenseValidationResult;
import eu.jsparrow.license.api.exception.ValidationException;

@Component
public class StandaloneLicenseUtil implements StandaloneLicenseUtilService {

	private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup()
		.lookupClass());

	@Reference(cardinality = ReferenceCardinality.MANDATORY)
	private LicenseService licenseService;

	@Reference(cardinality = ReferenceCardinality.MANDATORY)
	private LicenseModelFactoryService factoryService;

	private LicenseValidationResult result = null;
	private Random random = new Random(System.currentTimeMillis());
	private LicenseModel model;

	@Override
	public boolean validate(String key) {
		String sessionId = Integer.toString(random.nextInt());
		try {
			model = factoryService.createNewFloatingModel(key, sessionId);
			result = licenseService.validate(model);
		} catch (ValidationException e) {
			logger.debug("Error connecting to license server:", e);
			logger.error("Error connecting to license server: ", e.getMessage());
			return false;
		}
		
		if(result.getLicenseType() != LicenseType.FLOATING) {
			logger.error("Unsupported License Type");
			return false;
		}
		
		if(result.isValid()) {
			logger.debug("License valid");
			return true;
		}

		logger.error(result.getDetail());
		return false;
	}

	@Override
	public void stop() {
		try {
			licenseService.checkIn(model);
		} catch (ValidationException e) {
			logger.error("Failed to check in license.", e); //$NON-NLS-1$
		}
	}
}
