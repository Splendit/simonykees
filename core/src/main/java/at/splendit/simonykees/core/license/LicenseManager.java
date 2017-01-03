package at.splendit.simonykees.core.license;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Optional;

import org.eclipse.core.runtime.Status;

import com.labs64.netlicensing.domain.vo.Context;
import com.labs64.netlicensing.domain.vo.ValidationParameters;
import com.labs64.netlicensing.domain.vo.ValidationResult;
import com.labs64.netlicensing.exception.NetLicensingException;
import com.labs64.netlicensing.service.LicenseeService;

import at.splendit.simonykees.core.Activator;
import at.splendit.simonykees.core.i18n.Messages;
import at.splendit.simonykees.core.license.model.FloatingModel;
import at.splendit.simonykees.core.license.model.LicenseModel;
import at.splendit.simonykees.core.license.model.LicenseeModel;
import at.splendit.simonykees.core.license.model.NodeLockedModel;
import at.splendit.simonykees.core.license.model.PersistenceModel;
import at.splendit.simonykees.core.license.model.SchedulerModel;
import at.splendit.simonykees.core.license.model.TryAndBuyModel;
import oshi.SystemInfo;
import oshi.hardware.HWDiskStore;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.hardware.NetworkIF;

/**
 * Manager of the validation process. Starts the validation process, caches the
 * result of the validation, and provides relevant information of the validation
 * results.
 * 
 * @author ardit.ymeri
 *
 */
public class LicenseManager {

	private static final String DEFAULT_LICENSEE_NUMBER_PREFIX = "demo"; //$NON-NLS-1$
	private static final String PRODUCT_NUMBER = "PNZNF7Y7E"; //$NON-NLS-1$
	private static final String PRODUCT_MODULE_NUMBER = "M6IS9TIWG"; //$NON-NLS-1$ product module number for floating model
	private static final long WAIT_FOR_VALIDATION_RESPONSE = 1000; // in milliseconds

	private final boolean DO_VALIDATE = true;
	private final long VALIDATE_INTERVAL_IN_SECONDS = 600; // validation interval in seconds. 10 * 60s = 6 minutes
	private final long INITIAL_VALIDATION_DELAY = 0;  // delay of the first validation of the scheduler

	private static LicenseManager instance;

	private SchedulerModel schedulerEntity;
	private LicenseeModel licensee;
	private LicenseModel licenseModel;
	
	private String licenseeName;
	private String licenseeNumber;
	
	private String uniqueHwId = ""; //$NON-NLS-1$
	
	private LicenseManager() {
		// TODO: throw an exception if the instance is not null...
		initManager();
	}

	public synchronized static LicenseManager getInstance() {
		if (instance == null) {
			instance = new LicenseManager();
		}
		return instance;
	}

	void initManager() {
		schedulerEntity = new SchedulerModel(
				VALIDATE_INTERVAL_IN_SECONDS, 
				INITIAL_VALIDATION_DELAY, 
				DO_VALIDATE);

		Instant now = Instant.now();

		PersistenceManager persistenceManager = PersistenceManager.getInstance();
		LicenseType licenseType;
		ZonedDateTime evaluationExpiresDate;
		ZonedDateTime expirationTimeStamp;
		String licenseeName = persistenceManager.getPersistedLicenseeName().orElse(""); //$NON-NLS-1$
		setLicenseeName(licenseeName);
		String licenseeNumber = persistenceManager.getPersistedLicenseeNumber().orElse(calcDemoLicenseeNumber());
		setLicenseeNumber(licenseeNumber);
		
		try {
			// make a pre-validate call to get the license model relevant information...
			ValidationResult validationResult = preValidate(PRODUCT_NUMBER, PRODUCT_MODULE_NUMBER, licenseeNumber, licenseeName);
			ResponseParser parser = new ResponseParser(validationResult, now, licenseeName, ValidationAction.CHECK_OUT);
			
			// cash and persist pre-validation...
			ValidationResultCache cache = ValidationResultCache.getInstance();
			cache.updateCachedResult(validationResult, licenseeName, licenseeNumber, now, ValidationAction.CHECK_OUT);
			persistenceManager.persistCachedData();
			
			// extract pre-validation result
			licenseType = parser.getType();
			evaluationExpiresDate = parser.getEvaluationExpiresDate();
			expirationTimeStamp = parser.getExpirationTimeStamp();
			
		} catch (NetLicensingException e) {
			Optional<PersistenceModel> persistedData = persistenceManager.readPersistedData();
			
			Activator.log(Status.WARNING, Messages.LicenseManager_cannot_reach_licensing_provider_on_prevalidation, e);
			
			licenseType = 
					persistedData
						.flatMap(PersistenceModel::getLicenseType)
						.orElse(LicenseType.TRY_AND_BUY);
			evaluationExpiresDate = 
					persistedData
						.flatMap(PersistenceModel::getDemoExpirationDate)
						.orElse(null);
			expirationTimeStamp = 
					persistedData
						.flatMap(PersistenceModel::getExpirationTimeStamp)
						.orElse(null);
			
		}

		// construct a license model
		LicenseModel licenseModel = constructLicenseModel(licenseType, evaluationExpiresDate, expirationTimeStamp, PRODUCT_MODULE_NUMBER);
		setLicenseModel(licenseModel);

		// construct a licensee object...
		LicenseeModel licensee = new LicenseeModel(licenseeName, licenseeNumber, licenseModel, PRODUCT_NUMBER);
		setLicensee(licensee);

		// start validate scheduler
		ValidateExecutor.startSchedule(schedulerEntity, licensee);
 
	}

