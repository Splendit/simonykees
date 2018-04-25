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
	private String productNr;
	private String moduleNr;
	private LicenseType type;
	private ZonedDateTime expireDate;

	public NetlicensingLicenseModel(String key, String secret, String productNr, String moduleNr, LicenseType type) {
		this.type = type;
		this.key = key;
		this.secret = secret;
		this.productNr = productNr;
		this.moduleNr = moduleNr;
	}

	public NetlicensingLicenseModel(String key, String secret, String productNr, String moduleNr, LicenseType type, String name, 
			ZonedDateTime expireDate) {
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
	
	@Override
	public String toString() {
		return "NetlicensingLicenseModel [key=" + key + ", name=" + name + ", secret=" + secret + ", productNr=" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
				+ productNr + ", moduleNr=" + moduleNr + ", type=" + type + ", expireDate=" + expireDate + "]"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
	}


}
