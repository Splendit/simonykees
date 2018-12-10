package eu.jsparrow.registration;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import eu.jsparrow.license.api.exception.PersistenceException;

@Ignore
public class IntegrationSampleTest {
	
	private CustomerRegistrationPersistenceService persistenceService;
	
	@Before
	public void setUp() {
		persistenceService = new CustomerRegistrationPersistenceService();
	}
	
	@Test
	public void saveSecret() throws PersistenceException {
		String sampleSecret = "sample-secret"; //$NON-NLS-1$
		
		persistenceService.saveToPersistence(sampleSecret);
		
		String persistedModel = persistenceService.loadFromPersistence();
		assertEquals(sampleSecret, persistedModel);
	}
}
