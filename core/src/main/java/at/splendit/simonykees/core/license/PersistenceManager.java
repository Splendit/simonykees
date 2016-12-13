package at.splendit.simonykees.core.license;

import java.time.Instant;
import java.time.ZonedDateTime;

import com.labs64.netlicensing.domain.vo.ValidationResult;

public class PersistenceManager {

	private PersistenceModel persistenceModel;
	private static PersistenceManager instance;

	private PersistenceManager() {
		initPersistenceManager();
	}

	private void initPersistenceManager() {
		ValidationResultCache cache = ValidationResultCache.getInstance();
		if (!cache.isEmpty()) {
			persistCachedData();	
		} else {
			PersistenceModel pesistenceModel = readPersistedData();
			setPersistenceModel(pesistenceModel);
			persist();
		}
	}

	public synchronized static PersistenceManager getInstance() {
		if (instance == null) {
			instance = new PersistenceManager();
		}
		return instance;
	}

	/**
	 * Retrieves the actual data from {@link ValidationResultCache} and persist
	 * them in secure storage.
	 */
	public void persistCachedData() {
		ValidationResultCache cache = ValidationResultCache.getInstance();
		Instant timestamp = cache.getValidationTimestamp();
		ValidationResult validationResult = cache.getCachedValidationResult();
		LicenseCheckerImpl checker = new LicenseCheckerImpl(validationResult, timestamp, LicenseManager.LICENSEE_NAME);

		ZonedDateTime demoExpirationDate = checker.getEvaluationExpiresDate();
		ZonedDateTime subscriptionExpirationDate = checker.getSubscriptionExpiresDate();
		LicenseType licenseType = checker.getType();
		boolean subscriptionStatus = checker.getSubscriptionStatus();
		boolean lastValidationStatus = checker.getStatus();

		PersistenceModel persistenceModel = new PersistenceModel(
				LicenseManager.LICENSEE_NUMBER, 
				LicenseManager.LICENSEE_NAME, 
				lastValidationStatus,
				licenseType, 
				timestamp, 
				demoExpirationDate, 
				subscriptionExpirationDate, 
				subscriptionStatus);
		setPersistenceModel(persistenceModel);
		persist();
	}

	/**
	 * Stores {@link PersistenceManager#persistenceModel} into secure storage.
	 */
	private void persist() {
		// TODO Auto-generated method stub		
	}
	
	/**
	 * Constructs a {@link PersistenceModel} object from the persisted data on
	 * the secure storage.
	 * 
	 * @return An instance of {@link PersistenceModel}.
	 */
	private PersistenceModel readPersistedData() {
		// TODO: implement
		return null;
	}

	/**
	 * Reads data from secure storage and constructs a {@link LicenseChecker}
	 * object.
	 */
	public LicenseChecker vlidateUsingPersistedData() {
		PersistenceModel persistenceModel = readPersistedData();
		return new OfflineLicenseChecker(persistenceModel);
	}

	public PersistenceModel getPersistenceModel() {
		return persistenceModel;
	}

	private void setPersistenceModel(PersistenceModel persistenceModel) {
		this.persistenceModel = persistenceModel;
	}
	
	private class OfflineLicenseChecker implements LicenseChecker {
		private PersistenceModel persistence;
		
		public OfflineLicenseChecker(PersistenceModel persistence) {
			this.persistence = persistence;
		}

		@Override
		public LicenseType getType() {
			return persistence.getLicenseType();
		}

		@Override
		public boolean getStatus() {
			boolean status = false;
			Instant lastValidationTimestamp = persistence.getLastValidationTimestamp();
			Instant now = Instant.now();
			Instant oneHourAgo = now.minusSeconds(3600);
			
			if(lastValidationTimestamp.isAfter(oneHourAgo) 
					&& persistence.getLastValidationStatus()) {
				
				LicenseType licenseType = persistence.getLicenseType();
				if(licenseType.equals(LicenseType.TRY_AND_BUY)){
					ZonedDateTime demoExpiration = persistence.getDemoExpirationDate();
					if(demoExpiration.isAfter(ZonedDateTime.now())) {
						status = true;
					}
					
				} else if(licenseType.equals(LicenseType.FLOATING) 
						|| licenseType.equals(LicenseType.NODE_LOCKED)) {
					ZonedDateTime subscriptionExpires = persistence.getSubscriptionExpirationDate();
					
					if(subscriptionExpires.isAfter(ZonedDateTime.now())) {
						status = true;
					}
				}
			}
			// check if last validation is earlier than 1h
			// if type is TryAndBuy
			//	- check if demo is not expired
			// if type is node locked or floating
			// 	- check subscription is not expired
			//	- check if last validation was true.
			
			return status;
		}

		@Override
		public Instant getValidationTimeStamp() {
			return persistence.getLastValidationTimestamp();
		}

		@Override
		public String getLicenseeName() {
			return persistence.getLicenseeName();
		}
		
	}
}
