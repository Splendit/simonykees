package eu.jsparrow.license.netlicensing;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.labs64.netlicensing.domain.vo.Context;
import com.labs64.netlicensing.domain.vo.ValidationParameters;
import com.labs64.netlicensing.domain.vo.ValidationResult;
import com.labs64.netlicensing.exception.NetLicensingException;
import com.labs64.netlicensing.service.LicenseeService;

import eu.jsparrow.i18n.Messages;
import eu.jsparrow.license.netlicensing.model.FloatingModel;
import eu.jsparrow.license.netlicensing.model.LicenseModel;
import eu.jsparrow.license.netlicensing.model.LicenseeModel;
import eu.jsparrow.license.netlicensing.model.NodeLockedModel;
import eu.jsparrow.license.netlicensing.model.PersistenceModel;
import eu.jsparrow.license.netlicensing.model.SchedulerModel;
import eu.jsparrow.license.netlicensing.model.TryAndBuyModel;
import oshi.SystemInfo;
import oshi.hardware.HWDiskStore;
import oshi.hardware.HardwareAbstractionLayer;

/**
 * Manager of the validation process. Starts the validation process, caches the
 * result of the validation, and provides relevant information of the validation
 * results.
 * 
 * @author Ardit Ymeri, Andreja Sambolec, Matthias Webhofer
 * @since 1.0
 *
 */
public class LicenseManager {

	private static final Logger logger = LoggerFactory.getLogger(LicenseManager.class);

	public static final String DEMO_LICENSE_NUMBER = "demo"; //$NON-NLS-1$
	
	private static final String PRODUCT_NUMBER = LicenseProperties.LICENSE_PRODUCT_NUMBER;
	public static final String VERSION = PRODUCT_NUMBER;

	/**
	 * Rest API authentication token.
	 */
	static final String PASS_APIKEY = LicenseProperties.LICENSE_PASS_API_KEY;

	/**
	 * Product module number related to Floating licenses.
	 */
	private static final String PRODUCT_MODULE_NUMBER = LicenseProperties.LICENSE_PRODUCT_MODULE_NUMBER;

	/**
	 * Waiting time in milliseconds for receiving and processing a validation
	 * call.
	 */
	private static final long WAIT_FOR_VALIDATION_RESPONSE = 1000;

	private static final boolean DO_VALIDATE = true;
	/**
	 * Validation interval in seconds. 10 * 60s = 10 minutes
	 */
	private static final long VALIDATE_INTERVAL_IN_SECONDS = 600;
	/**
	 * Delay of the first validation of the scheduler
	 */
	private static final long INITIAL_VALIDATION_DELAY = 0;

	private static LicenseManager instance;

	private SchedulerModel schedulerEntity;
	private LicenseeModel licensee;
	private LicenseModel licenseModel;

	private String licenseeName;
	private String licenseeNumber;

	private String uniqueHwId = ""; //$NON-NLS-1$

	private LicenseManager() {
		// Hide default constructor
	}

	public static synchronized LicenseManager getInstance() {
		if (instance == null) {
			instance = new LicenseManager();
		}
		return instance;
	}

