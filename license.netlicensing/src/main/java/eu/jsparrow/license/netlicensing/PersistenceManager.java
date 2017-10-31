package eu.jsparrow.license.netlicensing;

import java.security.Key;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Optional;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import org.eclipse.equinox.security.storage.ISecurePreferences;
import org.eclipse.equinox.security.storage.SecurePreferencesFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.labs64.netlicensing.domain.vo.ValidationResult;

import eu.jsparrow.i18n.ExceptionMessages;
import eu.jsparrow.license.netlicensing.model.PersistenceModel;

/**
 * Responsible for encrypting, storing and retrieving data in secure storage.
 * Furthermore, this class is also responsible for constructing a validation
 * object of type {@link LicenseChecker} with the existing data being stored.
 * 
 * @author Ardit Ymeri
 * @since 1.0
 *
 */
public class PersistenceManager {

	private static final Logger logger = LoggerFactory.getLogger(PersistenceManager.class);

	private static final String LICENSEE_CREDENTIALS_NODE_KEY = "credentials"; //$NON-NLS-1$

	private static final String SIMONYKEES_KEY = "simonykees"; //$NON-NLS-1$

	private static PersistenceManager instance;

	private static final String ALGORITHM = "AES"; //$NON-NLS-1$

	private static final String TRANSFORMATION = "AES"; //$NON-NLS-1$

	private static final String KEY = "SOME_SECRET_KEY_"; //$NON-NLS-1$ //FIXME

	private static final String EMPTY_STRING = ""; //$NON-NLS-1$

	/**
	 * Time period (in seconds) of valid license without internet connection.
	 */
	private static final long OFFLINE_EXPIRATION_TIME_PERIOD = 3600;

	private PersistenceModel persistenceModel;

	private PersistenceManager() {

	}

	public static synchronized PersistenceManager getInstance() {
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
		String version = cache.getVersion();
		ResponseParser parser = new ResponseParser(validationResult, timestamp, licenseeName,
				cache.getValidatioAction());

		ZonedDateTime demoExpirationDate = parser.getEvaluationExpiresDate();
		ZonedDateTime expirationTimeStamp = parser.getExpirationTimeStamp();
		ZonedDateTime subscriptionExpirationDate = parser.getExpirationDate();
		LicenseType licenseType = parser.getType();
		boolean subscriptionStatus = parser.getSubscriptionStatus();
		boolean lastValidationStatus = parser.isValid();

		Instant lastSuccessTimestamp;
		LicenseType lastSuccessType;
		if (lastValidationStatus) {
			lastSuccessTimestamp = timestamp;
			lastSuccessType = licenseType;
		} else {
			Optional<PersistenceModel> optPersistedData = readPersistedData();
			lastSuccessTimestamp = optPersistedData.flatMap(PersistenceModel::getLastSuccessTimestamp)
				.orElse(null);
			lastSuccessType = optPersistedData.flatMap(PersistenceModel::getLastSuccessLicenseType)
				.orElse(null);
		}

		PersistenceModel persistence = new PersistenceModel(licenseeNumber, licenseeName, lastValidationStatus,
				licenseType, timestamp, demoExpirationDate, expirationTimeStamp, subscriptionExpirationDate,
				subscriptionStatus, lastSuccessTimestamp, lastSuccessType, version);
		setPersistenceModel(persistence);
		persist();
	}

	/**
	 * Stores {@link PersistenceManager#persistenceModel} into secure storage.
	 */
	void persist() {

		PersistenceModel persistence = getPersistenceModel();
		String licenseModelData = persistence.toString();

		try {
			ISecurePreferences iSecurePreferences = SecurePreferencesFactory.getDefault();
			ISecurePreferences simonykeesNode = iSecurePreferences.node(SIMONYKEES_KEY);
			simonykeesNode.clear();
			simonykeesNode.flush();

			Key secretKey = new SecretKeySpec(KEY.getBytes(), ALGORITHM);
			Cipher cipher = Cipher.getInstance(TRANSFORMATION);
			cipher.init(Cipher.ENCRYPT_MODE, secretKey);

			byte[] outputBytes = cipher.doFinal(licenseModelData.getBytes());
			simonykeesNode.putByteArray(LICENSEE_CREDENTIALS_NODE_KEY, outputBytes, false);
			simonykeesNode.flush();

		} catch (Exception exception) {
			logger.warn(ExceptionMessages.PersistenceManager_encryption_error, exception);
		}
	}

	/**
	 * Constructs a {@link PersistenceModel} object from the persisted data on
	 * the secure storage.
	 * 
	 * @return An instance of {@link PersistenceModel}.
	 */
	public Optional<PersistenceModel> readPersistedData() {
		PersistenceModel persistence = null;

		try {
			Key secretKey = new SecretKeySpec(KEY.getBytes(), ALGORITHM);
			Cipher cipher = Cipher.getInstance(TRANSFORMATION);
			cipher.init(Cipher.DECRYPT_MODE, secretKey);

			ISecurePreferences iSecurePreferences = SecurePreferencesFactory.getDefault();
			ISecurePreferences simonykeesNode = iSecurePreferences.node(SIMONYKEES_KEY);
			byte[] inputBytes = simonykeesNode.getByteArray(LICENSEE_CREDENTIALS_NODE_KEY, new byte[0]);

			byte[] outputBytes = cipher.doFinal(inputBytes);
			String persistenceStr = new String(outputBytes);
			persistence = PersistenceModel.fromString(persistenceStr);

		} catch (Exception exception) {
			logger.warn(ExceptionMessages.PersistenceManager_decryption_error, exception);
		}

		return Optional.ofNullable(persistence);
	}