	void setLicensee(LicenseeModel licensee) {
		this.licensee = licensee;
	}

	/**
	 * Sends a pre-validate call for the given product and licensee. Floating 
	 * license model is used for pre-validation, because it needs extra parameters.
	 * The validation response is returned without being modified.
	 */
	private ValidationResult preValidate(String productNumber,
										 String productModuleNumber,
										 String licenseeNumber, 
										 String licenseeName) throws NetLicensingException {
		
		Context context = RestApiConnection.getAPIRestConnection().getContext();
		ValidationResult preValidationResult;
		ZonedDateTime now = ZonedDateTime.now();
		// to be used only during pre-validation, as a expiration date.
		ZonedDateTime nowInOneYear = now.plusYears(1);
		// pre-validation is done by using floating model 
		// because it contains a superset of all other model's validation parameters 
		String uniqueHwId = getUniqueNodeIdentifier();
		
		FloatingModel floatingModel = new FloatingModel(productModuleNumber, nowInOneYear, uniqueHwId);

		// pre-validation with floating license model...
		LicenseeModel licensee = new LicenseeModel(licenseeName, licenseeNumber, floatingModel, productNumber);
		ValidationParameters valParams = licensee.getValidationParams();
		preValidationResult = LicenseeService.validate(context, licenseeNumber, valParams);

		return preValidationResult;
	}

	/**
	 * Sends a request to release one Floating session which is occupied 
	 * by the current user. Relevant only for the case when the licensing 
	 * model is {@link FloatingModel}. 
	 */
	public void checkIn() {
		LicenseModel licenseModel = getLicenseModel();
		PersistenceManager persistMng = PersistenceManager.getInstance();
		if(licenseModel instanceof FloatingModel){
			Context context = RestApiConnection.getAPIRestConnection().getContext();
			FloatingModel floatingModel = (FloatingModel)licenseModel;
			ValidationParameters checkingValParameters = floatingModel.getCheckInValidationParameters();
			try {
				Instant now = Instant.now();
				Activator.log(Messages.LicenseManager_session_check_in);
				ValidationResult checkinResult = LicenseeService.validate(context, getLicenseeNumber(), checkingValParameters);
				ValidationResultCache cache = ValidationResultCache.getInstance();
				cache.updateCachedResult(checkinResult, getLicenseeName(), getLicenseeNumber(), now, ValidationAction.CHECK_IN);
				persistMng.persistCachedData();
				ValidateExecutor.shutDownScheduler();
				 
			} catch (NetLicensingException e) {
				// TODO add a validation status indicating that the check-in was not successful.
				Activator.log(Status.WARNING, Messages.LicenseManager_cannot_reach_licensing_provider_on_checkin, e);
			}		
		}
	}
	
	public LicenseeModel getLicensee() {
		return licensee;
	}

