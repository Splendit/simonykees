package at.splendit.simonykees.core.license;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.time.ZonedDateTime;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;

import at.splendit.simonykees.core.license.model.LicenseeModel;
import at.splendit.simonykees.core.license.model.NodeLockedModel;
import at.splendit.simonykees.core.license.model.PersistenceModel;
import at.splendit.simonykees.core.license.model.TryAndBuyModel;
/**
 * Testing License Validator. 
 * 
 * @author Ardit Ymeri
 * @since 1.0
 *
 */
@SuppressWarnings("nls")
public class LicenseValidatorTest extends LicenseCommonTest {

	@Before
	public void clearCache() {
		ValidationResultCache.getInstance().reset();
	}
	
	@Test
	public void validateNodeLockedLicense() throws InterruptedException {
		// having a licensee with a node locked license...
		String productNumber = LicenseManager.getProductNumber();
		NodeLockedModel nodeLocked = new NodeLockedModel(NOW_IN_ONE_YEAR, TEST_UNIQUE_ID_01);
		LicenseeModel licensee = new LicenseeModel(NODE_LOCKED_LICENSEE_NAME, NODE_LOCKED_LICENSEE_NUMBER, nodeLocked, productNumber);
		ValidationResultCache cache = ValidationResultCache.getInstance();
		PersistenceManager persistenceManager = PersistenceManager.getInstance();
		
		assertTrue(cache.isEmpty());
		
		// when calling a validate request...
		LicenseValidator.doValidate(licensee);
		Thread.sleep(WAIT_FOR_VALIDATION_RESPONSE_TIME);
		
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
		// ... and expecting the validation result to be persisted...
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
	public void validateNodeLockedLicenseWrongSecret() throws InterruptedException {
		// having a licensee with a node locked license and with incorrect secret id...
		String productNumber = LicenseManager.getProductNumber();
		NodeLockedModel nodeLocked = new NodeLockedModel(NOW_IN_ONE_YEAR, "someWrongSecret");
		LicenseeModel licensee = new LicenseeModel(NODE_LOCKED_LICENSEE_NAME, NODE_LOCKED_LICENSEE_NUMBER, nodeLocked, productNumber);
		ValidationResultCache cache = ValidationResultCache.getInstance();
		PersistenceManager persistenceManager = PersistenceManager.getInstance();
		
		assertTrue(cache.isEmpty());
		
		// when calling a validate request...
		LicenseValidator.doValidate(licensee);
		Thread.sleep(WAIT_FOR_VALIDATION_RESPONSE_TIME);
		
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
		// ... and expecting the validation result to be persisted...
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
		persistNodeLockedLicensee();
		LicenseManager licenseMenager = LicenseManager.getInstance();
		Thread.sleep(WAIT_FOR_VALIDATION_RESPONSE_TIME);
		
		licenseMenager.setUniqueHwId(TEST_UNIQUE_ID_01);
		licenseMenager.updateLicenseeNumber(NODE_LOCKED_LICENSEE_NUMBER, NODE_LOCKED_LICENSEE_NAME);
		
		Thread.sleep(WAIT_FOR_VALIDATION_RESPONSE_TIME);
		
		LicenseChecker preVlaidateChecker = licenseMenager.getValidationData();
		assertTrue(preVlaidateChecker.isValid());
		assertEquals(LicenseType.NODE_LOCKED, preVlaidateChecker.getType());
		assertEquals(LicenseStatus.NODE_LOCKED_REGISTERED, preVlaidateChecker.getLicenseStatus());
		assertEquals(NODE_LOCKED_LICENSEE_NAME, preVlaidateChecker.getLicenseeName());
		
		// when calling a validate request with an incorrect hardware id...
		String productNumber = LicenseManager.getProductNumber();
		NodeLockedModel nodeLocked = new NodeLockedModel(NOW_IN_ONE_YEAR, "some-incorrect-hw-id");
		LicenseeModel licensee = new LicenseeModel(NODE_LOCKED_LICENSEE_NAME, NODE_LOCKED_LICENSEE_NUMBER, nodeLocked, productNumber);
		ValidationResultCache cache = ValidationResultCache.getInstance();
		PersistenceManager persistenceManager = PersistenceManager.getInstance();
		
		LicenseValidator.doValidate(licensee);// sending a validate request with incorrect HW ID
		Thread.sleep(WAIT_FOR_VALIDATION_RESPONSE_TIME);
		
		// expecting the validation result to be cached and validation status to be false
		assertFalse("Expecting cache to contain received validation data", cache.isEmpty());
		LicenseChecker checker = licenseMenager.getValidationData();
		
		assertFalse(checker.isValid()); // validation should be false
		assertEquals(LicenseType.NODE_LOCKED, checker.getType());
		assertEquals(LicenseStatus.NODE_LOCKED_HW_ID_FAILURE, checker.getLicenseStatus()); // hw id failure shall be detected
		assertEquals(NODE_LOCKED_LICENSEE_NAME, checker.getLicenseeName());
		// ... and expecting the validation result to be persisted...
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
	
	@Test
	public void validateExpiredDemoLicensee() throws InterruptedException {
		// having a licensee with expired demo...
		ValidationResultCache cache = ValidationResultCache.getInstance();
		String productNumber = LicenseManager.getProductNumber();
		TryAndBuyModel tryAndBuy = new TryAndBuyModel(ZonedDateTime.now().minusDays(1), DEMO_EXPIRED_LICENSEE_SECRET);
		LicenseeModel licensee = new LicenseeModel(DEMO_EXPIRED_LICENSEE_NAME, DEMO_EXPIRED_LICENSEE_NUMBER, tryAndBuy, productNumber);
		
		// when sending a validate call...
		LicenseValidator.doValidate(licensee);
		Thread.sleep(WAIT_FOR_VALIDATION_RESPONSE_TIME);
		
		// expecting the validation state to be parsed properly...
		assertFalse("Expecting cache to contain received validation data", cache.isEmpty());
		ResponseParser checker = new ResponseParser(
				cache.getCachedValidationResult(), 
				cache.getValidationTimestamp(),
				cache.getLicenseName(), 
				ValidationAction.NONE);
		
		assertEquals(LicenseStatus.TRIAL_EXPIRED, checker.getLicenseStatus());
		assertFalse(checker.isValid());
		assertNotNull(checker.getEvaluationExpiresDate());
		assertTrue(checker.getEvaluationExpiresDate().isBefore(ZonedDateTime.now()));
	}
}
