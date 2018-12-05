package eu.jsparrow.registration.validation;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import eu.jsparrow.registration.helper.DummyRegistrationModel;

public class RegisterValidationTest {
	
	private RegisterValidation registerValidation;
	
	@Before
	public void setUp() {
		this.registerValidation = new RegisterValidation();
	}
	
	@Test
	public void validate_validRegistration_shouldReturnTrue() {
		String expectedKey = "expected-key";
		String expectedSecret = "expected-secret";
		DummyRegistrationModel model = new DummyRegistrationModel(expectedKey, expectedSecret);
		
		boolean valid = registerValidation.validate(model, expectedSecret);
		
		assertTrue(valid);
	}
	
	@Test
	public void validate_emptyKey_shouldReturnFalse() {
		String expectedKey = "";
		String expectedSecret = "expected-secret";
		DummyRegistrationModel model = new DummyRegistrationModel(expectedKey, expectedSecret);
		
		boolean valid = registerValidation.validate(model, expectedSecret);
		
		assertFalse(valid);
	}
	
	@Test
	public void validate_secretMisMatch_shouldReturnFalse() {
		String expectedKey = "expected-key";
		String expectedSecret = "expected-secret";
		DummyRegistrationModel model = new DummyRegistrationModel(expectedKey, expectedSecret);
		
		boolean valid = registerValidation.validate(model, "mismatching-secret");
		
		assertFalse(valid);
	}

}
