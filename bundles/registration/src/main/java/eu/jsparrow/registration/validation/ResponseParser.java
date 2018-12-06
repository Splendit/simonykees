package eu.jsparrow.registration.validation;

import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;

import eu.jsparrow.registration.validation.response.ActivateResponse;
import eu.jsparrow.registration.validation.response.RegisterResponse;

/**
 * Parses the response body of activate and register request. 
 * 
 * @since 3.0.0
 *
 */
public class ResponseParser {

	private ObjectMapper mapper = new ObjectMapper();

	public RegisterResponse parseRegisterResponse(String responseBody) throws IOException {
		RegisterResponse registerResponse = mapper.readValue(responseBody, RegisterResponse.class);
		return registerResponse;

	}

	public ActivateResponse parseActivateResponse(String responseBody) throws IOException {
		ActivateResponse activateResponse = mapper.readValue(responseBody, ActivateResponse.class);
		return activateResponse;
	}

}
