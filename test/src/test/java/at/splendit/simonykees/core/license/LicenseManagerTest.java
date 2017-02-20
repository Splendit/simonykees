package at.splendit.simonykees.core.license;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Optional;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import at.splendit.simonykees.core.license.model.FloatingModel;
import at.splendit.simonykees.core.license.model.LicenseModel;
import at.splendit.simonykees.core.license.model.LicenseeModel;
import at.splendit.simonykees.core.license.model.PersistenceModel;

/**
 * Testing license manager. 
 * 
 * @author Ardit Ymeri
 * @since 1.0
 *
 */
@SuppressWarnings("nls")
public class LicenseManagerTest extends LicenseCommonTest {
		
	private final HashSet<String> usedSessions = new HashSet<>();

	@Before
	public void setUpLicensee() {
		persistFloatingLicensee();
	}
	
	@After
	public void checkIn() {
		LicenseManager instance = LicenseManager.getTestInstance();
		usedSessions.forEach(sessionId -> {
			FloatingModel floatingModel = new FloatingModel(LicenseManager.getTestFloatingProductModuleNumber(), ZonedDateTime.now().plusDays(356), sessionId);
			instance.setUniqueHwId(sessionId);
			
			LicenseeModel licensee = new LicenseeModel("", instance.getLicenseeNumber(), floatingModel, LicenseManager.getTestProductNumber());
			instance.setLicensee(licensee);
			instance.setLicenseModel(floatingModel);
			
			instance.checkIn();
			// setting a pause between checkIn requests.
			try {
				Thread.sleep(WAIT_FOR_VALIDATION_RESPONSE_TIME);
			} catch (InterruptedException e) {
				
			}
		});

	}
	
	@After
	public void clearSecureStorage() throws InterruptedException {
		Thread.sleep(WAIT_FOR_VALIDATION_RESPONSE_TIME);
		clearPersistedData();
	}
	
	//FIXME: tests related to floating license are temporarily removed
	//@Test
	public void testInitLicenseManager() {
		// when initiating the license manager...
		LicenseManager instance = LicenseManager.getTestInstance();
		instance.initManager();
		storeUsedSessionId();
		LicenseModel licenseModel = instance.getLicenseModel();
		LicenseeModel licensee = instance.getLicensee();
		
		// expecting the licensee and the license model to be initiated, too...
		assertNotNull(licensee);
		assertNotNull(licenseModel);
		assertNotNull(licenseModel.getType());
		assertNotNull(licensee.getLicenseeName());
		assertNotNull(licensee.getLicenseeNumber());
		
	}

	//@Test
	public void testValidator() throws InterruptedException {
		// having a licensee with license model from the prevalidation... 
		PersistenceManager persistenceMng = PersistenceManager.getInstance();
		ValidationResultCache cache = ValidationResultCache.getInstance();
		LicenseManager licenseMng = LicenseManager.getTestInstance();
		licenseMng.initManager();
		Thread.sleep(WAIT_FOR_VALIDATION_RESPONSE_TIME);
		LicenseModel licenseModel = licenseMng.getLicenseModel();
		LicenseeModel licensee = licenseMng.getLicensee();
		storeUsedSessionId();
		assertFalse(
				"Expecting cache to not be empty after prevalidation...", 
				cache.isEmpty());

		// when sending a routine validate call...
		Optional<PersistenceModel> optPm = persistenceMng.readPersistedData();
		assertTrue(optPm.isPresent());
		PersistenceModel pm = optPm.get();
		assertEquals(FLOATING_LICENSEE_NUMBER, pm.getLicenseeNumber().orElse(""));
		
		// expecting the validation result to comply with the pre-validation data...
		assertFalse(
				"Expecting cache to not be empty after a validation call...", 
				cache.isEmpty());
		LicenseChecker checker = licenseMng.getValidationData();
		assertEquals(licenseModel.getType(), checker.getType());		
		assertTrue(checker.isValid());
		assertEquals(licensee.getLicenseeName(), checker.getLicenseeName());
		assertNotNull(checker.getValidationTimeStamp());
		assertNotNull(checker.getLicenseStatus());
		
		// expecting the persisted validation status to comply with the pre-validation data...
		LicenseChecker checkFromPersistence = persistenceMng.vlidateUsingPersistedData();
		assertEquals(licenseModel.getType(), checkFromPersistence.getType());
		assertTrue(checkFromPersistence.isValid());
		assertEquals(licensee.getLicenseeName(), checkFromPersistence.getLicenseeName());
		assertNotNull(checkFromPersistence.getValidationTimeStamp());
		assertEquals(LicenseStatus.CONNECTION_FAILURE, checkFromPersistence.getLicenseStatus());
	}
	
