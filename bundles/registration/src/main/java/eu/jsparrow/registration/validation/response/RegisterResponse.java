package eu.jsparrow.registration.validation.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Represents the expected response body of the registration request.
 * 
 * @since 3.0.0
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class RegisterResponse {

	private boolean created;
	private String message;

	public RegisterResponse() {

	}

	public RegisterResponse(boolean created, String message) {
		this.created = created;
		this.message = message;
	}

	public boolean isCreated() {
		return created;
	}

	public String getMessage() {
		return message;
	}

	@Override
	public String toString() {
		return String.format("RegisterResponse [created=%s, message=%s ]", created, message); //$NON-NLS-1$
	}

}
