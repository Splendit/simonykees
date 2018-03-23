package eu.jsparrow.license.netlicensing.cleanslate.model;

import java.time.ZonedDateTime;

public class NetlicensingLicenseModel implements LicenseModel{

	private String key;
	private String name;
	private ZonedDateTime expireDate;
	private String product;
	private String secret;
	private LicenseType type;

	public NetlicensingLicenseModel(LicenseType type, String key, String name, String product, String secret, ZonedDateTime expireDate) {
		setType(type);
		setKey(key);
		setName(name);
		setProduct(product);
		setExpireDate(expireDate);
		setSecret(secret);
	}

	private void setType(LicenseType type2) {
		this.type = type2;
	}

	private void setKey(String key) {
		this.key = key;
	}

	private void setName(String name) {
		this.name = name;
	}

	private void setExpireDate(ZonedDateTime expireDate) {
		this.expireDate = expireDate;
	}

	private void setProduct(String product) {
		this.product = product;
	}
	
	private void setSecret(String secret) {
		this.secret = secret;
	}

	public String getKey() {
		return key;
	}

	public String getName() {
		return name;
	}

	public ZonedDateTime getExpireDate() {
		return expireDate;
	}

	public String getProduct() {
		return product;
	}
	
	public String getSecret() {
		return secret;
	}
	
	public LicenseType getType() {
		return this.type;
	}
}