	//@Test
	public void floatingOutOfSessionsSession() throws InterruptedException {
		LicenseChecker checker;
		LicenseeModel licensee;
		
		// having 3 sessions occupied (the floating model used for testing has only 3 available sessions)
		LicenseManager licenseMng = LicenseManager.getTestInstance();
		licenseMng.initManager();// 1 occupied session
		Thread.sleep(WAIT_FOR_VALIDATION_RESPONSE_TIME);
		LicenseModel licenseModel  = licenseMng.getLicenseModel();
		checker = licenseMng.getValidationData();
		assertEquals(LicenseStatus.FLOATING_CHECKED_OUT, checker.getLicenseStatus());
		assertTrue(checker.isValid());
		licenseMng.checkIn();// occupied sessions is released. 0 occupied sessions
		Thread.sleep(WAIT_FOR_VALIDATION_RESPONSE_TIME);
		checker = licenseMng.getValidationData();
		assertEquals(false, checker.isValid());
		assertEquals(LicenseStatus.FLOATING_CHECKED_IN, checker.getLicenseStatus());
		
		// setting a new hw-id will occupy a new session, because the next validate 
		// call will use the new hw-id as a session id.
		licenseMng.setUniqueHwId(TEST_UNIQUE_ID_02); 
		licenseMng.initManager(); // 1 occupied session
		Thread.sleep(WAIT_FOR_VALIDATION_RESPONSE_TIME);
		checker = licenseMng.getValidationData();
		assertTrue(checker.isValid());
		storeUsedSessionId();
		assertEquals(LicenseStatus.FLOATING_CHECKED_OUT, checker.getLicenseStatus());

		
		licenseMng.setUniqueHwId(TEST_UNIQUE_ID_03);
		licenseMng.initManager(); // 2 occupied sessions
		Thread.sleep(WAIT_FOR_VALIDATION_RESPONSE_TIME);
		checker = licenseMng.getValidationData();
		assertTrue(checker.isValid());
		storeUsedSessionId();
		assertEquals(LicenseStatus.FLOATING_CHECKED_OUT, checker.getLicenseStatus());
		
		licenseMng.setUniqueHwId(TEST_UNIQUE_ID_04);
		licenseMng.initManager(); // 3 occupied sessions
		Thread.sleep(WAIT_FOR_VALIDATION_RESPONSE_TIME);
		
		checker = licenseMng.getValidationData();
		licensee = licenseMng.getLicensee();
		
		assertEquals(licenseModel.getType(), checker.getType());	
		assertTrue(checker.isValid());
		storeUsedSessionId();
		assertEquals(licensee.getLicenseeName(), checker.getLicenseeName());
		assertNotNull(checker.getValidationTimeStamp());
		assertNotNull(checker.getLicenseStatus());
		assertEquals(LicenseStatus.FLOATING_CHECKED_OUT, checker.getLicenseStatus());
		
		// when sending validation with a fourth session id...
		licenseMng.setUniqueHwId(TEST_UNIQUE_ID_05);
		licenseMng.initManager();
		Thread.sleep(WAIT_FOR_VALIDATION_RESPONSE_TIME);
		licensee = licenseMng.getLicensee();
		checker = licenseMng.getValidationData();
		
		//expecting the validation result to be false	
		assertFalse(checker.isValid());
		assertEquals(licensee.getLicenseeName(), checker.getLicenseeName());
		assertNotNull(checker.getValidationTimeStamp());
		assertNotNull(checker.getLicenseStatus());
		assertEquals(LicenseStatus.FLOATING_OUT_OF_SESSION, checker.getLicenseStatus());
	}
	
