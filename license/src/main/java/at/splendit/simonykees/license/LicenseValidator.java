package at.splendit.simonykees.license;

import java.time.Instant;

import org.eclipse.core.runtime.Status;

import com.labs64.netlicensing.domain.vo.Context;
import com.labs64.netlicensing.domain.vo.ValidationParameters;
import com.labs64.netlicensing.domain.vo.ValidationResult;
import com.labs64.netlicensing.exception.NetLicensingException;
import com.labs64.netlicensing.service.LicenseeService;

import at.splendit.simonykees.i18n.Messages;

import at.splendit.simonykees.license.model.LicenseeModel;

/**
 * Sends a routine validate call. The result is cashed and persisted.
 * 
 * @author Ardit Ymeri
 * @since 1.0
 *
 */
public class LicenseValidator {

	public static void doValidate(LicenseeModel licensee) {

		try {
			// preparing validation parameters...
			ValidationParameters validationParameters = licensee.getValidationParams();
			String licenseeNumber = licensee.getLicenseeNumber();
			String licenseeName = licensee.getLicenseeName();
			Context context = RestApiConnection.getAPIRestConnection().getContext();
			
			Instant timestamp = Instant.now();

			// sending validation request...
			ValidationResult validationResult = LicenseeService.validate(context, licenseeNumber, validationParameters);

			// caching and persisting the validation result...
			ValidationResultCache cache = ValidationResultCache.getInstance();
			cache.updateCachedResult(validationResult, licenseeName, licenseeNumber, timestamp, ValidationAction.CHECK_OUT);
			PersistenceManager persistenceManager = PersistenceManager.getInstance();
			persistenceManager.persistCachedData();
			
			// logging validation result...
			Activator.log(Messages.LicenseValidator_received_validation_response);

		} catch (final NetLicensingException e) {
			ValidationResultCache cache = ValidationResultCache.getInstance();
			cache.reset();
			Activator.log(Status.WARNING, Messages.LicenseValidator_cannot_reach_license_provider_on_validation_call, e);
		}
	}
	
	public static boolean isValidLicensee(String licenseeNumber) {
		boolean validLicensee = false;
		Context context = RestApiConnection.getAPIRestConnection().getContext();
		try {
			ValidationResult validationResult = LicenseeService.validate(context, licenseeNumber, new ValidationParameters());
			validLicensee = ResponseParser.parseLicenseeValidation(validationResult);
		} catch (NetLicensingException e) {
			Activator.log(Status.WARNING, Messages.LicenseValidator_invalid_licensee_number, e);
		}
		
		return validLicensee;
	}

}