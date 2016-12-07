package at.splendit.simonykees.core.license;

import java.time.Instant;

import com.labs64.netlicensing.domain.vo.Composition;
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
			Context context = APIRestConnection.getAPIRestConnection().getContext();
			
			Instant timestamp = Instant.now();

			// sending validation request...
			ValidationResult validationResult = LicenseeService.validate(context, licenseeNumber, validationParameters);

			// caching the validation result...
			ValidationResultCache cache = ValidationResultCache.getInstance();
			cache.updateCachedResult(validationResult, timestamp);
			
			// logging validation result...
			// TODO: use a logger instead of System.out
			System.out.println(validationResult.getValidations().size());

			for (Composition value : validationResult.getValidations().values()) {
				System.out.println("model = " + value);

				for (String key : value.getProperties().keySet()) {
					System.out.print("Key = " + key);
					System.out.println("  value:  " + value.getProperties().get(key).getValue());
				}

			}

		} catch (final NetLicensingException e) {
			System.out.println("Got NetLicensing exception:" + e);
			// TODO: in each exception case, a proper behavior should be
			// triggered.
		} catch (final Exception e) {
			System.out.println("Got  exception:" + e);
		}
	}

}