	//@Test
	public void runningSchedulerAftercheckIn() throws InterruptedException {
		// having initiated an instance of license manager for a floating licensee
		LicenseManager licenseMng = LicenseManager.getTestInstance();
		licenseMng.initManager();
		Thread.sleep(WAIT_FOR_VALIDATION_RESPONSE_TIME);
		
		LicenseChecker checker = licenseMng.getValidationData();
		assertTrue(checker.isValid());
		assertEquals(LicenseType.FLOATING, checker.getType());
		
		// when sending a check-in request
		licenseMng.checkIn();
		Thread.sleep(WAIT_FOR_VALIDATION_RESPONSE_TIME);
		checker = licenseMng.getValidationData();
		
		// expecting the validity to be false and the scheduler to be shut down
		assertFalse(checker.isValid());
		assertTrue(ValidateExecutor.isShutDown());
		assertEquals(LicenseStatus.FLOATING_CHECKED_IN, checker.getLicenseStatus());
	}
	
	@Test
	public void updateLicenseeNumber() throws InterruptedException {
		// having an instance of license manager...
		clearPersistedData();
		LicenseManager licenseMng = LicenseManager.getTestInstance();
		licenseMng.initManager();
		Thread.sleep(WAIT_FOR_VALIDATION_RESPONSE_TIME);
		LicenseChecker checker = licenseMng.getValidationData();
		String oldLicenseeNumber = licenseMng.getLicenseeNumber();
		assertEquals(LicenseType.TRY_AND_BUY, checker.getType());
		assertNotNull(checker.getLicenseeName());
		assertTrue(oldLicenseeNumber.startsWith("demo", 0));
		
		licenseMng.checkIn();
		Thread.sleep(WAIT_FOR_VALIDATION_RESPONSE_TIME);
		
		//when updating the licensee name and number
		licenseMng.setUniqueHwId(TEST_UNIQUE_ID_01);
		licenseMng.updateLicenseeNumber(NODE_LOCKED_LICENSEE_NUMBER, NODE_LOCKED_LICENSEE_NAME);
		
		// expecting the licensee credentials to be replaced in the following validate calls
		checker = licenseMng.getValidationData();
		
		assertEquals(NODE_LOCKED_LICENSEE_NAME, checker.getLicenseeName());
		assertEquals(LicenseType.NODE_LOCKED, checker.getType());
		assertEquals(NODE_LOCKED_LICENSEE_NUMBER, licenseMng.getLicenseeNumber());		
		LicenseModel newLicenseModel = licenseMng.getLicenseModel();
		assertEquals(LicenseType.NODE_LOCKED, newLicenseModel.getType());
		assertTrue(checker.isValid());
		// ... and expecting the scheduler to be running...
		assertFalse(ValidateExecutor.isShutDown());
	}
	
	@Test
	public void createTryAndBuyLicensee() throws InterruptedException {
		// having cleared the persisted data...
		Thread.sleep(WAIT_FOR_VALIDATION_RESPONSE_TIME);
		clearPersistedData();
		
		LicenseManager licenseMng = LicenseManager.getTestInstance();
		licenseMng.setUniqueHwId("");
		Thread.sleep(WAIT_FOR_VALIDATION_RESPONSE_TIME);
		
		// when sending a validation request
		licenseMng.initManager();
		Thread.sleep(WAIT_FOR_VALIDATION_RESPONSE_TIME);
		LicenseChecker checker = licenseMng.getValidationData();

		
		// expecting to create (if it doesn't exist) a new licensee with demo license
		assertEquals(LicenseType.TRY_AND_BUY, checker.getType());
		assertEquals(LicenseStatus.TRIAL_REGISTERED, checker.getLicenseStatus());
		assertTrue(checker.isValid());
		
		// ... and expecting the stored data to comply with created licensee
		PersistenceManager persistenceMng = PersistenceManager.getInstance();
		PersistenceModel persistedData = persistenceMng.readPersistedData().orElse(null);
		assertNotNull(persistedData);
		assertEquals(licenseMng.getLicenseeNumber(), persistedData.getLicenseeNumber().orElse(""));
		assertEquals(LicenseType.TRY_AND_BUY, persistedData.getLicenseType().orElse(null));
	}
	
