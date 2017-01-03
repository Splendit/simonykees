package at.splendit.simonykees.core.license;

import java.time.Instant;
import java.time.ZonedDateTime;

import at.splendit.simonykees.core.license.model.PersistenceModel;

@SuppressWarnings("nls")
public abstract class LicenseCommonTest {
	
	protected static final String FLOATING_LICENSEE_NUMBER = "IAQ45SNQR";
	protected static final String FLOATING_LICENSEE_NAME = "Ardit Test"; 
	protected static final String NODE_LOCKED_LICENSEE_NUMBER = "IDVU36ETR";
	protected static final String NODE_LOCKED_LICENSEE_NAME = "TestAndRemoveIt-licensee3";
	
	protected static final String TEST_UNIQUE_ID_01 = "unique-01";	
	protected static final String TEST_UNIQUE_ID_02 = "unique-02";
	protected static final String TEST_UNIQUE_ID_03 = "unique-03";
	protected static final String TEST_UNIQUE_ID_04 = "unique-04";
	protected static final String TEST_UNIQUE_ID_05 = "unique-05";
	
	protected static final long WAIT_FOR_VALIDATION_RESPONSE_TIME = 1000;
	protected static final ZonedDateTime NOW_IN_AYEAR = ZonedDateTime.now().plusDays(365);
	
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

}
