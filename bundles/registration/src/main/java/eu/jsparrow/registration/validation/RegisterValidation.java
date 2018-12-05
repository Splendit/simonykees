package eu.jsparrow.registration.validation;

import eu.jsparrow.license.api.exception.ValidationException;
import eu.jsparrow.registration.model.CustomerRegistrationModel;

/**
 * Handles creating validation requests and response parsing for customer registrations. 
 * 
 * @since 3.0.0
 *
 */
public class RegisterValidation {
	
	private RegisterRequest registerRequest;
	
	public RegisterValidation() {
		this.registerRequest = new RegisterRequest();
	}

	public boolean register(CustomerRegistrationModel model) throws ValidationException {
		String response = registerRequest.sendRegisterRequest(model);
		return evaluateRegisterResponse(response);
	}

	public boolean activate(String activationKey) throws ValidationException {
		String response = registerRequest.sendActivateRequest(activationKey);
		return evaluateActivateResponse(response);
	}
	
	public boolean validate(String hardwareId, String secret) {
		return secret.equals(hardwareId);
	}


	private boolean evaluateActivateResponse(String response) {
		return false;
	}
	
	private boolean evaluateRegisterResponse(String response) {
		return false;
	}

}