	public void initManager() {
		schedulerEntity = new SchedulerModel(VALIDATE_INTERVAL_IN_SECONDS, INITIAL_VALIDATION_DELAY, DO_VALIDATE);

		Instant now = Instant.now();

		PersistenceManager persistenceManager = PersistenceManager.getInstance();
		LicenseType licenseType;
		ZonedDateTime evaluationExpiresDate;
		ZonedDateTime expirationTimeStamp;

		/**
		 * Load persisted data: If no data is there persist demo license with
		 * current timestamp. If there is demo data, check if license is still
		 * "valid". Otherwise, make call if validation is fine.
		 * 
		 **/

		Optional<PersistenceModel> persistedData = persistenceManager.readPersistedData()
			.filter(pm -> pm.getLastPersistedVersion()
				.filter(LicenseManager.VERSION::equals)
				.isPresent());

		String name = persistedData.flatMap(PersistenceModel::getLicenseeName)
			.filter(s -> !s.isEmpty())
			.orElse(calcDemoLicenseeName());
		String number = persistedData.flatMap(PersistenceModel::getLicenseeNumber)
			.filter(s -> !s.isEmpty())
			.orElse(DEMO_LICENSE_NUMBER);
		setLicenseeName(name);
		setLicenseeNumber(number);

		if (!persistedData.isPresent()) {
			// New install, no license information. Create demo persistence model.

			ValidationResultCache cache = ValidationResultCache.getInstance();
			cache.updateCachedResult(null, name, number, now, ValidationAction.CHECK_OUT, VERSION);
			evaluationExpiresDate = getFiveDaysFromNow();
			persistenceManager.persistDemoLicense(evaluationExpiresDate, true);
			
			 persistedData = persistenceManager.readPersistedData();
		} 
		
		if (number.contains(DEMO_LICENSE_NUMBER)) {
			licenseType = persistedData.flatMap(PersistenceModel::getLicenseType)
				.orElse(LicenseType.TRY_AND_BUY);
			evaluationExpiresDate = persistedData.flatMap(PersistenceModel::getDemoExpirationDate)
				.orElse(null);
			expirationTimeStamp = persistedData.flatMap(PersistenceModel::getExpirationTimeStamp)
				.orElse(null);
			boolean lastValidationStatus = persistedData.flatMap(PersistenceModel::getLastValidationStatus)
					.orElse(false);
			ValidationResultCache cache = ValidationResultCache.getInstance();
			cache.updateCachedResult(null, name, number, now, ValidationAction.CHECK_OUT, VERSION);
			persistenceManager.persistDemoLicense(evaluationExpiresDate, lastValidationStatus);
		} else {
			try {
				/*
				 * make a pre-validate call to get the information for the
				 * relevant license model
				 */
				ValidationResult validationResult = preValidate(PRODUCT_NUMBER, PRODUCT_MODULE_NUMBER, number, name);
				ResponseParser parser = new ResponseParser(validationResult, now, name, ValidationAction.CHECK_OUT);

				// cash and persist pre-validation...
				ValidationResultCache cache = ValidationResultCache.getInstance();
				cache.updateCachedResult(validationResult, name, number, now, ValidationAction.CHECK_OUT, VERSION);
				persistenceManager.persistCachedData();

				// extract pre-validation result
				licenseType = parser.getType();
				evaluationExpiresDate = parser.getEvaluationExpiresDate();
				expirationTimeStamp = parser.getExpirationTimeStamp();

			} catch (NetLicensingException e) {

				logger.warn(Messages.LicenseManager_cannot_reach_licensing_provider_on_checkin);
				licenseType = persistedData.flatMap(PersistenceModel::getLicenseType)
					.orElse(LicenseType.TRY_AND_BUY);
				evaluationExpiresDate = persistedData.flatMap(PersistenceModel::getDemoExpirationDate)
					.orElse(null);
				expirationTimeStamp = persistedData.flatMap(PersistenceModel::getExpirationTimeStamp)
					.orElse(null);

			}

		}

		// construct a license model
		LicenseModel model = constructLicenseModel(licenseType, evaluationExpiresDate, expirationTimeStamp,
				PRODUCT_MODULE_NUMBER);
		setLicenseModel(model);

		// construct a licensee object...
		LicenseeModel licensee1 = new LicenseeModel(name, number, model, PRODUCT_NUMBER);
		setLicensee(licensee1);

		// start validate scheduler
		ValidateExecutor.validationAttempt();
		ValidateExecutor.startSchedule(schedulerEntity, licensee1);

	}

	private ZonedDateTime getFiveDaysFromNow() {
		ZonedDateTime date = ZonedDateTime.now();
		return date.plusDays(5);
	}

