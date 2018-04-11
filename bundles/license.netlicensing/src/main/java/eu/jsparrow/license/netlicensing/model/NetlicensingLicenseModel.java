package eu.jsparrow.license.netlicensing.model;

import java.time.ZonedDateTime;

import eu.jsparrow.license.api.LicenseModel;
import eu.jsparrow.license.api.LicenseType;

/**
 * Implementor of {@link LicenseModel} that represents a NetLicensing license.
 */
public class NetlicensingLicenseModel implements LicenseModel {

	private static final long serialVersionUID = 7047162817207967199L;

	private String key;
	private String name;
	private String secret;
	private LicenseType type;
	private ZonedDateTime expireDate;

	public NetlicensingLicenseModel(LicenseType type, String key, String secret) {
		this.type = type;
		this.key = key;
		this.secret = secret;
	}

	public NetlicensingLicenseModel(LicenseType type, String key, String name, String secret,
			ZonedDateTime expireDate) {
		this(type, key, secret);
		this.name = name;
		this.expireDate = expireDate;
	}

	public String getKey() {
		return key;
	}

	public String getName() {
		return name;
	}

	public ZonedDateTime getExpirationDate() {
		return expireDate;
	}

	public String getSecret() {
		return secret;
	}

	public LicenseType getType() {
		return this.type;
	}

	@Override
	public String toString() {
		return "NetlicensingLicenseModel [key=" + key + ", name=" + name + ", secret=" + secret + ", type=" + type
				+ ", expireDate=" + expireDate + "]";
	}

}
