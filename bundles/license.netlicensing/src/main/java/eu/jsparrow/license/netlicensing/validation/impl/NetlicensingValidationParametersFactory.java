package eu.jsparrow.license.netlicensing.validation.impl;

import java.lang.invoke.MethodHandles;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.labs64.netlicensing.domain.vo.ValidationParameters;

import eu.jsparrow.license.api.LicenseType;
import eu.jsparrow.license.netlicensing.model.NetlicensingLicenseModel;

/**
 * This class is used to create validation parameters which will be sent to the
 * NetLicensing API. Depending on the {@link NetlicensingLicenseModel} that is
 * used different {@link ValidationParameters} are created.
 * 
 */
public class NetlicensingValidationParametersFactory {

	private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup()
		.lookupClass());

	private static final String SESSION_ID_KEY = "sessionId"; //$NON-NLS-1$
	private static final String ACTION_KEY = "action"; //$NON-NLS-1$
	private static final String ACTION_CHECK_OUT_VALUE = "checkOut"; //$NON-NLS-1$
	private static final String ACTION_CHECK_IN_VALUE = "checkIn"; //$NON-NLS-1$

	/**
	 * Create {@link ValidationParameters} from the given license model.
	 * 
	 * @param model
	 *            model to create validation parameters for
	 * @return validation parameters matching the data from the model
	 */
	public ValidationParameters createValidationParameters(NetlicensingLicenseModel model) {
		logger.debug("Creating validation parameters for {}", model); //$NON-NLS-1$
		LicenseType type = model.getType();
		ValidationParameters parameters;
		String secret = model.getSecret();
		if (LicenseType.FLOATING == type) {
			logger.debug("License type is floating"); //$NON-NLS-1$
			parameters = createFloatingParameters(model, ACTION_CHECK_OUT_VALUE);
		} else {
			logger.debug("License type is node-locked"); //$NON-NLS-1$
			parameters = createNodeLockedParameters(secret);
		}
		parameters.setProductNumber(model.getProductNr());
		parameters.setLicenseeName(model.getName());
		return parameters;
	}

	/**
	 * Create validation parameters executing a check in action with
	 * netlicensing. A checkin should only be possible with
	 * {@link NetlicensingLicenseModel} of floating type.
	 * 
	 * @param model
	 *            model with floating type
	 * @return validation parameters to execute a check in action with
	 *         netlicensing
	 */
	public ValidationParameters createFloatingCheckInParameters(NetlicensingLicenseModel model) {
		ValidationParameters parameters = createFloatingParameters(model, ACTION_CHECK_IN_VALUE);
		parameters.setProductNumber(model.getProductNr());
		parameters.setLicenseeName(model.getName());
		return parameters;
	}

	/**
	 * Creates the validation parameters required to get the full information
	 * about all the licenses related to a licensee.
	 * 
	 * @param model
	 *            model containing data to construct validation parameters. Must
	 *            contain a product and module number so netlicensing knows to
	 *            return only licenses for the right product
	 * @return the constructed {@link ValidationParameters}
	 */
	public ValidationParameters createVerifyParameters(NetlicensingLicenseModel model) {
		ValidationParameters parameters = new ValidationParameters();
		HashMap<String, String> params = new HashMap<>();
		params.put(SESSION_ID_KEY, model.getSecret());
		params.put(ACTION_KEY, ACTION_CHECK_OUT_VALUE);
		parameters.setProductModuleValidationParameters(model.getModuleNr(), params);
		parameters.setProductNumber(model.getProductNr());
		return parameters;
	}

	private ValidationParameters createFloatingParameters(NetlicensingLicenseModel model, String action) {
		logger.debug("Creating floating parameters for session id and action {}", action); //$NON-NLS-1$
		ValidationParameters parameters = new ValidationParameters();
		HashMap<String, String> params = new HashMap<>();
		params.put(SESSION_ID_KEY, model.getSecret());
		params.put(ACTION_KEY, action);
		parameters.setProductModuleValidationParameters(model.getModuleNr(), params);
		return parameters;

	}

	private ValidationParameters createNodeLockedParameters(String secretKey) {
		logger.debug("Creating node lock parameters"); //$NON-NLS-1$
		ValidationParameters parameters = new ValidationParameters();
		parameters.setLicenseeSecret(secretKey);
		return parameters;
	}

	public ValidationParameters createPayPerUseReserveParameters(NetlicensingLicenseModel model, int quantity) {
		ValidationParameters parameters = new ValidationParameters();
		HashMap<String, String> params = new HashMap<>();
		params.put("reserveQuantity", Integer.toString(quantity)); //$NON-NLS-1$
		parameters.setProductModuleValidationParameters(model.getModuleNr(), params);
		parameters.setProductNumber(model.getProductNr());
		parameters.setLicenseeName(model.getName());
		return parameters;
	}
}