	void setLicensee(LicenseeModel licensee) {
		this.licensee = licensee;
	}

	/**
	 * Sends a pre-validate call for the given product and licensee. Floating
	 * license model is used for pre-validation, because it needs extra
	 * parameters. The validation response is returned without being modified.
	 */
	private ValidationResult preValidate(String productNumber, String productModuleNumber, String licenseeNumber,
			String licenseeName) throws NetLicensingException {

		Context context = RestApiConnection.getAPIRestConnection()
			.getContext();
		ValidationResult preValidationResult;
		ZonedDateTime now = ZonedDateTime.now();
		// to be used only during pre-validation, as a expiration date.
		ZonedDateTime nowInOneYear = now.plusYears(1);
		/*
		 * pre-validation is done by using floating model because it contains a
		 * superset of all other model's validation parameters
		 */
		String hwId = getUniqueNodeIdentifier();

		FloatingModel floatingModel = new FloatingModel(productModuleNumber, nowInOneYear, hwId);

		// pre-validation with floating license model...
		LicenseeModel licensee1 = new LicenseeModel(licenseeName, licenseeNumber, floatingModel, productNumber);
		ValidationParameters valParams = licensee1.getValidationParams();
		preValidationResult = LicenseeService.validate(context, licenseeNumber, valParams);

		return preValidationResult;
	}

	/**
	 * Sends a request to release one Floating session which is occupied by the
	 * current user. Relevant only for the case when the licensing model is
	 * {@link FloatingModel}.
	 */
	public void checkIn() {
		LicenseModel model = getLicenseModel();
		PersistenceManager persistMng = PersistenceManager.getInstance();
		if (model instanceof FloatingModel) {
			Context context = RestApiConnection.getAPIRestConnection()
				.getContext();
			FloatingModel floatingModel = (FloatingModel) model;
			ValidationParameters checkingValParameters = floatingModel.getCheckInValidationParameters();
			try {
				Instant now = Instant.now();
				logger.debug(Messages.LicenseManager_session_check_in);
				ValidationResult checkinResult = LicenseeService.validate(context, getLicenseeNumber(),
						checkingValParameters);
				ValidationResultCache cache = ValidationResultCache.getInstance();
				cache.updateCachedResult(checkinResult, getLicenseeName(), getLicenseeNumber(), now,
						ValidationAction.CHECK_IN, VERSION);
				persistMng.persistCachedData();
				ValidateExecutor.shutDownScheduler();

			} catch (NetLicensingException e) {
				logger.warn(Messages.LicenseManager_cannot_reach_licensing_provider_on_checkin, e);
			}
		}
	}

	public LicenseeModel getLicensee() {
		return licensee;
	}

	private LicenseModel constructLicenseModel(LicenseType licenseType, ZonedDateTime expireDate,
			ZonedDateTime expireTimeStamp, String productModulNumber) {
		LicenseModel model;

		switch (licenseType) {
		case FLOATING:
			String sessionId = getUniqueNodeIdentifier();
			model = new FloatingModel(productModulNumber, expireTimeStamp, sessionId);
			break;
		case TRY_AND_BUY:
			String secret = getUniqueNodeIdentifier();
			model = new TryAndBuyModel(expireDate, secret);
			break;
		case NODE_LOCKED:
			String secretKey = getUniqueNodeIdentifier();
			model = new NodeLockedModel(expireDate, secretKey);
			break;
		default:
			model = new TryAndBuyModel(expireDate, getUniqueNodeIdentifier());
			break;
		}

		return model;
	}

	void setUniqueHwId(String uniqueHwId) {
		this.uniqueHwId = uniqueHwId;
	}

