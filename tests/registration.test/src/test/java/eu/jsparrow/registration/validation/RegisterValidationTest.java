package eu.jsparrow.registration.validation;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

public class RegisterValidationTest {
	
	private RegisterValidation registerValidation;
	
	@Before
	public void setUp() {
		this.registerValidation = new RegisterValidation();
	}
	
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
