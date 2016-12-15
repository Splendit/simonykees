package at.splendit.simonykees.core.license;

import java.time.Instant;

import com.labs64.netlicensing.domain.vo.Context;
import com.labs64.netlicensing.domain.vo.ValidationParameters;
import com.labs64.netlicensing.domain.vo.ValidationResult;
import com.labs64.netlicensing.exception.NetLicensingException;
import com.labs64.netlicensing.service.LicenseeService;

public class LicenseValidator {

	public static void doValidate(LicenseeEntity licensee) {

		try {

			// preparing validation parameters...
			ValidationParameters validationParameters = licensee.getValidationParams();
			String licenseeNumber = licensee.getLicenseeNumber();
			Context context = RestApiConnection.getAPIRestConnection().getContext();
			
			Instant timestamp = Instant.now();

			// sending validation request...
			ValidationResult validationResult = LicenseeService.validate(context, licenseeNumber, validationParameters);

			// caching and persisting the validation result...
			ValidationResultCache cache = ValidationResultCache.getInstance();
			cache.updateCachedResult(validationResult, timestamp);
			PersistenceManager persistenceManager = PersistenceManager.getInstance();
			persistenceManager.persistCachedData();
			
			// logging validation result...
			// TODO: log a message that a validation response was received successfully...

		} catch (final NetLicensingException e) {
			ValidationResultCache cache = ValidationResultCache.getInstance();
			cache.reset();
			System.out.println("Got NetLicensing exception:" + e);
			// TODO: in each exception case, a proper behavior should be
			// triggered.

		}
	}

}