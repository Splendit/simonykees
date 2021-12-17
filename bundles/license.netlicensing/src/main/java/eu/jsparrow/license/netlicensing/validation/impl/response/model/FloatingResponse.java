package eu.jsparrow.license.netlicensing.validation.impl.response.model;

import java.time.ZonedDateTime;

import com.labs64.netlicensing.domain.vo.Composition;
import com.labs64.netlicensing.domain.vo.ValidationResult;

/**
 * Represents the information contained in the {@link Composition} corresponding
 * to a Floating Product Module of a NetLicensing' {@link ValidationResult}.
 * 
 * @see <a href=
 *      "https://netlicensing.io/wiki/floating">Floating
 *      License Model</a>
 *
 */
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
