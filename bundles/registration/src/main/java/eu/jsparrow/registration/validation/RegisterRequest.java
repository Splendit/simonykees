package eu.jsparrow.registration.validation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import eu.jsparrow.license.api.exception.ValidationException;
import eu.jsparrow.registration.model.CustomerRegistrationModel;

/**
 * Provides functionality for sending HTTP requests to the validation endpoint.
 * 
 * @since 3.0.0
 *
 */
public class RegisterRequest {

	private static final String REGISTER_API_ENDPOINT = ""; //TODO: to be implemented in SIM-1360
	private static final String ACTIVATE_API_ENDPOINT = ""; //TODO: to be implemented in SIM-1360
	
	
	private static final String ACTIVATION_KEY = "activation-key"; //$NON-NLS-1$
	private ObjectMapper objectMapper = new ObjectMapper();

	private HttpClientWrapper httpClientWrapper;

	public RegisterRequest() {
		this.httpClientWrapper = new HttpClientWrapper();
	}

	RegisterRequest(HttpClientWrapper httpClientWrapper) {
		this.httpClientWrapper = httpClientWrapper;
	}

	public String sendRegisterRequest(CustomerRegistrationModel model) throws ValidationException {
		String json = toJson(model);
		return httpClientWrapper.post(json, REGISTER_API_ENDPOINT);
	}

	public String sendActivateRequest(String activationKey) throws ValidationException {
		String json = toJson(ACTIVATION_KEY, activationKey);
		return httpClientWrapper.post(json, ACTIVATE_API_ENDPOINT);
	}

	private String toJson(CustomerRegistrationModel model) throws ValidationException {
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
		
		//TODO: create a JSON with the given key/value. The exact format to be implemented in SIM-1360
		return "";
	}
}