	private LicenseModel constructLicenseModel(LicenseType licenseType, ZonedDateTime expireDate, ZonedDateTime expireTimeStamp, String productModulNumber) {
		LicenseModel licenseModel;

		switch (licenseType) {
		case FLOATING:
			String sessionId = getUniqueNodeIdentifier();
			licenseModel = new FloatingModel(productModulNumber, expireTimeStamp, sessionId);
			break;
		case TRY_AND_BUY:
			String secret = getUniqueNodeIdentifier();
			licenseModel = new TryAndBuyModel(expireDate, secret);
			break;
		case NODE_LOCKED:
			String secretKey = getUniqueNodeIdentifier();
			licenseModel = new NodeLockedModel(expireDate, secretKey);
			break;
		default:
			licenseModel = new TryAndBuyModel(expireDate, getUniqueNodeIdentifier());
			break;
		}

		return licenseModel;
	}
	
	void setUniqueHwId(String uniqueHwId) {
		this.uniqueHwId = uniqueHwId;
	}

	@SuppressWarnings("nls")
	private String getUniqueNodeIdentifier() {
		if(this.uniqueHwId != null && this.uniqueHwId.isEmpty()) {
	        String diskSerial = "";
			SystemInfo systemInfo = new SystemInfo();

	        HardwareAbstractionLayer hal = systemInfo.getHardware();
	        HWDiskStore[] diskStores = hal.getDiskStores();

	        if(diskStores.length > 0) {
	        	diskSerial = diskStores[0].getSerial();
	        }
	        
	        String mac = "";
	        NetworkIF[] netWorkIfs = hal.getNetworkIFs();
	        if(netWorkIfs.length > 0) {
	        	mac = netWorkIfs[0].getMacaddr();
	        }
	        
	        setUniqueHwId(diskSerial + mac);
		}

		return uniqueHwId;
	}
	
	
	@SuppressWarnings("nls")
	private String calcDemoLicenseeNumber() {
		String demoLicenseeName = "";
		
		SystemInfo systemInfo = new SystemInfo();

        HardwareAbstractionLayer hal = systemInfo.getHardware();
        HWDiskStore[] diskStores = hal.getDiskStores();

        String diskSerial = "";
        if(diskStores.length > 0) {
        	diskSerial = diskStores[0].getSerial();
        	if(diskSerial.length() > 26) {
        		diskSerial = diskSerial.substring(0, 25);
        	}
        }
        
        String mac = "";
        NetworkIF[] netWorkIfs = hal.getNetworkIFs();
        if(netWorkIfs.length > 0) {
        	mac = netWorkIfs[0].getMacaddr();
        }
        
        demoLicenseeName = DEFAULT_LICENSEE_NUMBER_PREFIX + mac + diskSerial;
		
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
		
		// if there is a cached validation result...
		if(!cache.isEmpty()) {
			// construct a validation result parser...
			ResponseParser parser = 
					new ResponseParser(
							cache.getCachedValidationResult(), 
							cache.getValidationTimestamp(), 
							getLicenseeName(), 
							cache.getValidatioAction());
			
			// and get the last successful validation information from persistence...
			Optional<PersistenceModel> optPersistedData = persistenceManager.readPersistedData();
			Instant lastSuccessTimestamp = 
					optPersistedData
					.flatMap(PersistenceModel::getLastSuccessTimestamp)
					.orElse(null);
			LicenseType lastSuccessType = 
					optPersistedData
					.flatMap(PersistenceModel::getLastSuccessLicenseType)
					.orElse(null);
			
			// create an instance of LicenseChecker from the parser and last successful info...
			checker = new CheckerImpl(parser, lastSuccessTimestamp, lastSuccessType);
			
			if(checker.isValid() && ValidateExecutor.isShutDown()) {
				// cache cannot be trusted if the validate executor is shut down.
				
				// restart the scheduler
				ValidateExecutor.startSchedule(schedulerEntity, licensee);
				
				// wait for the validation call...
				try {
					Thread.sleep(WAIT_FOR_VALIDATION_RESPONSE);
				} catch (InterruptedException e) {
					Activator.log(Status.ERROR, Messages.LicenseManager_wait_for_validation_was_interrupted, e);
					cache.reset();
				}
				
				// and reconstruct an instance of type LicenseChecker
				if(!cache.isEmpty()) {
					parser = 
							new ResponseParser(
									cache.getCachedValidationResult(), 
									cache.getValidationTimestamp(), 
									getLicenseeName(), 
									cache.getValidatioAction());
					
					checker = new CheckerImpl(parser, lastSuccessTimestamp, lastSuccessType);
				} else {
					checker = persistenceManager.vlidateUsingPersistedData();
				}
			}
			
		} else {

			// otherwise use the persisted data to create an instance of type LicenseChecker...
			checker = persistenceManager.vlidateUsingPersistedData();
		}

		return checker;
	}
	

