package eu.jsparrow.registration.validation;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Instant;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import eu.jsparrow.license.api.exception.ValidationException;
import eu.jsparrow.registration.helper.DummyRegistrationModel;
import eu.jsparrow.registration.helper.JsonHelper;

@SuppressWarnings("nls")
public class RegisterValidationTest {
	
	private RegisterValidation registerValidation;
	private RegisterRequest registerRequest;
	private ResponseParser response;
	
	@Rule
	public ExpectedException expectedException = ExpectedException.none(); 
	
	
	@Before
	public void setUp() {
		registerRequest = mock(RegisterRequest.class);
		response = new ResponseParser();
		registerValidation = new RegisterValidation(registerRequest, response);
	}
	
	/*
	 * Register
	 */
	
	@Test
	public void register_successfulRegistration_shouldReturnTrue() throws Exception {
		DummyRegistrationModel model = new DummyRegistrationModel();
		when(registerRequest.sendRegisterRequest(model)).thenReturn(JsonHelper.getRegisterResponseBody(true, "SUCCESS"));
		
		boolean successful = registerValidation.register(model);
		
		assertTrue(successful);
	}
	
	@Test
	public void register_failedregistration_shouldReturnFalse() throws Exception {
		DummyRegistrationModel model = new DummyRegistrationModel();
		when(registerRequest.sendRegisterRequest(model)).thenReturn(JsonHelper.getRegisterResponseBody(false, "FAIL"));
		
		boolean successful = registerValidation.register(model);
		
		assertFalse(successful);
	}
	
	@Test
	public void register_invalidResponseBody_shouldThrowException() throws Exception {
		DummyRegistrationModel model = new DummyRegistrationModel();
		when(registerRequest.sendRegisterRequest(model)).thenReturn("invlaid-response-body");
		
		expectedException.expect(ValidationException.class);
		registerValidation.register(model);
		
		fail();
	}
	
	/*
	 * Activate
	 */
	
	@Test 
	public void activate_successfulActivation_shouldReturnTrue() throws Exception {
		String activationKey = "activation-key"; 
		String responseBody = JsonHelper.getActivationResponseBody(true, Instant.now(), "SUCCESS");
		when(registerRequest.sendActivateRequest(activationKey)).thenReturn(responseBody);
		
		boolean successful = registerValidation.activate(activationKey);
		
		assertTrue(successful);
	}
	
	@Test 
	public void activate_failedActivation_shouldReturnFalse() throws Exception {
		String activationKey = "activation-key"; 
		String responseBody = JsonHelper.getActivationResponseBody(false, Instant.now(), "FAIL");
		when(registerRequest.sendActivateRequest(activationKey)).thenReturn(responseBody);
		
		boolean successful = registerValidation.activate(activationKey);
		
		assertFalse(successful);
	}
	
	@Test 
	public void activate_invalidResponseBody_shouldThrowValidationException() throws Exception {
		String activationKey = "activation-key"; 
		String responseBody = "invalid-response-body";
		when(registerRequest.sendActivateRequest(activationKey)).thenReturn(responseBody);
		
		expectedException.expect(ValidationException.class);
		registerValidation.activate(activationKey);
		
		fail();
	}
	
	/*
	 * Validate
	 */
	
	@Test
	public void validate_validRegistration_shouldReturnTrue() {
		String hardwareId = "secret";
		String expectedSecret = "secret";
		
		boolean valid = registerValidation.validate(hardwareId, expectedSecret);
		
		assertTrue(valid);
	}
	
	@Test
	public void validate_emptyKey_shouldReturnFalse() {
		String hardwareId = "";
		String expectedSecret = "secret";
		
		boolean valid = registerValidation.validate(hardwareId, expectedSecret);
		
		assertFalse(valid);
	}
	
	@Test
	public void validate_secretMisMatch_shouldReturnFalse() {
		String hardwareId = "secret";
		String expectedSecret = "";
		
		boolean valid = registerValidation.validate(hardwareId, expectedSecret);
		
		assertFalse(valid);
	}

}