	@SuppressWarnings("nls")
	private String getUniqueNodeIdentifier() {
		if (this.uniqueHwId != null && this.uniqueHwId.isEmpty()) {
			String diskSerial = "";
			SystemInfo systemInfo = new SystemInfo();

			HardwareAbstractionLayer hal = systemInfo.getHardware();
			HWDiskStore[] diskStores = hal.getDiskStores();

			if (diskStores.length > 0) {
				diskSerial = diskStores[0].getSerial();
			}

			setUniqueHwId(diskSerial);
		}

		return uniqueHwId;
	}

	@SuppressWarnings("nls")
	private String calcDemoLicenseeName() {
		String demoLicenseeName = "";

		try {
			InetAddress address = InetAddress.getLocalHost();
			demoLicenseeName = address.getHostName();
		} catch (UnknownHostException e) {
			// nothing
		}

		return demoLicenseeName;
	}

	/**
	 * Constructs an instance of type {@link LicenseChecker} which contains
	 * information about the validity of the license.
	 */
	public LicenseChecker getValidationData() {

		ValidationResultCache cache = ValidationResultCache.getInstance();
		PersistenceManager persistenceManager = PersistenceManager.getInstance();
		LicenseChecker checker;

		ValidateExecutor.validationAttempt();
		// if there is a cached validation result...

		if (!cache.isEmpty() && cache.getCachedValidationResult() != null) {

			/*
			 * create an instance of LicenseChecker from the parser and last
			 * successful info...
			 */
			checker = validateUsingCache();

			if (checker.isValid() && ValidateExecutor.isShutDown()) {
				/*
				 * cache cannot be trusted if the validate executor is shut
				 * down.
				 */

				// restart the scheduler
				ValidateExecutor.startSchedule(schedulerEntity, licensee);

				// wait for the validation call...
				try {
					Thread.sleep(WAIT_FOR_VALIDATION_RESPONSE);
				} catch (InterruptedException e) {
					logger.error(Messages.LicenseManager_wait_for_validation_was_interrupted, e);
					Thread.currentThread()
						.interrupt();
					cache.reset();
				}

				// and reconstruct an instance of type LicenseChecker
				if (!cache.isEmpty() && cache.getCachedValidationResult() != null) {
					checker = validateUsingCache();
				} else {
					checker = persistenceManager.vlidateUsingPersistedData();
				}
			}
			
		} else if (!cache.isEmpty()) {
			checker = persistenceManager.vlidateUsingPersistedData();
			
		} else {

			
			/*
			 * Try to make a validation call. Here is the case that the previous
			 * validation call failed due to internet connection.
			 */
			LicenseValidator.doValidate(getLicensee());

			if (!cache.isEmpty() && cache.getCachedValidationResult() != null) {
				checker = validateUsingCache();

			} else {
				/*
				 * otherwise use the persisted data to create an instance of
				 * type LicenseChecker...
				 */
				checker = persistenceManager.vlidateUsingPersistedData();
			}
		}

		return checker;
	}

	private LicenseChecker validateUsingCache() {
		ValidationResultCache cache = ValidationResultCache.getInstance();
		PersistenceManager persistenceManager = PersistenceManager.getInstance();

		// construct a validation result parser...
		ResponseParser parser = new ResponseParser(cache.getCachedValidationResult(), cache.getValidationTimestamp(),
				getLicenseeName(), cache.getValidatioAction());

		/*
		 * and get the last successful validation information from
		 * persistence...
		 */
		Optional<PersistenceModel> optPersistedData = persistenceManager.readPersistedData();
		Instant lastSuccessTimestamp = optPersistedData.flatMap(PersistenceModel::getLastSuccessTimestamp)
			.orElse(null);
		LicenseType lastSuccessType = optPersistedData.flatMap(PersistenceModel::getLastSuccessLicenseType)
			.orElse(null);

		/*
		 * create an instance of LicenseChecker from the parser and last
		 * successful info...
		 */
		return new CheckerImpl(parser, lastSuccessTimestamp, lastSuccessType);
	}

	LicenseModel getLicenseModel() {
		return licenseModel;
	}

