package eu.jsparrow.registration.validation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import eu.jsparrow.license.api.exception.ValidationException;
import eu.jsparrow.registration.model.RegistrationModel;

/**
 * Provides functionality for sending HTTP requests to the validation end-point.
 * 
 * @since 3.0.0
 *
 */
public class RegisterRequest {

	/*
	 * Should the api end-point change the location/name, the following fields
	 * need to be changed too.
	 */
	private static final String REGISTER_API_ENDPOINT = "https://1k4wn56lwd.execute-api.eu-central-1.amazonaws.com/Prod/starter/create"; //$NON-NLS-1$
	private static final String ACTIVATE_API_ENDPOINT = "https://1k4wn56lwd.execute-api.eu-central-1.amazonaws.com/Prod/starter/activate"; //$NON-NLS-1$

	private static final String ACTIVATION_KEY = "activationKey"; //$NON-NLS-1$
	private ObjectMapper objectMapper = new ObjectMapper();

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

	public String sendActivateRequest(String activationKey) throws ValidationException {
		String json = toJson(ACTIVATION_KEY, activationKey);
		return httpClientWrapper.post(json, ACTIVATE_API_ENDPOINT);
	}

	private String toJson(RegistrationModel model) throws ValidationException {
		String json;
		try {
			json = objectMapper.writeValueAsString(model);
		} catch (JsonProcessingException e) {
			String message = String.format("Failed to serialize model %s to json", model); //$NON-NLS-1$
			throw new ValidationException(message);
		}
		return json;
	}

	private String toJson(String key, String value) {
		ObjectNode node = objectMapper.createObjectNode();
		node.put(key, value);
		return node.toString();
	}
}