	LicenseModel getLicenseModel() {
		return licenseModel;
	}

	void setLicenseModel(LicenseModel licenseModel) {
		this.licenseModel = licenseModel;
	}

	/**
	 * Overwrites the existing license name and number with 
	 * the given ones. From the moment of calling this method,
	 * the new licensee name and number will be used on the
	 * validation calls.
	 * 
	 * @param licenseeNumber new licensee number.
	 * @param licenseeName 	new licensee name.
	 */
	public void updateLicenseeNumber(String licenseeNumber, String licenseeName) {
		Activator.log(Status.INFO, Messages.LicenseManager_updating_licensee_credentials, null);
		setLicenseeName(licenseeName);
		setLicenseeNumber(licenseeNumber);
		PersistenceManager persistence = PersistenceManager.getInstance();
		persistence.updateLicenseeData(licenseeName, licenseeNumber);
		// re-initiate manager as a new licenseeNumber is received...
		ValidateExecutor.shutDownScheduler();
		initManager();
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

	public Object clone() throws CloneNotSupportedException {
	    throw new CloneNotSupportedException(); 
	}

	static String getFloatingProductModuleNumber() {
		return PRODUCT_MODULE_NUMBER;
	}
	
	static String getProductNumber() {
		return PRODUCT_NUMBER;
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
		
		public CheckerImpl(ResponseParser parser, Instant lastSussessTimestamp, 
				LicenseType lastSuccessType) {
			this.demoExpireation = parser.getEvaluationExpiresDate();
			this.parsedLicenseType = parser.getType();
			this.parsedLicenseStatus = parser.getLicenseStatus();
			this.valid = parser.isValid();
			this.subscriptionValid = parser.getSubscriptionStatus();
			this.timestamp = parser.getValidationTimeStamp();
			this.expirationDate =  parser.getExpirationDate();
			this.licenseeName = parser.getLicenseeName();
			this.parsedExpirationDate = parser.getExpirationDate();
			calcLicenseStatus(lastSuccessType, lastSussessTimestamp);
			
		}

		/**
		 * Calculates the {@link LicenseStatus} based on the parsed 
		 * validity of the license and the last successful validation 
		 * time-stamp.
		 * 
		 * It covers the case where the NodeLocked/Trial license is still 
		 * valid but the hardware id does not match with the one 
		 * of the first validation. Note that this method does not 
		 * calculate anything about the validity of the license.
		 */
		private void calcLicenseStatus(LicenseType lastSuccessLicenseType, Instant lastSuccessTimestamp) {
			if(isValid() || isSubscriptionValid()) {
				// if the license or the subscription is valid, then the license type/status
				// is the same as the parsed license type/status.
				this.licenseType = parsedLicenseType;
				this.licenseStatus = parsedLicenseStatus;
			} else {
				// otherwise, if the last successful validation is stored, the last successful 
				// license type is NodeLocked and the license is not expired yet, 
				// then it must be the case that the hardware id does not match
				if(lastSuccessLicenseType != null 
						&& lastSuccessTimestamp != null 
						&& lastSuccessLicenseType.equals(LicenseType.NODE_LOCKED)
						&& parsedExpirationDate != null
						&& Instant.now().isBefore(parsedExpirationDate.toInstant())
						) {
					
					this.licenseStatus = LicenseStatus.NODE_LOCKED_HW_ID_FAILURE;
					this.licenseType = LicenseType.NODE_LOCKED;
					
				} else if(this.parsedLicenseType != null 
							&& this.demoExpireation != null
							&& parsedLicenseType.equals(LicenseType.TRY_AND_BUY)
							&& Instant.now().isBefore(demoExpireation.toInstant())) {
					
					this.licenseStatus = LicenseStatus.TRIAL_HW_ID_FAILURE;
					this.licenseType = LicenseType.TRY_AND_BUY;
				} else {
					// otherwise, just keep the parsed license type/status.
					this.licenseType = parsedLicenseType;
					this.licenseStatus = parsedLicenseStatus;
				}
			}	
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
