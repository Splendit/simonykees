package at.splendit.simonykees.core.license;

import java.security.Key;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Optional;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import org.eclipse.core.runtime.Status;
import org.eclipse.equinox.security.storage.ISecurePreferences;
import org.eclipse.equinox.security.storage.SecurePreferencesFactory;
import com.labs64.netlicensing.domain.vo.ValidationResult;
import at.splendit.simonykees.core.Activator;
import at.splendit.simonykees.core.i18n.ExceptionMessages;
import at.splendit.simonykees.core.license.model.PersistenceModel;

public class PersistenceManager {
	
	private PersistenceModel persistenceModel;
	private static PersistenceManager instance;
	private static final String ALGORITHM = "AES"; //$NON-NLS-1$
	private static final String TRANSFORMATION = "AES"; //$NON-NLS-1$
	private static final String KEY = "SOME_SECRET_KEY_"; //$NON-NLS-1$
	private static final String EMPTY_STRING = "";  //$NON-NLS-1$
	
	private static final String LICENSEE_CREDENTIALS_NODE_KEY = "licensee-credentials"; //$NON-NLS-1$
	private static final String SIMONYKEES_KEY = "simonykees"; //$NON-NLS-1$

	private PersistenceManager() {
		
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
		String licenseeName = cache.getLicenseName();
		String licenseeNumber = cache.getLicenseeNumber();
		LicenseCheckerImpl checker = new LicenseCheckerImpl(validationResult, timestamp, licenseeName, cache.getValidatioAction());

		ZonedDateTime demoExpirationDate = checker.getEvaluationExpiresDate();
		ZonedDateTime expirationTimeStamp = checker.getExpirationTimeStamp();
		ZonedDateTime subscriptionExpirationDate = checker.getSubscriptionExpiresDate();
		LicenseType licenseType = checker.getType();
		boolean subscriptionStatus = checker.getSubscriptionStatus();
		boolean lastValidationStatus = checker.isValid();
		
		PersistenceModel persistenceModel = new PersistenceModel(
				licenseeNumber,
				licenseeName, 
				lastValidationStatus,
				licenseType, 
				timestamp,
				demoExpirationDate,
				expirationTimeStamp,
				subscriptionExpirationDate, 
				subscriptionStatus);
		setPersistenceModel(persistenceModel);
		persist();
	}

	/**
	 * Stores {@link PersistenceManager#persistenceModel} into secure storage.
	 */
	void persist() {
		PersistenceModel persistenceModel = getPersistenceModel();
		String licenseModelData = persistenceModel.toString();
		
		try {
			ISecurePreferences iSecurePreferences = SecurePreferencesFactory.getDefault();
			ISecurePreferences simonykeesNode = iSecurePreferences.node(SIMONYKEES_KEY);
			
			
			Key secretKey = new SecretKeySpec(KEY.getBytes(), ALGORITHM);
			Cipher cipher = Cipher.getInstance(TRANSFORMATION);
			cipher.init(Cipher.ENCRYPT_MODE, secretKey);
			
			byte[] outputBytes = cipher.doFinal(licenseModelData.getBytes());
			simonykeesNode.putByteArray(LICENSEE_CREDENTIALS_NODE_KEY, outputBytes, true);
			simonykeesNode.flush();
			
		} catch (Exception exception) {
				Activator.log(Status.WARNING, ExceptionMessages.PersistenceManager_encryption_error,
						exception);
		}	
	}
	
	/**
	 * Constructs a {@link PersistenceModel} object from the persisted data on
	 * the secure storage.
	 * 
	 * @return An instance of {@link PersistenceModel}.
	 */
	public Optional<PersistenceModel> readPersistedData() {
		PersistenceModel persistenceModel = null;
		
		try {
			Key secretKey = new SecretKeySpec(KEY.getBytes(), ALGORITHM);
			Cipher cipher = Cipher.getInstance(TRANSFORMATION);
			cipher.init(Cipher.DECRYPT_MODE, secretKey);
			
			ISecurePreferences iSecurePreferences = SecurePreferencesFactory.getDefault();
			ISecurePreferences simonykeesNode = iSecurePreferences.node(SIMONYKEES_KEY);
			byte[] inputBytes = simonykeesNode.getByteArray(LICENSEE_CREDENTIALS_NODE_KEY, new byte[0]);
			
			byte[] outputBytes = cipher.doFinal(inputBytes);
			String persistenceStr = new String(outputBytes);
			persistenceModel = PersistenceModel.fromString(persistenceStr);
			
		} catch (Exception exception) {
			Activator.log(Status.WARNING, ExceptionMessages.PersistenceManager_decryption_error,
					exception);
		}
		
		return Optional.ofNullable(persistenceModel);
	}

