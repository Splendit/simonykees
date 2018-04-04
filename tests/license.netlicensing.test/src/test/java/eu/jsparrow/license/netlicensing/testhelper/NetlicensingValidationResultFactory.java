package eu.jsparrow.license.netlicensing.testhelper;

import java.time.ZonedDateTime;

import eu.jsparrow.license.netlicensing.validation.impl.NetlicensingValidationResult;

public class NetlicensingValidationResultFactory {

	public static NetlicensingValidationResult create() {
		return new NetlicensingValidationResult(null, false, null);
	}

	public static NetlicensingValidationResult create(ZonedDateTime expiration) {
		return new NetlicensingValidationResult(null, false, expiration);
	}

}