	void setLicenseModel(LicenseModel licenseModel) {
		this.licenseModel = licenseModel;
	}

	/**
	 * Overwrites the existing license name and number with the given ones,
	 * unless the new licensee number does not belong to an existing licensee
	 * (in which case, a fall-back to existing licensee is performed).
	 * 
	 * If the update process is successful (i.e the given licensee number
	 * belongs to an existing licensee), the new credentials will be used for
	 * future validation calls.
	 * 
	 * @param licenseeNumber
	 *            new licensee number.
	 * @param licenseeName
	 *            new licensee name.
	 */
	public boolean updateLicenseeNumber(String licenseeNumber, String licenseeName) {
		boolean updated = false;
		boolean validLicensee = LicenseValidator.isValidLicensee(licenseeNumber);
		if (validLicensee) {
			String existingLicenseeNumber = getLicenseeNumber();
			String existingLicenseeName = getLicenseeName();
			logger.info(Messages.LicenseManager_updating_licensee_credentials);
			setLicenseeName(licenseeName);
			setLicenseeNumber(licenseeNumber);
			PersistenceManager persistence = PersistenceManager.getInstance();
			persistence.updateLicenseeData(licenseeName, licenseeNumber);
			// re-initiate manager as a new licenseeNumber is received...
			ValidateExecutor.shutDownScheduler();
			initManager();
			updated = true;

			try {
				Thread.sleep(WAIT_FOR_VALIDATION_RESPONSE);
			} catch (InterruptedException e) {
				Thread.currentThread()
					.interrupt();
				// do nothing. no hurt...
			}

			LicenseChecker checker = getValidationData();
			if (!isValidUpdate(checker)) {
				logger.warn(Messages.LicenseManager_invalid_new_license_key);
				setLicenseeNumber(existingLicenseeNumber);
				setLicenseeName(existingLicenseeName);
				if (checker != null && checker.getLicenseStatus() == LicenseStatus.CONNECTION_FAILURE_UNREGISTERED) {
					existingLicenseeNumber = ""; //$NON-NLS-1$
					existingLicenseeName = ""; //$NON-NLS-1$
				}
				overwritePersistedData(existingLicenseeNumber, existingLicenseeName);
				ValidateExecutor.shutDownScheduler();
				initManager();
				updated = false;
			}
		} else {
			logger.warn(Messages.LicenseManager_invalid_new_license_key);
		}

		return updated;
	}

	private boolean isValidUpdate(LicenseChecker checker) {
		boolean valid = false;

		if (checker != null && checker.getLicenseStatus() != LicenseStatus.CONNECTION_FAILURE
				&& checker.getLicenseStatus() != LicenseStatus.CONNECTION_FAILURE_UNREGISTERED
				&& checker.getType() != null && checker.getType() != LicenseType.TRY_AND_BUY && checker.isValid()) {
			valid = true;
		}

		return valid;
	}

	String getLicenseeNumber() {
		return this.licenseeNumber;
	}

	private void setLicenseeNumber(String licenseeNumber) {
		this.licenseeNumber = licenseeNumber;
	}

	private String getLicenseeName() {
		return this.licenseeName;
	}

	private void setLicenseeName(String licenseeName) {
		this.licenseeName = licenseeName;
	}

	static String getFloatingProductModuleNumber() {
		return PRODUCT_MODULE_NUMBER;
	}

	static String getProductNumber() {
		return PRODUCT_NUMBER;
	}

	private void overwritePersistedData(String licenseeNumber, String licenseeName) {
		PersistenceModel persistenceModel = new PersistenceModel(licenseeNumber, licenseeName, false, null, null, null,
				null, null, false, null, null, null);
		PersistenceManager persistence = PersistenceManager.getInstance();
		persistence.setPersistenceModel(persistenceModel);
		persistence.persist();
	}

	public static void setJSparrowRunning(boolean running) {
		ValidateExecutor.setJSparrowRunning(running);
	}

