package eu.jsparrow.license.netlicensing.cleanslate.validation.impl.response.model;

import java.time.ZonedDateTime;

public class Subscription extends LicensingModel {
	
	public static final String LICENSING_MODEL = "Subscription"; //$NON-NLS-1$
	public static final String EXPIRES_KEY = "expires"; //$NON-NLS-1$

	private ZonedDateTime expires;
	
	public Subscription(boolean valid) {
		super(valid);
	}
	
	public Subscription(ZonedDateTime expires, boolean valid) {
		this(valid);
		this.expires = expires;
	}

	public ZonedDateTime getExpires() {
		return expires;
	}
}
