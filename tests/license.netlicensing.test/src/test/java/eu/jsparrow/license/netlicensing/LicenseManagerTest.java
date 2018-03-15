package eu.jsparrow.license.netlicensing;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runners.MethodSorters;

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
// TODO: Fails sometimes on some PCs, fix with SIM-931
@Ignore
public class LicenseManagerTest extends LicenseCommonTest {

	@Test
	public void updateLicenseeNumber() throws InterruptedException {
		// having an instance of license manager...
		// clearPersistedData();
		LicenseManager licenseMng = LicenseManager.getInstance();
		licenseMng.initManager();
		// Thread.sleep(WAIT_FOR_VALIDATION_RESPONSE_TIME);
		LicenseChecker checker = licenseMng.getValidationData();
		String oldLicenseeNumber = licenseMng.getLicenseeNumber();
		assertEquals(LicenseType.TRY_AND_BUY, checker.getType());
		assertNotNull(checker.getLicenseeName());
		assertTrue(oldLicenseeNumber.startsWith("_demo", 0));

		licenseMng.checkIn();
		Thread.sleep(WAIT_FOR_VALIDATION_RESPONSE_TIME);

		// when updating the licensee name and number
		licenseMng.setUniqueHwId(TEST_UNIQUE_ID_01);
		licenseMng.updateLicenseeNumber(NODE_LOCKED_LICENSEE_NUMBER, NODE_LOCKED_LICENSEE_NAME);

		// expecting the licensee credentials to be replaced in the following
		// validate calls
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

		LicenseManager licenseMng = LicenseManager.getInstance();
		licenseMng.setUniqueHwId("");
		Thread.sleep(WAIT_FOR_VALIDATION_RESPONSE_TIME);

		// when sending a validation request
		licenseMng.initManager();
		Thread.sleep(WAIT_FOR_VALIDATION_RESPONSE_TIME);
		LicenseChecker checker = licenseMng.getValidationData();

		// expecting to create (if it doesn't exist) a new licensee with demo
		// license
		assertEquals(LicenseType.TRY_AND_BUY, checker.getType());
		assertEquals(LicenseStatus.FREE_REGISTERED, checker.getLicenseStatus());
		assertTrue(checker.isValid());

		// ... and expecting the stored data to comply with created licensee
		PersistenceManager persistenceMng = PersistenceManager.getInstance();
		PersistenceModel persistedData = persistenceMng.readPersistedData()
			.orElse(null);
		assertNotNull(persistedData);
		assertEquals(licenseMng.getLicenseeNumber(), persistedData.getLicenseeNumber()
			.orElse(""));
		assertEquals(LicenseType.TRY_AND_BUY, persistedData.getLicenseType()
			.orElse(null));
	}

	@Test
	public void validateDemoLicense() throws InterruptedException {
		// having no licensee information stored
		LicenseManager licenseManager = LicenseManager.getInstance();
		licenseManager.setUniqueHwId("");

		// when initiating the license manager
		licenseManager.initManager();

		Thread.sleep(WAIT_FOR_VALIDATION_RESPONSE_TIME);

		// expecting a try and buy licensee to be created
		LicenseeModel licensee = licenseManager.getLicensee();
		LicenseChecker licenseChecker = licenseManager.getValidationData();

		assertEquals(LicenseType.TRY_AND_BUY, licenseChecker.getType());
		assertTrue("Expecting licensee " + licensee.getLicenseeName() + " (" + licensee.getLicenseeNumber()
				+ ") to have a valid demo license.", licenseChecker.isValid());

		LicenseValidator.doValidate(licensee);

		Thread.sleep(WAIT_FOR_VALIDATION_RESPONSE_TIME);
		licenseChecker = licenseManager.getValidationData();

		assertEquals(LicenseType.TRY_AND_BUY, licenseChecker.getType());
		assertEquals(LicenseStatus.FREE_REGISTERED, licenseChecker.getLicenseStatus());
		assertTrue(licenseChecker.isValid());
	}

	@Test
	public void validateHwIdFailureDemoLicense() throws InterruptedException {
		// having a demo licensee...
		LicenseManager licenseManager = LicenseManager.getInstance();
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
		assertEquals(LicenseStatus.FREE_HW_ID_FAILURE, licenseChecker.getLicenseStatus());
		assertFalse(licenseChecker.isValid());
	}

	@Test
	public void initiateExpiredDemoLicense() throws InterruptedException {
		// having an expired demo licensee...
		persistExpiredDemoLicensee();
		LicenseManager licenseManager = LicenseManager.getInstance();
		licenseManager.setUniqueHwId(DEMO_EXPIRED_LICENSEE_SECRET);

		// when initiating the license manager with a wrong hardware id...
		licenseManager.initManager();

		// expecting the validation state to be detected properly
		LicenseChecker licenseChecker = licenseManager.getValidationData();
		assertEquals(LicenseType.TRY_AND_BUY, licenseChecker.getType());
		assertFalse(licenseChecker.isValid());
		assertEquals(LicenseStatus.FREE_EXPIRED, licenseChecker.getLicenseStatus());
		assertNotNull(licenseChecker.getExpirationDate());
	}
}
