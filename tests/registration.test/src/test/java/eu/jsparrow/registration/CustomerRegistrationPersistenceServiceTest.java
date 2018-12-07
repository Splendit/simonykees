package eu.jsparrow.registration;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;

import eu.jsparrow.registration.persistence.RegistrationSecureStoragePersistence;

public class CustomerRegistrationPersistenceServiceTest {
	
	private CustomerRegistrationPersistenceService persistenceService;
	private RegistrationSecureStoragePersistence registrationPersistence;
	
	@Before
	public void setUp() {
		registrationPersistence = mock(RegistrationSecureStoragePersistence.class);
		persistenceService = new CustomerRegistrationPersistenceService(registrationPersistence);
	}
	
	@Test
	public void loadFromPersistence_validKey_shouldReturnStoredValue() throws Exception {
		String expectedStoredValue = "expected-value";
		when(registrationPersistence.load()).thenReturn(expectedStoredValue);
		
		String value = persistenceService.loadFromPersistence();
		
		verify(registrationPersistence).load();
		assertEquals(expectedStoredValue, value);
	}

	@Test
	public void save_validKey_shouldInvokeSave() throws Exception {
		String expectedStoredValue = "expected-value";
		when(registrationPersistence.load()).thenReturn(expectedStoredValue);
		
		persistenceService.saveToPersistence(expectedStoredValue);
		
		verify(registrationPersistence).save(expectedStoredValue);
	}

}
