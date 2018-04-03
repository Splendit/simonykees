package eu.jsparrow.license.netlicensing.validation.impl.response.model;

import java.time.ZonedDateTime;

public class FloatingResponse extends NetlicensingResponse {
	
	public static final String LICENSING_MODEL = "Floating"; //$NON-NLS-1$
	public static final String EXPIRATION_TIME_STAMP_KEY = "expirationTimestamp"; //$NON-NLS-1$
	
	
	private ZonedDateTime expirationTimeStamp;
	
	public FloatingResponse(boolean valid) {
		super(valid);
	}
	
	public FloatingResponse(ZonedDateTime expirationTimeStamp, boolean valid) {
		this(valid);
		this.expirationTimeStamp = expirationTimeStamp;
	}
	
	public ZonedDateTime getExpirationTimeStamp() {
		return expirationTimeStamp;
	}
}
