package at.splendit.simonykees.core.license;

import java.io.IOException;
import java.time.Instant;
import java.time.ZonedDateTime;

import org.eclipse.core.runtime.Status;
import org.eclipse.equinox.security.storage.ISecurePreferences;
import org.eclipse.equinox.security.storage.SecurePreferencesFactory;
import org.junit.After;
import org.junit.Assert;
import org.junit.BeforeClass;

import at.splendit.simonykees.core.Activator;
import at.splendit.simonykees.core.i18n.ExceptionMessages;
import at.splendit.simonykees.core.license.model.PersistenceModel;

/**
 * A super class containing relevant constants and methods for testing. 
 * 
 * @author Ardit Ymeri
 * @since 1.0
 *
 */
@SuppressWarnings("nls")
public abstract class LicenseCommonTest {
	
	// the following values are hard copied from existing licensees on the net licensing server
	protected static final String FLOATING_LICENSEE_NUMBER = "IAQ45SNQR";
	protected static final String FLOATING_LICENSEE_NAME = "Ardit Test"; 
	protected static final String NODE_LOCKED_LICENSEE_NUMBER = "IDVU36ETR";
	protected static final String NODE_LOCKED_LICENSEE_NAME = "TestAndRemoveIt-licensee3";
	protected static final String DEMO_EXPIRED_LICENSEE_NUMBER = "for-expired-demo"; 
	protected static final String DEMO_EXPIRED_LICENSEE_NAME = "For expired demo";
	protected static final String DEMO_EXPIRED_LICENSEE_SECRET = "demo-expired-secret";
	
	// the following are used as test values for hw id and floating session id
	protected static final String TEST_UNIQUE_ID_01 = "unique-01";	
	protected static final String TEST_UNIQUE_ID_02 = "unique-02";
	protected static final String TEST_UNIQUE_ID_03 = "unique-03";
	protected static final String TEST_UNIQUE_ID_04 = "unique-04";
	protected static final String TEST_UNIQUE_ID_05 = "unique-05";
	
	// other constants for testing purposes
	protected static final long WAIT_FOR_VALIDATION_RESPONSE_TIME = 1000; // in milliseconds
	protected static final ZonedDateTime NOW_IN_ONE_YEAR = ZonedDateTime.now().plusDays(365);
	
	/**
	 * Stores/overwrites a licensee with a valid floating license. 
	 */
	protected static void persistFloatingLicensee() {
		PersistenceManager persistenceMng = PersistenceManager.getInstance();
		PersistenceModel persistenceModel = new PersistenceModel(
				FLOATING_LICENSEE_NUMBER, 
				FLOATING_LICENSEE_NAME, 
				true, 
				LicenseType.FLOATING, 
				Instant.now(), 
				ZonedDateTime.now().plusDays(1),
				ZonedDateTime.now().plusHours(1), 
				ZonedDateTime.now().plusYears(1), 
				true, 
				Instant.now().minusSeconds(1), 
				LicenseType.FLOATING);
		persistenceMng.setPersistenceModel(persistenceModel);
		persistenceMng.persist();
	}
	
	/**
	 * Stores/overwrites a licensee with a valid node locked license. 
	 */
	protected static void persistNodeLockedLicensee() {
		PersistenceManager persistenceMng = PersistenceManager.getInstance();
		PersistenceModel persistenceModel = new PersistenceModel(
				NODE_LOCKED_LICENSEE_NUMBER, 
				NODE_LOCKED_LICENSEE_NAME, 
				true, 
				LicenseType.NODE_LOCKED, 
				Instant.now(), 
				ZonedDateTime.now().plusDays(1),
				ZonedDateTime.now().plusHours(1), 
				ZonedDateTime.now().plusYears(1), 
				true, 
				Instant.now().minusSeconds(1), 
				LicenseType.NODE_LOCKED);
		persistenceMng.setPersistenceModel(persistenceModel);
		persistenceMng.persist();
	}
	
	/**
	 * Stores/overwrites a licensee with expired demo license.
	 */
	protected static void persistExpiredDemoLicensee() {
		PersistenceManager persistenceMng = PersistenceManager.getInstance();
		PersistenceModel persistenceModel = new PersistenceModel(
				DEMO_EXPIRED_LICENSEE_NUMBER, 
				DEMO_EXPIRED_LICENSEE_NAME, 
				false, 
				LicenseType.TRY_AND_BUY, 
				Instant.now(), 
				ZonedDateTime.now().minusDays(1),
				null, 
				null, 
				false, 
				Instant.now().minusSeconds(1), 
				LicenseType.TRY_AND_BUY);
		persistenceMng.setPersistenceModel(persistenceModel);
		persistenceMng.persist();
	}
	
	@After
	public void tearDown() throws InterruptedException {
		Thread.sleep(WAIT_FOR_VALIDATION_RESPONSE_TIME);
		try {
			ISecurePreferences iSecurePreferences = SecurePreferencesFactory.getDefault();
			do {
				iSecurePreferences.node("simonykees").removeNode();
				iSecurePreferences.flush();
			} while (iSecurePreferences.nodeExists("simonykees"));
		} catch (IOException exception) {
			Activator.log(Status.WARNING, ExceptionMessages.PersistenceManager_encryption_error, exception);
			Assert.fail();
		}
	}

	
	/**
	 * Overwrites the persisted data with empty and null values.
	 */
	protected void clearPersistedData() {
		PersistenceManager persistenceMng = PersistenceManager.getInstance();
		PersistenceModel persistenceModel = new PersistenceModel(
				"", "", 
				true, 
				null, null, null, null, null, 
				true,
				null, null);
		persistenceMng.setPersistenceModel(persistenceModel);
		persistenceMng.persist();
	}
	
	@BeforeClass
	public static void initTestLicenseManager() {
		LicenseManager.getInstance();
	}
}
