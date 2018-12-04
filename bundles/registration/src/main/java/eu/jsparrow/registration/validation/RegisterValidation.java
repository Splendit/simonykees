package eu.jsparrow.registration.validation;

import eu.jsparrow.license.api.RegistrationModel;
import eu.jsparrow.license.api.exception.ValidationException;

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

	public boolean register(RegistrationModel model) throws ValidationException {
		String response = registerRequest.sendRegisterRequest(model);
		return evaluateRegisterResponse(response);
	}

	public boolean activate(RegistrationModel model) throws ValidationException {
		String response = registerRequest.sendActivateRequest(model);
		return evaluateActivateResponse(response);
	}
	
	public boolean validate(RegistrationModel model) throws ValidationException {
		String response = registerRequest.sendValidateRequest(model);
		return evaluateValidateResponse(response);
	}


	private boolean evaluateActivateResponse(String response) {
		return false;
	}

	private boolean evaluateValidateResponse(String response) {
		return false;
	}
	
	private boolean evaluateRegisterResponse(String response) {
		return false;
	}

}
