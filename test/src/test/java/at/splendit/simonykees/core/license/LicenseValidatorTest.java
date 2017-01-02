package at.splendit.simonykees.core.license;

import static org.junit.Assert.*;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Optional;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import at.splendit.simonykees.core.license.model.LicenseeModel;
import at.splendit.simonykees.core.license.model.NodeLockedModel;
import at.splendit.simonykees.core.license.model.PersistenceModel;

@SuppressWarnings("nls")
public class LicenseValidatorTest {

	private static final String NODE_LOCKED_LICENSEE_NUMBER = "IDVU36ETR";
	private static final String NODE_LOCKED_LICENSEE_NAME = "TestAndRemoveIt-licensee3";
	private static final String UNIQUE_HW_ID_01 = "unique-01";
	private static final ZonedDateTime NOW_IN_AYEAR = ZonedDateTime.now().plusDays(365);
	private static final long WAIT_FOR_VALIDATION_RESPONSE_TIME = 700; // in millis
	

	@Before
	public void clearCache() {
		ValidationResultCache.getInstance().reset();
	}
	
	@After
	public void waitToCompleteProcessing() throws InterruptedException {
		Thread.sleep(WAIT_FOR_VALIDATION_RESPONSE_TIME);
	}
	
	@Test
	public void validateNodeLockedLicense() {
		// having a licensee with a node locked license...
		String productNumber = LicenseManager.getProductNumber();
		NodeLockedModel nodeLocked = new NodeLockedModel(NOW_IN_AYEAR, UNIQUE_HW_ID_01);
		LicenseeModel licensee = new LicenseeModel(NODE_LOCKED_LICENSEE_NAME, NODE_LOCKED_LICENSEE_NUMBER, nodeLocked, productNumber);
		ValidationResultCache cache = ValidationResultCache.getInstance();
		PersistenceManager persistenceManager = PersistenceManager.getInstance();
		
		assertTrue(cache.isEmpty());
		
		// when calling a validate request...
		LicenseValidator.doValidate(licensee);
		
		// expecting the validation result to be cached and validation status to be true
		assertFalse("Expecting cache to contain received validation data", cache.isEmpty());
		ResponseParser checker = new ResponseParser(
				cache.getCachedValidationResult(), 
				cache.getValidationTimestamp(),
				cache.getLicenseName(), 
				ValidationAction.NONE);
		assertTrue(checker.isValid());
		assertEquals(LicenseType.NODE_LOCKED, checker.getType());
		assertEquals(LicenseStatus.NODE_LOCKED_REGISTERED, checker.getLicenseStatus());
		assertTrue(checker.getSubscriptionStatus());
		assertEquals(NODE_LOCKED_LICENSEE_NAME, checker.getLicenseeName());
		
		Optional<PersistenceModel> optPersistedData = persistenceManager.readPersistedData();
		assertTrue(optPersistedData.isPresent());
		PersistenceModel persistenceModel = optPersistedData.get();
		assertEquals(LicenseType.NODE_LOCKED, persistenceModel.getLicenseType().orElse(null));
		assertEquals(true, persistenceModel.getLastValidationStatus().orElse(false));
		assertEquals(NODE_LOCKED_LICENSEE_NAME, persistenceModel.getLicenseeName().orElse(null));
		assertEquals(NODE_LOCKED_LICENSEE_NUMBER, persistenceModel.getLicenseeNumber().orElse(null));
		assertEquals(true, persistenceModel.getSubscriptionStatus().orElse(false));
		
	}
	
	@Test
	public void validateNodeLockedLicenseWrongSecret() {
		// having a licensee with a node locked license and with incorrect secret id...
		String productNumber = LicenseManager.getProductNumber();
		NodeLockedModel nodeLocked = new NodeLockedModel(NOW_IN_AYEAR, "someWrongSecret");
		LicenseeModel licensee = new LicenseeModel(NODE_LOCKED_LICENSEE_NAME, NODE_LOCKED_LICENSEE_NUMBER, nodeLocked, productNumber);
		ValidationResultCache cache = ValidationResultCache.getInstance();
		PersistenceManager persistenceManager = PersistenceManager.getInstance();
		
		assertTrue(cache.isEmpty());
		
		// when calling a validate request...
		LicenseValidator.doValidate(licensee);
		
		// expecting the validation result to be cached and validation status to be false
		assertFalse("Expecting cache to contain received validation data", cache.isEmpty());
		ResponseParser checker = new ResponseParser(
				cache.getCachedValidationResult(), 
				cache.getValidationTimestamp(),
				cache.getLicenseName(), 
				ValidationAction.NONE);
		assertFalse(checker.isValid()); // validation should be false
		assertEquals(LicenseType.TRY_AND_BUY, checker.getType());
		assertEquals(LicenseStatus.TRIAL_EXPIRED, checker.getLicenseStatus());
		assertFalse(checker.getSubscriptionStatus());
		assertEquals(NODE_LOCKED_LICENSEE_NAME, checker.getLicenseeName());
		
		Optional<PersistenceModel> optPersistedData = persistenceManager.readPersistedData();
		assertTrue(optPersistedData.isPresent());
		PersistenceModel persistenceModel = optPersistedData.get();
		assertEquals(LicenseType.TRY_AND_BUY, persistenceModel.getLicenseType().orElse(null));
		assertFalse(persistenceModel.getLastValidationStatus().orElse(false));
		assertEquals(NODE_LOCKED_LICENSEE_NAME, persistenceModel.getLicenseeName().orElse(null));
		assertEquals(NODE_LOCKED_LICENSEE_NUMBER, persistenceModel.getLicenseeNumber().orElse(null));
		assertFalse(persistenceModel.getSubscriptionStatus().orElse(false));
	}
	
