package at.splendit.simonykees.core.license;

import java.time.Instant;

import org.eclipse.core.runtime.Status;

import com.labs64.netlicensing.domain.vo.Context;
import com.labs64.netlicensing.domain.vo.ValidationParameters;
import com.labs64.netlicensing.domain.vo.ValidationResult;
import com.labs64.netlicensing.exception.NetLicensingException;
import com.labs64.netlicensing.service.LicenseeService;

import at.splendit.simonykees.core.Activator;

public class LicenseValidator {

	public static void doValidate(LicenseeEntity licensee) {

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
			cache.updateCachedResult(validationResult, licenseeName, licenseeNumber, timestamp);
			PersistenceManager persistenceManager = PersistenceManager.getInstance();
			persistenceManager.persistCachedData();
			
			// logging validation result...
			//Activator.log("License validation response received successfully ");

		} catch (final NetLicensingException e) {
			ValidationResultCache cache = ValidationResultCache.getInstance();
			cache.reset();
			Activator.log(Status.WARNING, "Couldn't reach licensing provider", e);
		}
	}

}