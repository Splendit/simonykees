package eu.jsparrow.registration.integration;


import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import eu.jsparrow.license.api.exception.PersistenceException;
import eu.jsparrow.registration.RegistrationPersistenceService;

@Disabled
public class CustomerRegistrationPersistenceServiceIT {
	
	private RegistrationPersistenceService persistenceService;
	
	@BeforeEach
	public void setUp() {
		persistenceService = new RegistrationPersistenceService();
	}
	
	@Test
	public void saveSecret() throws PersistenceException {
		String sampleSecret = "sample-secret"; //$NON-NLS-1$
		
		persistenceService.saveToPersistence(sampleSecret);
		
		String persistedModel = persistenceService.loadFromPersistence();
		assertEquals(sampleSecret, persistedModel);
	}
}
