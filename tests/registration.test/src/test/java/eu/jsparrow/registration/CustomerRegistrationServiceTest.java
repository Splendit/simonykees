package eu.jsparrow.registration;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import eu.jsparrow.license.api.exception.ValidationException;
import eu.jsparrow.registration.validation.RegisterValidator;

@SuppressWarnings("nls")
public class CustomerRegistrationServiceTest {
	
	private CustomerRegistrationService service;
	private RegisterValidator registrationValidation;

	@BeforeEach
	public void setUp() {
		this.registrationValidation = Mockito.mock(RegisterValidator.class);
		this.service = new CustomerRegistrationService(registrationValidation);
	}
	
	@Test
	public void activate_validKey_shouldReturnTrue() throws Exception {
		String activationKey = "sample-activation-key";
		when(registrationValidation.activate(activationKey)).thenReturn(true);
		
		boolean success = service.activate(activationKey);
		
		assertTrue(success);
	}
	
	@Test
	public void activate_unavailableService_shouldThrowValidationException() throws Exception {
		String activationKey = "sample-activation-key";
		when(registrationValidation.activate(activationKey)).thenThrow(ValidationException.class);
		
		assertThrows(ValidationException.class, () -> service.activate(activationKey));
	}
	
	@Test
	public void register_successfulRegistration_shouldReturnTrue() throws Exception {
		when(registrationValidation.register(any())).thenReturn(true);
		
		boolean success = service.register("mail", "first-name", "last-name", "company", true);
		
		assertTrue(success);
	}
	
	@Test
	public void register_unavailableService_shouldThrowException() throws Exception {
		when(registrationValidation.register(any())).thenThrow(ValidationException.class);
		
		assertThrows(ValidationException.class, () -> service.register("mail", "first-name", "last-name", "company", true));
	}
	
	@Test
	public void validate_shouldInvokeRegistrationValidate() {
		String hardwareId = "hardware-id";
		String attachedSecret = "attached-secret";
		
		service.validate(hardwareId, attachedSecret);
		
		Mockito.verify(registrationValidation).validate(eq(hardwareId), eq(attachedSecret));
	}
}
