package eu.jsparrow.registration.integration;

import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import eu.jsparrow.license.api.exception.ValidationException;
import eu.jsparrow.registration.CustomerRegistrationService;

@SuppressWarnings("nls")
@Ignore
public class CustomerRegistrationServiceIT {

	private CustomerRegistrationService customerRegistrationService;

	@Before
	public void setUp() {
		customerRegistrationService = new CustomerRegistrationService();
	}

	@Test
	public void register_validRegistration_shouldReturnTrue() throws ValidationException {
		boolean success = customerRegistrationService.register("sample@mail.com", "First Name", "Last Name", "Company",
				true);
		assertTrue(success);
	}

	@Test
	public void register_validActivation_shouldReturnTrue() throws ValidationException {
		/*
		 * This test will fail as soon as the mock response in aws api is
		 * replaced with the actual service
		 */
		boolean success = customerRegistrationService.activate("valid-key");
		assertTrue(success);
	}

}