	@Test
	public void validateDemoLicense() throws InterruptedException {
		// having no licensee information stored
		Thread.sleep(WAIT_FOR_VALIDATION_RESPONSE_TIME);
		clearPersistedData();
		LicenseManager licenseManager = LicenseManager.getTestInstance();
		licenseManager.setUniqueHwId("");
		
		
		// when initiating the license manager
		licenseManager.initManager();
		
		Thread.sleep(WAIT_FOR_VALIDATION_RESPONSE_TIME);
		
		// expecting a try and buy licensee to be created
		LicenseeModel licensee = licenseManager.getLicensee();
		LicenseChecker licenseChecker = licenseManager.getValidationData();
		
		assertEquals(LicenseType.TRY_AND_BUY, licenseChecker.getType());
		assertTrue(licenseChecker.isValid());
		
		LicenseValidator.doValidate(licensee);
		
		Thread.sleep(WAIT_FOR_VALIDATION_RESPONSE_TIME);
		licenseChecker = licenseManager.getValidationData();
		
		assertEquals(LicenseType.TRY_AND_BUY, licenseChecker.getType());
		assertEquals(LicenseStatus.TRIAL_REGISTERED, licenseChecker.getLicenseStatus());
		assertTrue(licenseChecker.isValid());
	}
	
	@Test
	public void validateHwIdFailureDemoLicense() throws InterruptedException {
		// having a demo licensee...
		clearPersistedData();
		LicenseManager licenseManager = LicenseManager.getTestInstance();
		licenseManager.setUniqueHwId("");

		licenseManager.initManager();
		Thread.sleep(WAIT_FOR_VALIDATION_RESPONSE_TIME);
		LicenseChecker licenseChecker = licenseManager.getValidationData();
		
		assertEquals(LicenseType.TRY_AND_BUY, licenseChecker.getType());
		assertTrue(licenseChecker.isValid());
		
		// when initiating the license manager with a wrong hardware id...
		licenseManager.setUniqueHwId("wrong-hw-id");
		licenseManager.initManager();
		LicenseeModel licensee = licenseManager.getLicensee();
		LicenseValidator.doValidate(licensee);
		Thread.sleep(WAIT_FOR_VALIDATION_RESPONSE_TIME);
		
		licenseChecker = licenseManager.getValidationData();

		// expecting the validation data to be false
		assertEquals(LicenseType.TRY_AND_BUY, licenseChecker.getType());
		assertEquals(LicenseStatus.TRIAL_HW_ID_FAILURE, licenseChecker.getLicenseStatus());
		assertFalse(licenseChecker.isValid());
	}
	
	@Test
	public void initiateExpiredDemoLicense() throws InterruptedException {
		// having an expired demo licensee...
		persistExpiredDemoLicensee();
		LicenseManager licenseManager = LicenseManager.getTestInstance();
		licenseManager.setUniqueHwId(DEMO_EXPIRED_LICENSEE_SECRET);

		// when initiating the license manager with a wrong hardware id...
		licenseManager.initManager();
		
		// expecting the validation state to be detected properly
		LicenseChecker licenseChecker = licenseManager.getValidationData();
		assertEquals(LicenseType.TRY_AND_BUY, licenseChecker.getType());
		assertFalse(licenseChecker.isValid());
		assertEquals(LicenseStatus.TRIAL_EXPIRED, licenseChecker.getLicenseStatus());
		assertNotNull(licenseChecker.getExpirationDate());
	}
	
	private void storeUsedSessionId() {
		LicenseManager licenseManager = LicenseManager.getTestInstance();
		LicenseModel licenseModel = licenseManager.getLicenseModel();
		if(licenseModel instanceof FloatingModel) {
			String sessionId = ((FloatingModel) licenseModel).getSessionId();
			usedSessions.add(sessionId);
		}
	}
}
