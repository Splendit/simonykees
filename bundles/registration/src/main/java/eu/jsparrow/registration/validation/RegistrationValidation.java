package eu.jsparrow.registration.validation;

import eu.jsparrow.license.api.RegistrationModel;

public class RegistrationValidation {
	
	private Request request = new Request();

	public boolean register(RegistrationModel model) {
		String response = request.sendRegisterRequest(model);
		return evaluateRegisterResponse(response);
	}

	public boolean activate(RegistrationModel model) {
		String response = request.sendActivateRequest(model);
		return evaluateActivateResponse(response);
	}
	
	public boolean validate(RegistrationModel model) {
		String response = request.sendValidateRequest(model);
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
