package at.splendit.simonykees.core.license;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.time.Instant;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import at.splendit.simonykees.core.license.model.LicenseeModel;

public class ValidateExecutorTest extends LicenseCommonTest {
	
	@Before
	public void clearCache() {
		ValidationResultCache.getInstance().reset();
		persistNodeLockedLicensee();
	}
	
	@After
	public void waitToCompleteProcessing() throws InterruptedException {
		Thread.sleep(300);
	}
	
	@Test
	public void shutDownAndRestartScheduler() throws InterruptedException {
		// having an instance of license manager and a running scheduler...
		LicenseManager licenseManager = LicenseManager.getInstance();
		Thread.sleep(500);
		licenseManager.setUniqueHwId(TEST_UNIQUE_ID_01);
		licenseManager.initManager();
		LicenseeModel licensee = licenseManager.getLicensee();
		assertEquals(NODE_LOCKED_LICENSEE_NUMBER, licensee.getLicenseeNumber());
		assertEquals(NODE_LOCKED_LICENSEE_NAME, licensee.getLicenseeName());
		
		assertFalse(ValidateExecutor.isShutDown());
		assertFalse(ValidateExecutor.isTerminated());
		
		// when shutting down the scheduler and re-initiating the manager...
		ValidateExecutor.shutDownScheduler();
		assertTrue(ValidateExecutor.isShutDown());
		licenseManager.initManager();
		
		// expecting the scheduler to be restarted...
		assertFalse(ValidateExecutor.isShutDown());
		assertFalse(ValidateExecutor.isTerminated());
	}
	
	@Test
	public void shutDownAndCheckLicense() throws InterruptedException {
		// having an instance of license manager where the scheduler is shut down...
		LicenseManager licenseManager = LicenseManager.getInstance();
		Thread.sleep(500);
		licenseManager.setUniqueHwId(TEST_UNIQUE_ID_01);
		licenseManager.initManager();
		LicenseChecker validationData = licenseManager.getValidationData();
		Instant firstValidationTimestamp = validationData.getValidationTimeStamp();
		assertTrue(validationData.isValid());
		assertFalse(ValidateExecutor.isShutDown());
		assertFalse(ValidateExecutor.isTerminated());
		
		Thread.sleep(300);
		
		ValidateExecutor.shutDownScheduler();
		assertTrue(ValidateExecutor.isShutDown());
		
		// when getting the validation data from the manager...
		validationData = licenseManager.getValidationData();
		assertTrue(validationData.isValid());
		Instant secondValidationTimestamp = validationData.getValidationTimeStamp();
		
		// expecting the scheduler to be restarted... 
		assertFalse(ValidateExecutor.isShutDown());
		assertTrue(firstValidationTimestamp.isBefore(secondValidationTimestamp));
	}
}
