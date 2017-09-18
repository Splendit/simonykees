package eu.jsparrow.license.netlicensing;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Optional;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import eu.jsparrow.license.netlicensing.LicenseChecker;
import eu.jsparrow.license.netlicensing.LicenseManager;
import eu.jsparrow.license.netlicensing.LicenseStatus;
import eu.jsparrow.license.netlicensing.LicenseType;
import eu.jsparrow.license.netlicensing.PersistenceManager;
import eu.jsparrow.license.netlicensing.ValidateExecutor;
import eu.jsparrow.license.netlicensing.ValidationResultCache;
import eu.jsparrow.license.netlicensing.model.FloatingModel;
import eu.jsparrow.license.netlicensing.model.LicenseModel;
import eu.jsparrow.license.netlicensing.model.LicenseeModel;
import eu.jsparrow.license.netlicensing.model.PersistenceModel;

/**
 * Testing license manager. 
 * 
 * @author Ardit Ymeri
 * @since 1.0
 *
 */
@SuppressWarnings("nls")
public class FloatingLicenseManagerTest extends LicenseCommonTest {
		
	private final HashSet<String> usedSessions = new HashSet<>();

	@Before
	public void setUpLicensee() throws InterruptedException {
		persistFloatingLicensee();
	}
	
	//Connected to floating licenses
	@After
	public void checkIn() {
		LicenseManager instance = LicenseManager.getInstance();
		usedSessions.forEach(sessionId -> {
			FloatingModel floatingModel = new FloatingModel(LicenseManager.getFloatingProductModuleNumber(), ZonedDateTime.now().plusDays(356), sessionId);
			instance.setUniqueHwId(sessionId);
			
			LicenseeModel licensee = new LicenseeModel("", instance.getLicenseeNumber(), floatingModel, LicenseManager.getProductNumber());
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
		
	
	//FIXME: tests related to floating license are temporarily removed
	@Ignore
	@Test
	public void testInitLicenseManager() {
		// when initiating the license manager...
		LicenseManager instance = LicenseManager.getInstance();
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

	@Ignore
	@Test
	public void testValidator() throws InterruptedException {
		// having a licensee with license model from the prevalidation... 
		PersistenceManager persistenceMng = PersistenceManager.getInstance();
		ValidationResultCache cache = ValidationResultCache.getInstance();
		LicenseManager licenseMng = LicenseManager.getInstance();
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
	
	@Ignore
	@Test
	public void floatingOutOfSessionsSession() throws InterruptedException {
		LicenseChecker checker;
		LicenseeModel licensee;
		
		// having 3 sessions occupied (the floating model used for testing has only 3 available sessions)
		LicenseManager licenseMng = LicenseManager.getInstance();
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
	
	@Ignore
	@Test
	public void runningSchedulerAftercheckIn() throws InterruptedException {
		// having initiated an instance of license manager for a floating licensee
		LicenseManager licenseMng = LicenseManager.getInstance();
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
	
	private void storeUsedSessionId() {
		LicenseManager licenseManager = LicenseManager.getInstance();
		LicenseModel licenseModel = licenseManager.getLicenseModel();
		if(licenseModel instanceof FloatingModel) {
			String sessionId = ((FloatingModel) licenseModel).getSessionId();
			usedSessions.add(sessionId);
		}
	}
}
