package eu.jsparrow.license.netlicensing.model;

import java.lang.reflect.Field;
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

	private String secret; // excluded in toString

	@Shorten
	private String key;
	@Shorten
	private String productNr;
	@Shorten
	private String moduleNr;

	private String name;
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

	public NetlicensingLicenseModel(String key, String productNr, String moduleNr, LicenseType type) {
		this.type = type;
		this.key = key;
		this.productNr = productNr;
		this.moduleNr = moduleNr;
	}

	public NetlicensingLicenseModel(String key, String secret, String productNr, String moduleNr, LicenseType type,
			String name, ZonedDateTime expireDate) {
		this(key, secret, productNr, moduleNr, type);
		this.name = name;
		this.expireDate = expireDate;
	}

	public String getKey() {
		return key;
	}

	public String getName() {
		return name;
	}

	@Override
	public ZonedDateTime getExpirationDate() {
		return expireDate;
	}

	public String getSecret() {
		return secret;
	}

	@Override
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
		/*
		 * with commons lang 3.5 the ToStringExclude Annotation could be used
		 * instead
		 */
		return (new AnnotationToStringBuilder(this) {
			@Override
			protected boolean accept(Field f) {
				return super.accept(f) && !f.getName()
					.equals("secret"); //$NON-NLS-1$
			}
		}).toString();
	}

}
