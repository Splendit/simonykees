package eu.jsparrow.license.netlicensing.model;

import java.time.ZonedDateTime;

import eu.jsparrow.license.api.LicenseModel;
import eu.jsparrow.license.api.LicenseType;
import eu.jsparrow.license.api.util.AnnotationToStringBuilder;
import eu.jsparrow.license.api.util.Shorten;

/**
 * Implementor of {@link LicenseModel} that represents a NetLicensing license.
 */
public class NetlicensingLicenseModel implements LicenseModel {

	private static final long serialVersionUID = 7047162817207967199L;

	@Shorten
	private String key;

	private String name;
	private String secret;
	private String productNr;
	private String moduleNr;
	private LicenseType type;
	private ZonedDateTime expireDate;
	private String validationBaseUrl;

	public NetlicensingLicenseModel(String key, String secret, String productNr, String moduleNr, LicenseType type) {
		this.type = type;
		this.key = key;
		this.secret = secret;
		this.productNr = productNr;
		this.moduleNr = moduleNr;
	}

	public NetlicensingLicenseModel(String key, String secret, String productNr, String moduleNr, LicenseType type,
			String validationBaseUrl) {
		this(key, secret, productNr, moduleNr, type);
		this.validationBaseUrl = validationBaseUrl;
	}

	public NetlicensingLicenseModel(String key, String secret, String productNr, String moduleNr, LicenseType type,
			String name, ZonedDateTime expireDate) {
		this(key, secret, productNr, moduleNr, type);
		this.name = name;
		this.expireDate = expireDate;
	}

	public NetlicensingLicenseModel(String key, String secret, String productNr, String moduleNr, LicenseType type,
			String name, ZonedDateTime expireDate, String validationBaseUrl) {
		this(key, secret, productNr, moduleNr, type, name, expireDate);
		this.validationBaseUrl = validationBaseUrl;
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

	public String getProductNr() {
		return productNr;
	}

	public String getModuleNr() {
		return moduleNr;
	}

	public String getValidationBaseUrl() {
		return validationBaseUrl;
	}

	@Override
	public String toString() {
		return new AnnotationToStringBuilder(this).toString();
	}

}
