package at.splendit.simonykees.core.license;

import static org.junit.Assert.*;

import java.time.Instant;
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

@SuppressWarnings("nls")
public class LicenseManagerTest {
	
	private static final String LICENSEE_NUMBER = "IAQ45SNQR"; //$NON-NLS-1$
	private static final String LICENSEE_NAME = "Ardit Test"; //$NON-NLS-1$
	
	private static final String TEST_UNIQUE_ID_02 = "unique-02";
	private static final String TEST_UNIQUE_ID_03 = "unique-03";
	private static final String TEST_UNIQUE_ID_04 = "unique-04";
	private static final String TEST_UNIQUE_ID_05 = "unique-05";
	
	private final HashSet<String> usedSessions = new HashSet<>();
	
	
	@Before
	public void setUpLicensee() {
		prepareLicensee();
	}
	
	@After
	public void checkIn() {
		LicenseManager instance = LicenseManager.getInstance();
		usedSessions.forEach(sessionId -> {
			FloatingModel floatingModel = new FloatingModel(instance.getFloatingProductModuleNumber(), ZonedDateTime.now().plusDays(356), sessionId);
			instance.setUniqueHwId(sessionId);
			
			LicenseeModel licensee = new LicenseeModel("", instance.getLicenseeNumber(), floatingModel, instance.getProductNumber());
			instance.setLicensee(licensee);
			instance.setLicenseModel(floatingModel);
			
			instance.checkIn();
		});
	}
	
	@After
	public void waitToCompleteProcessing() throws InterruptedException {
		Thread.sleep(300);
	}
	
	@Test
	public void testInitLicenseManager() {

		LicenseManager instance = LicenseManager.getInstance();
		instance.initManager();
		storeUsedSessionId();
		LicenseModel licenseModel = instance.getLicenseModel();
		LicenseeModel licensee = instance.getLicensee();
		
		assertNotNull(licensee);
		assertNotNull(licenseModel);
		assertNotNull(licenseModel.getType());
		assertNotNull(licensee.getLicenseeName());
		assertNotNull(licensee.getLicenseeNumber());
		
	}

	@Test
	public void testValidator() throws InterruptedException {
		// having a licensee with license model from the prevalidation... 
		PersistenceManager persistenceMng = PersistenceManager.getInstance();
		ValidationResultCache cache = ValidationResultCache.getInstance();
		LicenseManager licenseMng = LicenseManager.getInstance();
		licenseMng.initManager();
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
		assertEquals(LICENSEE_NUMBER, pm.getLicenseeNumber().orElse(""));
		
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
	
	@Test
	public void testFloatingSession() throws InterruptedException {
		LicenseChecker checker;
		LicenseeModel licensee;
		
		// having 3 sessions occupied (the floating model used for testing has only 3 available sessions)
		LicenseManager licenseMng = LicenseManager.getInstance();
		licenseMng.initManager();
		LicenseModel licenseModel  = licenseMng.getLicenseModel();
		checker = licenseMng.getValidationData();
		assertEquals(LicenseStatus.FLOATING_CHECKED_OUT, checker.getLicenseStatus());
		assertTrue(checker.isValid());
		Thread.sleep(300);
		licenseMng.checkIn();
		checker = licenseMng.getValidationData();
		assertEquals(false, checker.isValid());
		assertEquals(LicenseStatus.FLOATING_CHECKED_IN, checker.getLicenseStatus());
		Thread.sleep(300);
		
		licenseMng.setUniqueHwId(TEST_UNIQUE_ID_02);
		licenseMng.initManager();
		checker = licenseMng.getValidationData();
		assertTrue(checker.isValid());
		storeUsedSessionId();
		assertEquals(LicenseStatus.FLOATING_CHECKED_OUT, checker.getLicenseStatus());
		Thread.sleep(300);

		
		licenseMng.setUniqueHwId(TEST_UNIQUE_ID_03);
		licenseMng.initManager();
		checker = licenseMng.getValidationData();
		assertTrue(checker.isValid());
		storeUsedSessionId();
		assertEquals(LicenseStatus.FLOATING_CHECKED_OUT, checker.getLicenseStatus());
		Thread.sleep(300);
		
		licenseMng.setUniqueHwId(TEST_UNIQUE_ID_04);
		licenseMng.initManager();
		
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
		licensee = licenseMng.getLicensee();
		checker = licenseMng.getValidationData();
		
		//expecting the validation result to be false	
		assertFalse(checker.isValid());
		assertEquals(licensee.getLicenseeName(), checker.getLicenseeName());
		assertNotNull(checker.getValidationTimeStamp());
		assertNotNull(checker.getLicenseStatus());
		assertEquals(LicenseStatus.FLOATING_OUT_OF_SESSION, checker.getLicenseStatus());
	}
	
	private static void prepareLicensee() {
		PersistenceManager persistenceMng = PersistenceManager.getInstance();
		PersistenceModel persistenceModel = new PersistenceModel(
				LICENSEE_NUMBER, 
				LICENSEE_NAME, 
				true, 
				LicenseType.FLOATING, 
				Instant.now(), 
				ZonedDateTime.now().plusDays(1),
				ZonedDateTime.now().plusHours(1), 
				ZonedDateTime.now().plusYears(1), 
				true);
		persistenceMng.setPersistenceModel(persistenceModel);
		persistenceMng.persist();
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