	@Test
	public void validateNodeLockedHwIdFailureStatus() throws InterruptedException {
		// having a licensee with a node locked license...
		prepareNodeLockedLicensee();
		LicenseManager licenseMenager = LicenseManager.getInstance();
		licenseMenager.setUniqueHwId(UNIQUE_HW_ID_01);
		licenseMenager.updateLicenseeNumber(NODE_LOCKED_LICENSEE_NUMBER, NODE_LOCKED_LICENSEE_NAME);
		
		Thread.sleep(WAIT_FOR_VALIDATION_RESPONSE_TIME);
		
		LicenseChecker preVlaidateChecker = licenseMenager.getValidationData();
		assertTrue(preVlaidateChecker.isValid());
		assertEquals(LicenseType.NODE_LOCKED, preVlaidateChecker.getType());
		assertEquals(LicenseStatus.NODE_LOCKED_REGISTERED, preVlaidateChecker.getLicenseStatus());
		assertEquals(NODE_LOCKED_LICENSEE_NAME, preVlaidateChecker.getLicenseeName());
		
		// when calling a validate request with an incorrect hardware id...
		String productNumber = LicenseManager.getProductNumber();
		NodeLockedModel nodeLocked = new NodeLockedModel(NOW_IN_AYEAR, "some-incorrect-hw-id");
		LicenseeModel licensee = new LicenseeModel(NODE_LOCKED_LICENSEE_NAME, NODE_LOCKED_LICENSEE_NUMBER, nodeLocked, productNumber);
		ValidationResultCache cache = ValidationResultCache.getInstance();
		PersistenceManager persistenceManager = PersistenceManager.getInstance();
		
		Thread.sleep(WAIT_FOR_VALIDATION_RESPONSE_TIME);

		LicenseValidator.doValidate(licensee);
		
		Thread.sleep(WAIT_FOR_VALIDATION_RESPONSE_TIME);

		LicenseValidator.doValidate(licensee);// sending a second validate request with incorrect HW ID
		
		// expecting the validation result to be cached and validation status to be false
		assertFalse("Expecting cache to contain received validation data", cache.isEmpty());
		LicenseChecker checker = licenseMenager.getValidationData();
		
		assertFalse(checker.isValid()); // validation should be false
		assertEquals(LicenseType.NODE_LOCKED, checker.getType());
		assertEquals(LicenseStatus.NODE_LOCKED_HW_ID_FAILURE, checker.getLicenseStatus()); // hw id failure shall be detected
		assertEquals(NODE_LOCKED_LICENSEE_NAME, checker.getLicenseeName());
		
		Optional<PersistenceModel> optPersistedData = persistenceManager.readPersistedData();
		assertTrue(optPersistedData.isPresent());
		PersistenceModel persistenceModel = optPersistedData.get();
		assertEquals(LicenseType.TRY_AND_BUY, persistenceModel.getLicenseType().orElse(null));
		assertFalse(persistenceModel.getLastValidationStatus().orElse(false));
		assertEquals(NODE_LOCKED_LICENSEE_NAME, persistenceModel.getLicenseeName().orElse(null));
		assertEquals(NODE_LOCKED_LICENSEE_NUMBER, persistenceModel.getLicenseeNumber().orElse(null));
		assertFalse(persistenceModel.getSubscriptionStatus().orElse(false));
		assertEquals(LicenseType.NODE_LOCKED, persistenceModel.getLastSuccessLicenseType().orElse(null));
		assertNotNull(persistenceModel.getLastSuccessTimestamp().orElse(null));
	}
	
	
	private static void prepareNodeLockedLicensee() {
		PersistenceManager persistenceMng = PersistenceManager.getInstance();
		PersistenceModel persistenceModel = new PersistenceModel(
				NODE_LOCKED_LICENSEE_NUMBER, 
				NODE_LOCKED_LICENSEE_NAME, 
				true, 
				LicenseType.NODE_LOCKED, 
				Instant.now(), 
				ZonedDateTime.now().plusDays(1),
				ZonedDateTime.now().plusHours(1), 
				ZonedDateTime.now().plusYears(1), 
				true, 
				Instant.now().minusSeconds(1), 
				LicenseType.NODE_LOCKED);
		persistenceMng.setPersistenceModel(persistenceModel);
		persistenceMng.persist();
	}
}