	/**
	 * Reads data from secure storage and constructs a {@link LicenseChecker}
	 * object.
	 */
	public LicenseChecker vlidateUsingPersistedData() {
		PersistenceModel persistence = readPersistedData().orElse(
				// if persisted data is corrupted
				new PersistenceModel(EMPTY_STRING, // licensee number
						EMPTY_STRING, // licensee name
						false, // last validation status
						null, // license type
						null, // last validation time stamp
						null, // demo expiration date
						null, // expiration time stamp
						null, // subscription expiration date
						false, // last subscription status
						null, // last successful timestamp
						null, // last successful type
						null // last persisted version
				));
		return new OfflineLicenseChecker(persistence);
	}

	public PersistenceModel getPersistenceModel() {
		return persistenceModel;
	}

	void setPersistenceModel(PersistenceModel persistenceModel) {
		this.persistenceModel = persistenceModel;
	}

	public void updateLicenseeData(String licenseeName, String licenseeNumber) {
		PersistenceModel persistence = readPersistedData().orElse(
				// if persisted data is corrupted, keep licensee number
				// and licensee name, and ignore the rest of the data.
				new PersistenceModel(licenseeNumber, licenseeName, false, null, null, null, null, null, false, null,
						null, null));
		persistence.updateLicenseeCredential(licenseeName, licenseeNumber);
		setPersistenceModel(persistence);
		persist();
	}

	private class OfflineLicenseChecker implements LicenseChecker {

		private LicenseType licenseType;
		private boolean valid;
		private Instant validationTimestamp;
		private String licenseeName;
		private LicenseStatus licenseStatus;
		private ZonedDateTime expirationDate;

		public OfflineLicenseChecker(PersistenceModel persistence) {

			this.licenseType = persistence.getLicenseType()
				.orElse(null);
			this.valid = calcValidity(persistence);
			this.validationTimestamp = persistence.getLastValidationTimestamp()
				.orElse(null);
			this.licenseeName = persistence.getLicenseeName()
				.orElse(EMPTY_STRING);
			this.licenseStatus = calcLicenseStatus();
			this.expirationDate = calcExpirationDate(persistence);

		}

		@Override
		public LicenseType getType() {
			return licenseType;
		}

		@Override
		public boolean isValid() {
			return valid;
		}

		private LicenseStatus calcLicenseStatus() {
			LicenseStatus status = LicenseStatus.CONNECTION_FAILURE;
			if (getType() == null) {
				status = LicenseStatus.CONNECTION_FAILURE_UNREGISTERED;
			}
			return status;
		}

		private ZonedDateTime calcExpirationDate(PersistenceModel persistenceModel) {
			ZonedDateTime date;

			if (getType() != null && getType() == LicenseType.TRY_AND_BUY) {
				date = persistenceModel.getDemoExpirationDate()
					.orElse(null);
			} else {
				date = persistenceModel.getSubscriptionExpirationDate()
					.orElse(null);
			}

			return date;
		}

		private boolean calcValidity(PersistenceModel persistence) {
			/*
			 * check if last validation is not expired if type is TryAndBuy -
			 * check if demo is not expired if type is node locked or floating -
			 * check subscription is not expired - check if last validation was
			 * true.
			 */
			boolean status = false;
			Optional<Instant> lastValidationTimestamp = persistence.getLastValidationTimestamp();
			Instant now = Instant.now();
			boolean lastValidationStatus = persistence.getLastValidationStatus()
				.orElse(false);

			if (lastValidationTimestamp.isPresent() && lastValidationTimestamp.get()
				.isAfter(now.minusSeconds(OFFLINE_EXPIRATION_TIME_PERIOD)) && lastValidationStatus) {
				/*
				 * last validation time stamp was stored and is earlier than one
				 * hour. further more the last validation status was true...
				 */
				Optional<LicenseType> optLicenseType = persistence.getLicenseType();
				if (optLicenseType.isPresent()) {
					// license type was stored...
					LicenseType type = optLicenseType.get();
					if (LicenseType.TRY_AND_BUY == type) {
						// the stored license type was TryAndBuy. A further
						// check is needed for the expiration date.
						Optional<ZonedDateTime> demoExpiration = persistence.getDemoExpirationDate();
						if (demoExpiration.isPresent() && demoExpiration.get()
							.isAfter(ZonedDateTime.now())) {
							// demo time period was stored and it is not expired
							// yet
							status = true;
						}

					} else if (LicenseType.FLOATING == type || LicenseType.NODE_LOCKED == type) {
						/*
						 * the stored license type was either Floating or
						 * NodeLocked a further check is needed for the
						 * subscription expiration
						 */
						Optional<ZonedDateTime> subscriptionExpires = persistence.getSubscriptionExpirationDate();

						if (subscriptionExpires.isPresent() && subscriptionExpires.get()
							.isAfter(ZonedDateTime.now())) {
							// the subscription date was stored and is not
							// expired yet.
							status = true;
						}
					}
				}
			}

			return status;
		}

		@Override
		public Instant getValidationTimeStamp() {
			return validationTimestamp;
		}

		@Override
		public String getLicenseeName() {
			return licenseeName;
		}

		@Override
		public LicenseStatus getLicenseStatus() {
			return licenseStatus;
		}

		@Override
		public ZonedDateTime getExpirationDate() {
			return expirationDate;
		}

	}
}