	public static boolean isRunning() {
		return !ValidateExecutor.isShutDown();
	}

	
	private class CheckerImpl implements LicenseChecker {

		private LicenseType parsedLicenseType;
		private LicenseType licenseType;
		private LicenseStatus licenseStatus;
		private boolean valid;
		private boolean subscriptionValid;
		private Instant timestamp;
		private ZonedDateTime expirationDate;
		private String licenseeName;
		private LicenseStatus parsedLicenseStatus;
		private ZonedDateTime parsedExpirationDate;
		private ZonedDateTime demoExpireation;

		public CheckerImpl(ResponseParser parser, Instant lastSussessTimestamp, LicenseType lastSuccessType) {
			this.demoExpireation = parser.getEvaluationExpiresDate();
			this.parsedLicenseType = parser.getType();
			this.parsedLicenseStatus = parser.getLicenseStatus();
			this.valid = parser.isValid();
			this.subscriptionValid = parser.getSubscriptionStatus();
			this.timestamp = parser.getValidationTimeStamp();
			this.licenseeName = parser.getLicenseeName();
			this.parsedExpirationDate = parser.getExpirationDate();
			calcLicenseStatus(lastSuccessType, lastSussessTimestamp);
			this.expirationDate = calcExpireDate(parser);

		}

		/**
		 * Calculates the {@link LicenseStatus} based on the parsed validity of
		 * the license and the last successful validation time-stamp.
		 * 
		 * It covers the case where the NodeLocked/Free license is still valid
		 * but the hardware id does not match with the one of the first
		 * validation. Note that this method does not calculate anything about
		 * the validity of the license.
		 */
		private void calcLicenseStatus(LicenseType lastSuccessLicenseType, Instant lastSuccessTimestamp) {
			if (isValid() || isSubscriptionValid()) {
				/*
				 * If the license or the subscription is valid, then the license
				 * type/status is the same as the parsed license type/status.
				 */
				this.licenseType = parsedLicenseType;
				this.licenseStatus = parsedLicenseStatus;
			} else {
				/*
				 * Otherwise, if the last successful validation is stored, the
				 * last successful license type is NodeLocked and the license is
				 * not expired yet, then it must be the case that the hardware
				 * id does not match
				 */
				if (lastSuccessLicenseType != null && lastSuccessTimestamp != null
						&& lastSuccessLicenseType == LicenseType.NODE_LOCKED && parsedExpirationDate != null
						&& Instant.now()
							.isBefore(parsedExpirationDate.toInstant())) {

					this.licenseStatus = LicenseStatus.NODE_LOCKED_HW_ID_FAILURE;
					this.licenseType = LicenseType.NODE_LOCKED;

				} else if (this.parsedLicenseType != null && this.demoExpireation != null
						&& parsedLicenseType == LicenseType.TRY_AND_BUY && Instant.now()
							.isBefore(demoExpireation.toInstant())) {

					this.licenseStatus = LicenseStatus.FREE_HW_ID_FAILURE;
					this.licenseType = LicenseType.TRY_AND_BUY;
				} else {
					// otherwise, just keep the parsed license type/status.
					this.licenseType = parsedLicenseType;
					this.licenseStatus = parsedLicenseStatus;
				}
			}
		}

		public ZonedDateTime calcExpireDate(ResponseParser parser) {
			LicenseType type = getType();
			ZonedDateTime expireDate = null;
			if (LicenseType.TRY_AND_BUY == type) {
				expireDate = parser.getEvaluationExpiresDate();
			} else {
				expireDate = parser.getExpirationDate();
			}
			return expireDate;
		}

		private boolean isSubscriptionValid() {
			return subscriptionValid;
		}

		@Override
		public LicenseType getType() {
			return licenseType;
		}

		@Override
		public boolean isValid() {
			return this.valid;
		}

		@Override
		public Instant getValidationTimeStamp() {
			return timestamp;
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
