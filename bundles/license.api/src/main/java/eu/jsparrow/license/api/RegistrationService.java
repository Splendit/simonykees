package eu.jsparrow.license.api;

import eu.jsparrow.license.api.exception.ValidationException;

public interface RegistrationService {

	boolean register(String email, String firstName, String lastName, String company, boolean subscribe) throws ValidationException;

	boolean validate(String hardwareId, String secret);

	boolean activate(String activationKey) throws ValidationException;
}
