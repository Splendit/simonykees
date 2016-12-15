package at.splendit.simonykees.core.license;

import static org.junit.Assert.*;

import java.time.Instant;
import java.time.ZonedDateTime;

import org.junit.Test;

public class PersistenceManagerTest {
	
	@Test
	public void encryptDecryptPersistenceModel() {
		// having an instance of a PersistenceManager together with a PersistenceModel...
		PersistenceManager persistenceMng = PersistenceManager.getInstance();
		PersistenceModel orgModel = new PersistenceModel(
				"test-number", 
				"test-name", 
				true, 
				LicenseType.TRY_AND_BUY, 
				Instant.now().minusSeconds(30),
				ZonedDateTime.now().plusDays(4), 
				ZonedDateTime.now().plusHours(4), 
				ZonedDateTime.now().plusDays(350), 
				true);
		persistenceMng.setPersistenceModel(orgModel);
		
		// when persisting the data and reading it back...
		persistenceMng.persist();
		PersistenceModel decModel = persistenceMng.readPersistedData();
		
		// expecting the same data as original...
		assertEquals(orgModel.getLicenseeName(), decModel.getLicenseeName());
		assertEquals(orgModel.getLicenseeNumber(), decModel.getLicenseeNumber());
		assertEquals(orgModel.getDemoExpirationDate(), decModel.getDemoExpirationDate());
		assertEquals(orgModel.getExpirationTimeStamp(), decModel.getExpirationTimeStamp());
		assertEquals(orgModel.getLastValidationStatus(), decModel.getLastValidationStatus());
		assertEquals(orgModel.getLastValidationTimestamp(), decModel.getLastValidationTimestamp());
		assertEquals(orgModel.getLicenseType(), decModel.getLicenseType());
		assertEquals(orgModel.getSubscriptionExpirationDate(), decModel.getSubscriptionExpirationDate());
		assertEquals(orgModel.getSubscriptionStatus(), decModel.getSubscriptionStatus());
	}
	
	@Test
	public void encryptDecryptWithMissingData() {
		// having an instance of a PersistenceManager together with a PersistenceModel...
		PersistenceManager persistenceMng = PersistenceManager.getInstance();
		PersistenceModel orgModel = new PersistenceModel(
				"", 
				null, 
				true, 
				null, 
				Instant.now().minusSeconds(30),
				null, 
				ZonedDateTime.now().plusHours(4), 
				null, 
				true);
		persistenceMng.setPersistenceModel(orgModel);
		
		// when persisting the data and reading it back...
		persistenceMng.persist();
		PersistenceModel decModel = persistenceMng.readPersistedData();
		
		// expecting the same data as original...
		assertEquals(orgModel.getLicenseeName(), decModel.getLicenseeName());
		assertEquals(orgModel.getLicenseeNumber(), decModel.getLicenseeNumber());
		assertEquals(orgModel.getDemoExpirationDate(), decModel.getDemoExpirationDate());
		assertEquals(orgModel.getExpirationTimeStamp(), decModel.getExpirationTimeStamp());
		assertEquals(orgModel.getLastValidationStatus(), decModel.getLastValidationStatus());
		assertEquals(orgModel.getLastValidationTimestamp(), decModel.getLastValidationTimestamp());
		assertEquals(orgModel.getLicenseType(), decModel.getLicenseType());
		assertEquals(orgModel.getSubscriptionExpirationDate(), decModel.getSubscriptionExpirationDate());
		assertEquals(orgModel.getSubscriptionStatus(), decModel.getSubscriptionStatus());
	}
}
