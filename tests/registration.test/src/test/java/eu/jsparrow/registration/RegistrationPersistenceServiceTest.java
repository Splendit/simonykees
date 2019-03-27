package eu.jsparrow.registration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.registration.persistence.RegistrationSecureStoragePersistence;

public class RegistrationPersistenceServiceTest {
	
	private RegistrationPersistenceService persistenceService;
	private RegistrationSecureStoragePersistence registrationPersistence;
	
	@BeforeEach
	public void setUp() {
		registrationPersistence = mock(RegistrationSecureStoragePersistence.class);
		persistenceService = new RegistrationPersistenceService(registrationPersistence);
	}
	
	@Test
	public void loadFromPersistence_validKey_shouldReturnStoredValue() throws Exception {
		String expectedStoredValue = "expected-value"; //$NON-NLS-1$
		when(registrationPersistence.load()).thenReturn(expectedStoredValue);
		
		String value = persistenceService.loadFromPersistence();
		
		verify(registrationPersistence).load();
		assertEquals(expectedStoredValue, value);
	}

	@Test
	public void save_validKey_shouldInvokeSave() throws Exception {
		String expectedStoredValue = "expected-value"; //$NON-NLS-1$
		when(registrationPersistence.load()).thenReturn(expectedStoredValue);
		
		persistenceService.saveToPersistence(expectedStoredValue);
		
		verify(registrationPersistence).save(expectedStoredValue);
	}

}