	/**
	 * Reads data from secure storage and constructs a {@link LicenseChecker}
	 * object.
	 */
	public LicenseChecker vlidateUsingPersistedData() {
		PersistenceModel persistenceModel = readPersistedData()
				.orElse( 
						// if persisted data is corrupted
						new PersistenceModel(
								EMPTY_STRING, // licensee number
								EMPTY_STRING, // licensee name
								false, // last validation status
								null, // license type
								null, // last validation time stamp
								null, // demo expiration date
								null, // expiration time stamp
								null, // subscription expiration date
								false // last subscription status
								));
		return new OfflineLicenseChecker(persistenceModel);
	}
	
	public PersistenceModel getPersistenceModel() {
		return persistenceModel;
	}

	void setPersistenceModel(PersistenceModel persistenceModel) {
		this.persistenceModel = persistenceModel;
	}
	
	private class OfflineLicenseChecker implements LicenseChecker {
		private PersistenceModel persistence;
		
		public OfflineLicenseChecker(PersistenceModel persistence) {
			this.persistence = persistence;
		}

		@Override
		public LicenseType getType() {
			return persistence.getLicenseType().orElse(null);
		}

		@Override
		public boolean isValid() {
			// check if last validation is earlier than 1h
			// if type is TryAndBuy
			//	- check if demo is not expired
			// if type is node locked or floating
			// 	- check subscription is not expired
			//	- check if last validation was true.
			boolean status = false;
			Optional<Instant> lastValidationTimestamp = persistence.getLastValidationTimestamp();
			Instant now = Instant.now();
			Instant oneHourAgo = now.minusSeconds(3600);
			boolean lastValidationStatus = 
					persistence.getLastValidationStatus()
					.orElse(false);
			
			if(lastValidationTimestamp.isPresent()
					&& lastValidationTimestamp.get().isAfter(oneHourAgo) 
					&& lastValidationStatus) {
				
				Optional<LicenseType> optLicenseType = persistence.getLicenseType();
				if(optLicenseType.isPresent()) {
					LicenseType licenseType = optLicenseType.get();
					if(licenseType.equals(LicenseType.TRY_AND_BUY)) {
						
						Optional<ZonedDateTime> demoExpiration = persistence.getDemoExpirationDate();
						if(demoExpiration.isPresent()
								&& demoExpiration.get().isAfter(ZonedDateTime.now())) {
							status = true;
						}
						
					} else if(licenseType.equals(LicenseType.FLOATING) 
								|| licenseType.equals(LicenseType.NODE_LOCKED)) {
						
						Optional<ZonedDateTime> subscriptionExpires = persistence.getSubscriptionExpirationDate();
						
						if(subscriptionExpires.isPresent()
								&& subscriptionExpires.get().isAfter(ZonedDateTime.now())) {
							status = true;
						}
					}
				}
			}
			
			return status;
		}

		@Override
		public Instant getValidationTimeStamp() {
			return persistence.getLastValidationTimestamp().orElse(null);
		}

		@Override
		public String getLicenseeName() {
			return persistence.getLicenseeName().orElse(EMPTY_STRING);
		}

		@Override
		public LicenseStatus getLicenseStatus() {
			return LicenseStatus.CONNECTION_FAILURE;
		}
		
	}

	public Optional<String> getPersistedLicenseeName() {
		String licenseeName =
				readPersistedData()
					.flatMap(PersistenceModel::getLicenseeName)
					.orElse(EMPTY_STRING);
		
		return Optional.of(licenseeName).filter(s -> !s.isEmpty());
	}

	public Optional<String> getPersistedLicenseeNumber() {
		String licenseeNumber =
				readPersistedData()
					.flatMap(PersistenceModel::getLicenseeNumber)
					.orElse(EMPTY_STRING);
		
		return Optional.of(licenseeNumber).filter(s -> !s.isEmpty());
	}

	public void updateLicenseeData(String licenseeName, String licenseeNumber) {
		PersistenceModel persistenceModel = 
				readPersistedData()
				.orElse(
						// if persisted data is corrupted, keep licensee number
						// and licensee name, and ignore the rest of the data.
						new PersistenceModel(
							licenseeNumber,
							licenseeName,
							false,
							null, null, null, null, null, 
							false
						));
		persistenceModel.updateLicenseeCredential(licenseeName, licenseeNumber);
		setPersistenceModel(persistenceModel);
		persist();
	}
}
