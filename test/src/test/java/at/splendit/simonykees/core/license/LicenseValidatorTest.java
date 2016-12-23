package at.splendit.simonykees.core.license;

import static org.junit.Assert.*;

import java.time.ZonedDateTime;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;

import at.splendit.simonykees.core.license.model.LicenseeModel;
import at.splendit.simonykees.core.license.model.NodeLockedModel;
import at.splendit.simonykees.core.license.model.PersistenceModel;

@SuppressWarnings("nls")
public class LicenseValidatorTest {

	private static final String NODE_LOCKED_LICENSEE_NUMBER = "IDVU36ETR";
	private static final String NODE_LOCKED_LICENSEE_NAME = "TestAndRemoveIt-licensee3";
	private static final String UNIQUE_WH_ID_01 = "unique-01";
	private static final ZonedDateTime NOW_IN_AYEAR = ZonedDateTime.now().plusDays(365);
	

	@Before
	public void clearCache() {
		ValidationResultCache.getInstance().reset();
	}
	
	@Test
	public void validateNodeLockedLicense() {
		// having a licensee with a node locked license...
		String productNumber = LicenseManager.getProductNumber();
		NodeLockedModel nodeLocked = new NodeLockedModel(NOW_IN_AYEAR, UNIQUE_WH_ID_01);
		LicenseeModel licensee = new LicenseeModel(NODE_LOCKED_LICENSEE_NAME, NODE_LOCKED_LICENSEE_NUMBER, nodeLocked, productNumber);
		ValidationResultCache cache = ValidationResultCache.getInstance();
		PersistenceManager persistenceManager = PersistenceManager.getInstance();
		
		assertTrue(cache.isEmpty());
		
		// when calling a validate request...
		LicenseValidator.doValidate(licensee);
		
		// expecting the validation result to be cached and validation status to be true
		assertFalse("Expecting cache to contain received validation data", cache.isEmpty());
		LicenseCheckerImpl checker = new LicenseCheckerImpl(
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
		LicenseCheckerImpl checker = new LicenseCheckerImpl(
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
}
