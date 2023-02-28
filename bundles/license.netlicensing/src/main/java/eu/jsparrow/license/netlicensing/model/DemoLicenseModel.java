package eu.jsparrow.license.netlicensing.model;

import java.time.Year;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

import eu.jsparrow.license.api.LicenseModel;
import eu.jsparrow.license.api.LicenseType;

public class DemoLicenseModel implements LicenseModel {

	private static final long serialVersionUID = 5753428747671948588L;

	private static final ZonedDateTime EXPIRATION_IN_FAR_FUTURE = ZonedDateTime.of(Year.MAX_VALUE, 12, 1, 0, 0, 0, 0,
			ZoneId.systemDefault());

	private static final LicenseType TYPE = LicenseType.DEMO;

	@Override
	public ZonedDateTime getExpirationDate() {
		return EXPIRATION_IN_FAR_FUTURE;
	}

	@Override
	public LicenseType getType() {
		return TYPE;
	}

	@Override
	public String toString() {
		return ReflectionToStringBuilder.toString(this);
	}
}
