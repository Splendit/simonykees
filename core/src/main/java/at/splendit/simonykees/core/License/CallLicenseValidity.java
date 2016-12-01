package at.splendit.simonykees.core.License;

import com.labs64.netlicensing.domain.vo.Composition;
import com.labs64.netlicensing.domain.vo.Context;
import com.labs64.netlicensing.domain.vo.SecurityMode;
import com.labs64.netlicensing.domain.vo.ValidationParameters;
import com.labs64.netlicensing.domain.vo.ValidationResult;
import com.labs64.netlicensing.exception.NetLicensingException;
import com.labs64.netlicensing.service.LicenseeService;

public class CallLicenseValidity {

	
	
	public static void doValidate(LicenseeEntity licensee) {

		try {

			ValidationResult validationResult = LicenseeService.validate(APIRestConnection.getAPIRestConnection().getContext(), licensee.getLicenseeNumber(),licensee.PRODUCT_NUMBER,
					licensee.getLicenseeName(), new ValidationParameters());

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
		} catch (final Exception e) {
			System.out.println("Got  exception:" + e);
		}
	}

	
}