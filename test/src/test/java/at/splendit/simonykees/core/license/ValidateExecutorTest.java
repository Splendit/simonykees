package at.splendit.simonykees.core.license;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.time.Instant;

import org.junit.After;
import org.junit.Ignore;
import org.junit.Test;

import at.splendit.simonykees.core.license.model.LicenseeModel;

/**
 * Testing scheduler. 
 * 
 * @author Ardit Ymeri
 * @since 1.0
 *
 */
public class ValidateExecutorTest extends LicenseCommonTest {
	
	@After
	public void cleareSecureStorage() throws InterruptedException {
		ValidateExecutor.shutDownScheduler();
	}
	
	@Test
	//@Ignore
	public void shutDownAndRestartScheduler() throws InterruptedException {
		// having an instance of license manager and a running scheduler...
		
		ValidationResultCache.getInstance().reset();
		persistNodeLockedLicensee();
		Thread.sleep(WAIT_FOR_VALIDATION_RESPONSE_TIME);
		LicenseManager licenseManager = LicenseManager.getTestInstance();
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
	//@Ignore
	public void shutDownAndCheckLicense() throws InterruptedException {
		// having an instance of license manager where the scheduler is shut down...
		
		ValidationResultCache.getInstance().reset();
		persistNodeLockedLicensee();
		
		LicenseManager licenseManager = LicenseManager.getTestInstance();
		licenseManager.setUniqueHwId(TEST_UNIQUE_ID_01);
		licenseManager.initManager();
		Thread.sleep(WAIT_FOR_VALIDATION_RESPONSE_TIME);
		LicenseChecker validationData = licenseManager.getValidationData();
		Instant firstValidationTimestamp = validationData.getValidationTimeStamp();
		assertTrue(validationData.isValid());
		assertFalse(ValidateExecutor.isShutDown());
		assertFalse(ValidateExecutor.isTerminated());
		
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
