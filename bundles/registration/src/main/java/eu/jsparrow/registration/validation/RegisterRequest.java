package eu.jsparrow.registration.validation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import eu.jsparrow.license.api.RegistrationModel;
import eu.jsparrow.license.api.exception.ValidationException;

/**
 * Provides functionality for sending HTTP requests to the validation endpoint.
 * 
 * @since 3.0.0
 *
 */
public class RegisterRequest {

	private static final String REGISTER_API_ENDPOINT = "";
	private static final String ACTIVATE_API_ENDPOINT = "";
	private static final String VALIDATE_API_ENDPOINT = "";

	private HttpClientWrapper httpClientWrapper;

	public RegisterRequest() {
		this.httpClientWrapper = new HttpClientWrapper();
	}

	RegisterRequest(HttpClientWrapper httpClientWrapper) {
		this.httpClientWrapper = httpClientWrapper;
	}

	public String sendRegisterRequest(RegistrationModel model) throws ValidationException {
		String json = toJson(model);
		return httpClientWrapper.post(json, REGISTER_API_ENDPOINT);
	}

	public String sendActivateRequest(RegistrationModel model) throws ValidationException {
		String json = toJson(model);
		return httpClientWrapper.post(json, ACTIVATE_API_ENDPOINT);
	}

	public String sendValidateRequest(RegistrationModel model) throws ValidationException {
		String json = toJson(model);
		return httpClientWrapper.post(json, VALIDATE_API_ENDPOINT);
	}

	private String toJson(RegistrationModel model) throws ValidationException {
		ObjectMapper objectMapper = new ObjectMapper();
		String json;
		try {
			json = objectMapper.writeValueAsString(model);
		} catch (JsonProcessingException e) {
			String message = String.format("Failed to serialize model %s to json", model);
			throw new ValidationException(message);
		}
		return json;
	}
}
