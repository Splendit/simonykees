package at.splendit.simonykees.core.license;

import static org.junit.Assert.*;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Optional;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

import at.splendit.simonykees.core.license.model.LicenseModel;
import at.splendit.simonykees.core.license.model.LicenseeModel;
import at.splendit.simonykees.core.license.model.PersistenceModel;

public class LicenseManagerTest {
	
	private static final String LICENSEE_NUMBER = "IPWAY9YZI"; //$NON-NLS-1$
	private static final String LICENSEE_NAME = ""; //$NON-NLS-1$
	
	@BeforeClass
	public static void setUpLicensee() {
		prepareLicensee();
	}
	
	@After
	public void checkId() {
		LicenseManager instance = LicenseManager.getInstance();
		instance.checkIn();
	}
	
	@Test
	public void testInitLicenseManager() {

		LicenseManager instance = LicenseManager.getInstance();
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
		LicenseModel licenseModel = licenseMng.getLicenseModel();
		LicenseeModel licensee = licenseMng.getLicensee();
		
		assertFalse(
				"Expecting cache to not be empty after prevalidation...", 
				cache.isEmpty());

		
		// when sending a routine validate call...
		Thread.sleep(13000);
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
	public void testFloatingSession() {
		// having 3 sessions occupied (the floating model used for testing has only 3 available sessions)
		LicenseManager licenseMng = LicenseManager.getInstance();
		LicenseModel licenseModel  = licenseMng.getLicenseModel();
		
		LicenseChecker checker;
		LicenseeModel licensee;
		
		licenseMng.setUniqueHwId("unique-02");
		licenseMng.initManager();

		
		licenseMng.setUniqueHwId("unique-03");
		licenseMng.initManager();
		
		licenseMng.setUniqueHwId("unique-04");
		licenseMng.initManager();
		
		checker = licenseMng.getValidationData();
		licensee = licenseMng.getLicensee();
		
		LicenseValidator.doValidate(licensee);
		
		LicenseValidator.doValidate(licensee);
		
		assertEquals(licenseModel.getType(), checker.getType());	
		assertTrue(checker.isValid());
		assertEquals(licensee.getLicenseeName(), checker.getLicenseeName());
		assertNotNull(checker.getValidationTimeStamp());
		assertNotNull(checker.getLicenseStatus());
		assertEquals(LicenseStatus.FLOATING_CHECKED_OUT, checker.getLicenseStatus());
		
		// when sending validation with a fourth session id...
		licenseMng.setUniqueHwId("unique-05");
		licenseMng.initManager();
		licensee = licenseMng.getLicensee();
		checker = licenseMng.getValidationData();
		
		//expecting the validation result to be false	
		assertFalse(checker.isValid());
		assertEquals(licensee.getLicenseeName(), checker.getLicenseeName());
		assertNotNull(checker.getValidationTimeStamp());
		assertNotNull(checker.getLicenseStatus());
		assertEquals(LicenseStatus.FLOATING_OUT_OF_SESSION, checker.getLicenseStatus());
		
		licenseMng.setUniqueHwId("unique-02");
		licenseMng.initManager();
		licenseMng.checkIn();
		
		licenseMng.setUniqueHwId("unique-03");
		licenseMng.initManager();
		licenseMng.checkIn();
		
		licenseMng.setUniqueHwId("unique-04");
		licenseMng.initManager();
		licenseMng.checkIn();
	}
	
	private static void prepareLicensee() {
		PersistenceManager persistenceMng = PersistenceManager.getInstance();
		PersistenceModel persistenceModel = new PersistenceModel(
				LICENSEE_NUMBER, 
				LICENSEE_NAME, 
				true, 
				LicenseType.NODE_LOCKED, 
				Instant.now(), 
				ZonedDateTime.now().plusDays(1),
				ZonedDateTime.now().plusHours(1), 
				ZonedDateTime.now().plusYears(1), 
				true);
		persistenceMng.setPersistenceModel(persistenceModel);
		persistenceMng.persist();
	}
}
