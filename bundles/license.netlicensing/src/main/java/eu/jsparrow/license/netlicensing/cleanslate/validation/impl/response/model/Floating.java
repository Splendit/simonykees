package eu.jsparrow.license.netlicensing.cleanslate.validation.impl.response.model;

import java.time.ZonedDateTime;

public class Floating extends LicensingModel {
	
	public static final String LICENSING_MODEL = "Floating"; //$NON-NLS-1$
	public static final String EXPIRATION_TIME_STAMP_KEY = "expirationTimestamp"; //$NON-NLS-1$
	
	
	private ZonedDateTime expirationTimeStamp;
	
	public Floating(boolean valid) {
		super(valid);
	}
	
	public Floating(ZonedDateTime expirationTimeStamp, boolean valid) {
		this(valid);
		this.expirationTimeStamp = expirationTimeStamp;
	}
	
	public ZonedDateTime getExpirationTimeStamp() {
		return expirationTimeStamp;
	}
}
