package eu.jsparrow.registration.validation.response;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Represents the response body of the activation request.
 * 
 * @since 3.0.0
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ActivateResponse {

	private boolean active;
	private Instant activatonTimestamp;
	private String message;

	public ActivateResponse() {

	}

	public ActivateResponse(boolean active, String activationTimestamp, String message) {
		this.active = active;
		this.activatonTimestamp = Instant.parse(activationTimestamp);
		this.message = message;
	}

	public boolean isActive() {
		return active;
	}

	public String getActivationTimestamp() {
		return activatonTimestamp.toString();
	}

	public String getMessage() {
		return message;
	}
	
	public void setActivationTimestamp(String activationTimestamp) {
		this.activatonTimestamp = Instant.parse(activationTimestamp);
	}

	@Override
	public String toString() {
		return String.format("ActivateResponse [active=%s, activatonTimestamp=%s, message=%s]", isActive(), //$NON-NLS-1$
				getActivationTimestamp(), getMessage());
	}
}
