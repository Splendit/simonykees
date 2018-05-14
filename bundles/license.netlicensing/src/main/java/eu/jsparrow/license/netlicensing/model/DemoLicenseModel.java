package eu.jsparrow.license.netlicensing.model;

import java.time.ZonedDateTime;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

import eu.jsparrow.license.api.LicenseModel;
import eu.jsparrow.license.api.LicenseType;

public class DemoLicenseModel implements LicenseModel {

	private static final long serialVersionUID = 5753428747671948588L;

	private ZonedDateTime expirationDate;

	private static final LicenseType TYPE = LicenseType.DEMO;

	public DemoLicenseModel() {
		expirationDate = ZonedDateTime.now().plusDays(5);
	}

	public DemoLicenseModel(ZonedDateTime expirationDate) {
		this.expirationDate = expirationDate;
	}

	public void setExpirationDate(ZonedDateTime expirationDate) {
		this.expirationDate = expirationDate;
	}

	public ZonedDateTime getExpirationDate() {
		return expirationDate;
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
