package at.splendit.simonykees.core.license;

import static org.junit.Assert.*;

import java.time.Instant;
import java.time.ZonedDateTime;

import org.junit.Test;

import at.splendit.simonykees.core.license.model.PersistenceModel;

public class PersistenceManagerTest {
	
	@Test
	public void encryptDecryptPersistenceModel() {
		// having an instance of a PersistenceManager together with a PersistenceModel...
		PersistenceManager persistenceMng = PersistenceManager.getInstance();
		Instant nowMin30sec = Instant.now().minusSeconds(30);
		ZonedDateTime demoExpireDate  = ZonedDateTime.now().plusDays(4);
		ZonedDateTime expireTimeStamp = ZonedDateTime.now().plusHours(4);
		ZonedDateTime subscriptionExpires = ZonedDateTime.now().plusDays(350);
		PersistenceModel orgModel = new PersistenceModel(
				"test-number", 
				"test-name", 
				true, 
				LicenseType.TRY_AND_BUY, 
				nowMin30sec,
				demoExpireDate, 
				expireTimeStamp, 
				subscriptionExpires, 
				true);
		persistenceMng.setPersistenceModel(orgModel);
		
		// when persisting the data and reading it back...
		persistenceMng.persist();
		PersistenceModel decModel = persistenceMng.readPersistedData().orElse(null);
		assertNotNull(decModel);
		
		// expecting the same data as original...
		assertEquals("test-name", decModel.getLicenseeName().orElse(""));
		assertEquals("test-number", decModel.getLicenseeNumber().orElse(""));
		assertEquals(demoExpireDate, decModel.getDemoExpirationDate().orElse(null));
		assertEquals(expireTimeStamp, decModel.getExpirationTimeStamp().orElse(null));
		assertEquals(true, decModel.getLastValidationStatus().orElse(false));
		assertEquals(nowMin30sec, decModel.getLastValidationTimestamp().orElse(null));
		assertEquals(LicenseType.TRY_AND_BUY, decModel.getLicenseType().orElse(null));
		assertEquals(subscriptionExpires, decModel.getSubscriptionExpirationDate().orElse(null));
		assertEquals(true, decModel.getSubscriptionStatus().orElse(false));
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
		PersistenceModel decModel = persistenceMng.readPersistedData().orElse(null);
		assertNotNull(decModel);
		
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
