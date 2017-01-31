package at.splendit.simonykees.core.license;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.time.Instant;
import java.time.ZonedDateTime;

import org.junit.Test;

import at.splendit.simonykees.core.license.model.PersistenceModel;

/**
 * Testing persistence manager. 
 * 
 * @author Ardit Ymeri
 * @since 1.0
 *
 */
@SuppressWarnings("nls")
public class PersistenceManagerTest extends LicenseCommonTest {
	
	@Test
	public void encryptDecryptPersistenceModel() {
		// having an instance of a PersistenceManager together with a PersistenceModel...
		PersistenceManager persistenceMng = PersistenceManager.getInstance();
		Instant nowMin30sec = Instant.now().minusSeconds(30);
		Instant nowMin35sec = Instant.now().minusSeconds(35);
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
				true,
				nowMin35sec, 
				LicenseType.NODE_LOCKED);
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
		assertEquals(nowMin35sec, decModel.getLastSuccessTimestamp().orElse(null));
		assertEquals(LicenseType.NODE_LOCKED, decModel.getLastSuccessLicenseType().orElse(null));
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
				true, 
				null, 
				null);
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
	
	@Test
	public void validateNodeLocked() {
		// having stored a valid persistence model of node locked license...
		PersistenceManager persistenceMng = PersistenceManager.getInstance();
		Instant nowMin30sec = Instant.now().minusSeconds(30);
		Instant nowMin35sec = Instant.now().minusSeconds(35);
		ZonedDateTime demoExpireDate  = ZonedDateTime.now().plusDays(4);
		ZonedDateTime subscriptionExpires = ZonedDateTime.now().plusDays(350);
		PersistenceModel orgModel = new PersistenceModel(
				"test-number", 
				"test-name", 
				true,  // last validation status
				LicenseType.NODE_LOCKED, // license type
				nowMin30sec, // validation time stamp
				demoExpireDate, 
				null,  // expiration time stamp
				subscriptionExpires, 
				true, // last subscription status
				nowMin35sec, // last success timestamp
				LicenseType.NODE_LOCKED // last success type 
				); 
		persistenceMng.setPersistenceModel(orgModel);
		persistenceMng.persist();
		
		// when validating using the persisted data...
		LicenseChecker checker = persistenceMng.vlidateUsingPersistedData();
		
		// expecting license to be valid...
		assertTrue(checker.isValid());
		assertEquals(LicenseStatus.CONNECTION_FAILURE, checker.getLicenseStatus());
		assertEquals(LicenseType.NODE_LOCKED, checker.getType());
		assertEquals(subscriptionExpires, checker.getExpirationDate());
		assertEquals("test-name", checker.getLicenseeName());
		assertEquals(nowMin30sec, checker.getValidationTimeStamp());
	}
	
	@Test
	public void validateFloating() {
		// having stored a valid persistence model of some floating license...
		PersistenceManager persistenceMng = PersistenceManager.getInstance();
		Instant nowMin30sec = Instant.now().minusSeconds(30);
		Instant nowMin35sec = Instant.now().minusSeconds(35);
		ZonedDateTime demoExpireDate  = ZonedDateTime.now().plusDays(4);
		ZonedDateTime subscriptionExpires = ZonedDateTime.now().plusDays(350);
		PersistenceModel orgModel = new PersistenceModel(
				"test-number", 
				"floating test-name", 
				true,  // last validation status
				LicenseType.FLOATING, // license type
				nowMin30sec, // validation time stamp
				demoExpireDate, 
				null,  // expiration time stamp
				subscriptionExpires, 
				true, // last subscription status
				nowMin35sec, 
				LicenseType.FLOATING);
		persistenceMng.setPersistenceModel(orgModel);
		persistenceMng.persist();
		
		// when validating using the persisted data...
		LicenseChecker checker = persistenceMng.vlidateUsingPersistedData();
		
		// expecting license to be valid...
		assertTrue(checker.isValid());
		assertEquals(LicenseStatus.CONNECTION_FAILURE, checker.getLicenseStatus());
		assertEquals(LicenseType.FLOATING, checker.getType());
		assertEquals(subscriptionExpires, checker.getExpirationDate());
		assertEquals("floating test-name", checker.getLicenseeName());
		assertEquals(nowMin30sec, checker.getValidationTimeStamp());
	}
	
	@Test
	public void validateTryAndBuy() {
		// having stored a valid persistence model of some demo license...
		PersistenceManager persistenceMng = PersistenceManager.getInstance();
		Instant nowMin30sec = Instant.now().minusSeconds(30);
		Instant nowMin35sec = Instant.now().minusSeconds(35);
		ZonedDateTime demoExpireDate  = ZonedDateTime.now().plusDays(4);
		ZonedDateTime subscriptionExpires = ZonedDateTime.now().plusDays(350);
		PersistenceModel orgModel = new PersistenceModel(
				"test-number", 
				"floating test-name", 
				true,  // last validation status
				LicenseType.TRY_AND_BUY, // license type
				nowMin30sec, // validation time stamp
				demoExpireDate, 
				null,  // expiration time stamp
				subscriptionExpires, 
				true, // last subscription status
				nowMin35sec, 
				LicenseType.TRY_AND_BUY);
		persistenceMng.setPersistenceModel(orgModel);
		persistenceMng.persist();
		
		// when validating using the persisted data...
		LicenseChecker checker = persistenceMng.vlidateUsingPersistedData();
		
		// expecting license to be valid...
		assertTrue(checker.isValid());
		assertEquals(LicenseStatus.CONNECTION_FAILURE, checker.getLicenseStatus());
		assertEquals(LicenseType.TRY_AND_BUY, checker.getType());
		assertEquals(demoExpireDate, checker.getExpirationDate());
		assertEquals("floating test-name", checker.getLicenseeName());
		assertEquals(nowMin30sec, checker.getValidationTimeStamp());
	}
	
	@Test
	public void validateTryAndBuyExiredDemo() {
		// having stored an expired persistence model of some demo license...
		PersistenceManager persistenceMng = PersistenceManager.getInstance();
		Instant nowMin1300sec = Instant.now().minusSeconds(1300);
		Instant nowMin1350sec = Instant.now().minusSeconds(1350);
		ZonedDateTime demoExpireDate  = ZonedDateTime.now();
		ZonedDateTime subscriptionExpires = null;
		PersistenceModel orgModel = new PersistenceModel(
				"test-number", 
				"floating test-name", 
				true,  // last validation status
				LicenseType.TRY_AND_BUY, // license type
				nowMin1300sec, // validation time stamp
				demoExpireDate, 
				null,  // expiration time stamp
				subscriptionExpires, 
				true, // last subscription status
				nowMin1350sec, 
				LicenseType.TRY_AND_BUY);
		persistenceMng.setPersistenceModel(orgModel);
		persistenceMng.persist();
		
		// when validating using the persisted data...
		LicenseChecker checker = persistenceMng.vlidateUsingPersistedData();
		
		// expecting license to NOT be valid...
		assertFalse(checker.isValid());
		assertEquals(LicenseStatus.CONNECTION_FAILURE, checker.getLicenseStatus());
		assertEquals(LicenseType.TRY_AND_BUY, checker.getType());
		assertEquals(demoExpireDate, checker.getExpirationDate());
		assertEquals("floating test-name", checker.getLicenseeName());
		assertEquals(nowMin1300sec, checker.getValidationTimeStamp());
	}
	
	@Test
	public void validateExpiredValidationTimestamp() {
		// having stored a PersistedModel with an expired validation time-stamp...
		PersistenceManager persistenceMng = PersistenceManager.getInstance();
		Instant nowMin3601sec = Instant.now().minusSeconds(3601);
		Instant nowMin3605sec = Instant.now().minusSeconds(3605);
		ZonedDateTime demoExpireDate  = ZonedDateTime.now().plusDays(4);
		ZonedDateTime subscriptionExpires = ZonedDateTime.now().plusDays(350);
		PersistenceModel orgModel = new PersistenceModel(
				"test-number", 
				"test-name", 
				true,  // last validation status
				LicenseType.NODE_LOCKED, // license type
				nowMin3601sec, // validation time-stamp, expired 1 second ago
				demoExpireDate, 
				null,  // expiration time stamp
				subscriptionExpires, 
				true, // last subscription status
				nowMin3605sec, 
				LicenseType.NODE_LOCKED);
		persistenceMng.setPersistenceModel(orgModel);
		persistenceMng.persist();
		
		// when validating using the persisted data...
		LicenseChecker checker = persistenceMng.vlidateUsingPersistedData();
		
		// expecting license to NOT be valid...
		assertFalse(checker.isValid());
		assertEquals(LicenseStatus.CONNECTION_FAILURE, checker.getLicenseStatus());
		assertEquals(LicenseType.NODE_LOCKED, checker.getType());
		assertEquals(subscriptionExpires, checker.getExpirationDate());
		assertEquals("test-name", checker.getLicenseeName());
		assertEquals(nowMin3601sec, checker.getValidationTimeStamp());
	}
	
	@Test
	public void validateExpiredSubscription() {
		// having stored a PersistedModel with an expired subscription date...
		PersistenceManager persistenceMng = PersistenceManager.getInstance();
		Instant nowMin1000sec = Instant.now().minusSeconds(1000);
		Instant nowMin1005sec = Instant.now().minusSeconds(1005);
		ZonedDateTime demoExpireDate  = ZonedDateTime.now().plusDays(4);
		ZonedDateTime subscriptionExpires = ZonedDateTime.now();
		PersistenceModel orgModel = new PersistenceModel(
				"test-number", 
				"test-name", 
				true,  // last validation status
				LicenseType.NODE_LOCKED, // license type
				nowMin1000sec, // validation time stamp
				demoExpireDate, 
				null,  // expiration time stamp
				subscriptionExpires, 
				true, // last subscription status
				nowMin1005sec, 
				LicenseType.NODE_LOCKED);
		persistenceMng.setPersistenceModel(orgModel);
		persistenceMng.persist();
		
		// when validating using the persisted data...
		LicenseChecker checker = persistenceMng.vlidateUsingPersistedData();
		
		// expecting license to NOT be valid...
		assertFalse(checker.isValid());
		assertEquals(LicenseStatus.CONNECTION_FAILURE, checker.getLicenseStatus());
		assertEquals(LicenseType.NODE_LOCKED, checker.getType());
		assertEquals(subscriptionExpires, checker.getExpirationDate());
		assertEquals("test-name", checker.getLicenseeName());
		assertEquals(nowMin1000sec, checker.getValidationTimeStamp());
	}
}
