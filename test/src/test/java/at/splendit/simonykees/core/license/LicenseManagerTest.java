package at.splendit.simonykees.core.license;

import static org.junit.Assert.*;

import java.time.Instant;
import java.time.ZonedDateTime;

import org.junit.BeforeClass;
import org.junit.Test;

public class LicenseManagerTest {
	
	private static final String LICENSEE_NUMBER = "IITAK75GN";
	private static final String LICENSEE_NAME = "License-Ali-Test";
	
	@BeforeClass
	public static void setUpLicensee() {
		prepareLicensee();
	}
	
	@Test
	public void testInitLicenseManager() {

		LicenseManager instance = LicenseManager.getInstance();
		LicenseModel licenseModel = instance.getLicenseModel();
		LicenseeEntity licensee = instance.getLicensee();
		
		assertNotNull(licensee);
		assertNotNull(licenseModel);
		assertNotNull(licenseModel.getType());
		assertNotNull(licensee.getLicenseeName());
		assertNotNull(licensee.getLicenseeNumber());
		
	}
	
	@Test
	public void testValidator() {
		// having a licensee with license model from the prevalidation... 
		PersistenceManager persistenceMng = PersistenceManager.getInstance();
		ValidationResultCache cache = ValidationResultCache.getInstance();
		LicenseManager licenseMng = LicenseManager.getInstance();
		LicenseModel licenseModel = licenseMng.getLicenseModel();
		LicenseeEntity licensee = licenseMng.getLicensee();
		
		assertFalse(
				"Expecting cache to not be empty after prevalidation...", 
				cache.isEmpty());
		
		cache.reset();
		assertTrue("Expecting cache to be empty after reseting it...", cache.isEmpty());
		
		// when sending a routine validate call...
		LicenseValidator.doValidate(licensee);
		
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
}
