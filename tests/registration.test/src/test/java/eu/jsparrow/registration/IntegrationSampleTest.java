package eu.jsparrow.registration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import eu.jsparrow.license.api.RegistrationModel;
import eu.jsparrow.license.api.exception.PersistenceException;

public class IntegrationSampleTest {
	
	private CustomerRegistrationPersistenceService persistenceService;
	private CustomerRegistrationModelFactoryService modelFactory;
	
	@Before
	public void setUp() {
		persistenceService = new CustomerRegistrationPersistenceService();
		modelFactory = new CustomerRegistrationModelFactoryService();
	}

	@Test
	public void saveDefaultRegistration() throws PersistenceException {
		RegistrationModel model = modelFactory.createRegistrationModel();
		persistenceService.saveToPersistence(model);
		
		RegistrationModel persistedModel = persistenceService.loadFromPersistence();
		assertTrue(persistedModel.getKey().isEmpty());
		assertTrue(persistedModel.getEmail().isEmpty());
	}
	
	@Test
	public void saveCustomerRegistration() throws PersistenceException {
		RegistrationModel model = modelFactory.createRegistrationModel("sample-key", "sample@mail.com");
		persistenceService.saveToPersistence(model);
		
		RegistrationModel persistedModel = persistenceService.loadFromPersistence();
		assertEquals("sample-key", persistedModel.getKey());
		assertEquals("sample@mail.com", persistedModel.getEmail());
	}
}
