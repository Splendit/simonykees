package eu.jsparrow.registration.validation;

import java.io.IOException;

import eu.jsparrow.license.api.exception.ValidationException;
import eu.jsparrow.registration.model.RegistrationModel;
import eu.jsparrow.registration.validation.response.RegisterResponse;

/**
 * Handles creating validation requests and response parsing for customer
 * registrations.
 * 
 * @since 3.0.0
 *
 */
public class RegisterValidation {

	private RegisterRequest registerRequest;
	private ResponseParser response;

	public RegisterValidation() {
		this.registerRequest = new RegisterRequest();
		this.response = new ResponseParser();
	}

	public boolean register(RegistrationModel model) throws ValidationException {
		String responseBody = registerRequest.sendRegisterRequest(model);
		return evaluateRegisterResponse(responseBody);
	}

	public boolean activate(String activationKey) throws ValidationException {
		String responseBody = registerRequest.sendActivateRequest(activationKey);
		return evaluateActivateResponse(responseBody);
	}

	public boolean validate(String hardwareId, String secret) {
		return secret.equals(hardwareId);
	}

	private boolean evaluateActivateResponse(String activateResponseBody) throws ValidationException {
		try {
			RegisterResponse activateResponse = response.parseRegisterResponse(activateResponseBody);
			// TODO: check the values in the parsed body SIM-1360
		} catch (IOException e) {
			throw new ValidationException("Cannot parse registration response body", e);

		}
		return true;
	}

	private boolean evaluateRegisterResponse(String registerResponseBody) throws ValidationException {
		try {
			RegisterResponse registerResponse = response.parseRegisterResponse(registerResponseBody);
			// TODO: check the values in the parsed body SIM-1360
		} catch (IOException e) {
			throw new ValidationException("Cannot parse registration response body", e);

		}
		return true;
	}

}
