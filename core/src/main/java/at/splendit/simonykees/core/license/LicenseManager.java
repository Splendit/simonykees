package at.splendit.simonykees.core.license;

import java.net.NetworkInterface;
import java.net.SocketException;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Enumeration;
import java.util.Optional;

import org.eclipse.core.runtime.Status;

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

import com.labs64.netlicensing.domain.vo.Context;
import com.labs64.netlicensing.domain.vo.ValidationParameters;

/**
 * Manager of the validation process. Starts the validation process, caches the
 * result of the validation, and provides relevant information of the validation
 * results.
 * 
 * @author ardit.ymeri
 *
 */
public class LicenseManager {

	private static final String DEFAULT_LICENSEE_NUMBER = "trial-licensee-number"; //$NON-NLS-1$
	private static final String PRODUCT_NUMBER = "PNZNF7Y7E"; //$NON-NLS-1$
	private static final String PRODUCT_MODULE_NUMBER = "M6IS9TIWG"; //$NON-NLS-1$ product module number for floating model

	private final boolean DO_VALIDATE = true;
	private final long VALIDATE_INTERVAL_IN_SECONDS = 600; // validation interval in seconds. 10 * 60s = 6 minutes

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
		schedulerEntity = new SchedulerModel(VALIDATE_INTERVAL_IN_SECONDS, DO_VALIDATE);

		Instant now = Instant.now();

		PersistenceManager persistenceManager = PersistenceManager.getInstance();
		LicenseType licenseType;
		ZonedDateTime evaluationExpiresDate;
		ZonedDateTime expirationTimeStamp;
		String licenseeName = persistenceManager.getPersistedLicenseeName().orElse(""); //$NON-NLS-1$
		setLicenseeName(licenseeName);
		String licenseeNumber = persistenceManager.getPersistedLicenseeNumber().orElse(DEFAULT_LICENSEE_NUMBER);
		setLicenseeNumber(licenseeNumber);
		
		try {
			// make a pre-validate call to get the license model relevant information...
			ValidationResult validationResult = preValidate(PRODUCT_NUMBER, PRODUCT_MODULE_NUMBER, licenseeNumber, licenseeName);
			LicenseCheckerImpl checker = new LicenseCheckerImpl(validationResult, now, licenseeName, ValidationAction.CHECK_OUT);
			
			// cash and persist pre-validation...
			ValidationResultCache cache = ValidationResultCache.getInstance();
			cache.updateCachedResult(validationResult, licenseeName, licenseeNumber, now, ValidationAction.CHECK_OUT);
			persistenceManager.persistCachedData();
			
			// extract pre-validation result
			licenseType = checker.getType();
			evaluationExpiresDate = checker.getEvaluationExpiresDate();
			expirationTimeStamp = checker.getExpirationTimeStamp();
			
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
		logPrevalidationRequest(licenseeNumber, valParams);
		preValidationResult = LicenseeService.validate(context, licenseeNumber, valParams);
//		logPrevalidationResponse(preValidationResult);

		return preValidationResult;
	}
	
	
	private void logPrevalidationRequest(String licenseeNumber, ValidationParameters validationParams) {
		System.out.println("------Prevalidation request-----"); //$NON-NLS-1$
		System.out.println("Licensee number:" + licenseeNumber); //$NON-NLS-1$
		validationParams.getParameters().forEach((key, val) -> {
			System.out.println("key: " + key); //$NON-NLS-1$
			val.forEach((mapKey, mapVal) -> {
				System.out.println(mapKey + ":" + mapVal); //$NON-NLS-1$
			});
		});
		
	}
	
	private void logPrevalidationResponse(ValidationResult validationResult) {
		System.out.println("------Prevalidation response-----"); //$NON-NLS-1$
		System.out.println("size: " +  validationResult.getValidations().size()); //$NON-NLS-1$
		
		System.out.print(validationResult.toString());
		
		System.out.println("------------------------\n"); //$NON-NLS-1$
	}

	/**
	 * Sends a request to free one session from the session pool available
	 * for Floating License Model. Relevant only for the case when the 
	 * licensing model is Floating. 
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

	private String getUniqueNodeIdentifier() {
		if(this.uniqueHwId != null && this.uniqueHwId.isEmpty()) {
	        String diskSerial = ""; //$NON-NLS-1$
			SystemInfo systemInfo = new SystemInfo();

	        HardwareAbstractionLayer hal = systemInfo.getHardware();
	        HWDiskStore[] diskStores = hal.getDiskStores();

	        if(diskStores.length > 0) {
	        	diskSerial = diskStores[0].getSerial();
	        }
	        
	        String mac = "";  //$NON-NLS-1$
	        NetworkIF[] netWorkIfs = hal.getNetworkIFs();
	        if(netWorkIfs.length > 0) {
	        	mac = netWorkIfs[0].getMacaddr();
	        }
	        
	        setUniqueHwId(diskSerial + mac);
		}

		return uniqueHwId;
	}

	public LicenseChecker getValidationData() {
		ValidationResultCache cache = ValidationResultCache.getInstance();
		LicenseChecker checker;
		if(!cache.isEmpty()) {
			ValidationResult validationResult = cache.getCachedValidationResult();
			Instant timestamp = cache.getValidationTimestamp();
			checker = new LicenseCheckerImpl(validationResult, timestamp, getLicenseeName(), cache.getValidatioAction());
		} else {
			PersistenceManager persistenceManager = PersistenceManager.getInstance();
			checker = persistenceManager.vlidateUsingPersistedData();
		}

		return checker;
	}
	

	public LicenseModel getLicenseModel() {
		return licenseModel;
	}
	

	void setLicenseModel(LicenseModel licenseModel) {
		this.licenseModel = licenseModel;
	}

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
	
	public String getLicenseeNumber() {
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
	// TODO: override clone()
	// @Override
	// public Object clone(){
	// throw new RuntimeException();
	// }

	String getFloatingProductModuleNumber() {
		return PRODUCT_MODULE_NUMBER;
	}
	
	String getProductNumber() {
		return PRODUCT_NUMBER;
	}

}
