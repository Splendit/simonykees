package eu.jsparrow.registration.validation;

import java.io.IOException;
import java.lang.invoke.MethodHandles;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

	private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup()
		.lookupClass());

	private ObjectMapper mapper = new ObjectMapper();

	public RegisterResponse parseRegisterResponse(String responseBody) throws IOException {
		RegisterResponse response = mapper.readValue(responseBody, RegisterResponse.class);
		logger.debug("Parsed registration response '{}'", response); //$NON-NLS-1$
		return response;
	}

	public ActivateResponse parseActivateResponse(String responseBody) throws IOException {
		ActivateResponse response = mapper.readValue(responseBody, ActivateResponse.class);
		logger.debug("Parsed activation response '{}'", response); //$NON-NLS-1$
		return response;
	}

}
