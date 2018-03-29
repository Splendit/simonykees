package eu.jsparrow.license.netlicensing.cleanslate.model;

import java.time.ZonedDateTime;

public class NetlicensingLicenseModel implements LicenseModel{

	private static final long serialVersionUID = 7047162817207967199L;
	
	private String key;
	private String name;
	private String product;
	private String secret;
	private NetlicensingLicenseType type;
	private ZonedDateTime expireDate;
	
	private ZonedDateTime offlineExpireDate;

	public NetlicensingLicenseModel(NetlicensingLicenseType type, String key, String secret) {
		this.type = type;
		this.key = key;
		this.secret = secret;
	}
	
	public NetlicensingLicenseModel(NetlicensingLicenseType type, String key, String name, String product, String secret, ZonedDateTime expireDate, ZonedDateTime offlineExpireDate) {
		this(type, key, secret);
		this.name = name;
		this.product = product;
		this.expireDate = expireDate;
		this.offlineExpireDate = offlineExpireDate;
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

	public String getProduct() {
		return product;
	}
	
	public String getSecret() {
		return secret;
	}
	
	public NetlicensingLicenseType getType() {
		return this.type;
	}

	public ZonedDateTime getOfflineExpireDate() {
		return offlineExpireDate;
	}

}
