package eu.jsparrow.license.netlicensing.validation.impl.response.model;

import java.time.ZonedDateTime;

public class SubscriptionResponse extends NetlicensingResponse {
	
	public static final String LICENSING_MODEL = "Subscription"; //$NON-NLS-1$
	public static final String EXPIRES_KEY = "expires"; //$NON-NLS-1$

	private ZonedDateTime expires;
	
	public SubscriptionResponse(boolean valid) {
		super(valid);
	}
	
	public SubscriptionResponse(ZonedDateTime expires, boolean valid) {
		this(valid);
		this.expires = expires;
	}

	public ZonedDateTime getExpires() {
		return expires;
	}
}
