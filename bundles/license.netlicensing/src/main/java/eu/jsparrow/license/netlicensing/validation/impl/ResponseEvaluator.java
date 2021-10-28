package eu.jsparrow.license.netlicensing.validation.impl;

import java.lang.invoke.MethodHandles;
import java.time.ZonedDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.labs64.netlicensing.domain.vo.ValidationResult;

import eu.jsparrow.i18n.ExceptionMessages;
import eu.jsparrow.license.api.LicenseType;
import eu.jsparrow.license.api.exception.ValidationException;
import eu.jsparrow.license.netlicensing.model.StatusDetail;
import eu.jsparrow.license.netlicensing.validation.impl.response.Parser;
import eu.jsparrow.license.netlicensing.validation.impl.response.model.FloatingResponse;
import eu.jsparrow.license.netlicensing.validation.impl.response.model.MultiFeatureResponse;
import eu.jsparrow.license.netlicensing.validation.impl.response.model.PayPerUseResponse;
import eu.jsparrow.license.netlicensing.validation.impl.response.model.SubscriptionResponse;

/**
 * Parses the NetLicensing {@link ValidationResult} using the {@link Parser} and
 * computes the {@link NetlicensingValidationResult} that it represents. A
 * {@link LicenseType#NODE_LOCKED} license is a combination of Subscription and
 * Multi-Feature, while a {@link LicenseType#FLOATING} license is a combination
 * of Subscription and Floating NetLicensing models.
 *
 */
public class ResponseEvaluator {

	private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup()
		.lookupClass());

	private static final int OFFLINE_VALIDITY_DURATION_MINUTES = 60;

	private String key;
	private Parser parser;

	public ResponseEvaluator(String key) {
		this.parser = new Parser();
		this.key = key;
	}

	public NetlicensingValidationResult evaluateResult(ValidationResult response) throws ValidationException {
		logger.debug("Evaluating validation result"); //$NON-NLS-1$
		parser.parseValidationResult(response);
		PayPerUseResponse payPerUse = parser.getPayPerUse();
		SubscriptionResponse subscription = parser.getSubscription();
		if (subscription != null) {
			if (subscription.isValid()) {
				return evaluateNonExpiredLicense();
			}
			if (payPerUse != null && payPerUse.isValid()) {
				return evaluatePayPerUse();
			}
			return evaluateExpiredLicense();

		}
		if (payPerUse != null) {
			return evaluatePayPerUse();
		}
		throw new ValidationException(ExceptionMessages.Netlicensing_validationError_noSubscriptionReceived);
	}

	/**
	 * Checks whether the parsed NetLicensing response having an invalid
	 * subscription represents any of the following:
	 * 
	 * <ul>
	 * <li>Expired {@link LicenseType.NODE_LOCKED}</li>
	 * <li>Expired {@link LicenseType.FLOATING}</li>
	 * <li>Non-expired {@link LicenseType.NODE_LOCKED} having a secret key
	 * mismatch</li>
	 * </ul>
	 * 
	 * @return the {@link NetlicensingValidationResult} created with the
	 *         evaluated information.
	 * @throws ValidationException
	 *             if the parsed response does not contain enough information to
	 *             derive the represented license type.
	 */
	private NetlicensingValidationResult evaluateExpiredLicense() throws ValidationException {

		logger.debug("Evaluating expired license"); //$NON-NLS-1$
		MultiFeatureResponse multiFeature = parser.getMultiFeature();
		SubscriptionResponse subscription = parser.getSubscription();
		ZonedDateTime expireDate = subscription.getExpires();

		if (multiFeature != null && multiFeature.isValid()) {
			return createValidationResult(LicenseType.NODE_LOCKED, false, expireDate, StatusDetail.NODE_LOCKED_EXPIRED);
		}

		FloatingResponse floating = parser.getFloating();
		if (floating != null && floating.isValid()) {
			return createValidationResult(LicenseType.FLOATING, false, expireDate, floating.getExpirationTimeStamp(),
					StatusDetail.FLOATING_EXPIRED);
		}

		if (multiFeature != null && ZonedDateTime.now()
			.isBefore(expireDate)) {
			return createValidationResult(LicenseType.NODE_LOCKED, false, expireDate,
					StatusDetail.NODE_LOCKED_HARDWARE_MISMATCH);
		}

		logger.warn("No fitting validation result found for validation response"); //$NON-NLS-1$
		throw new ValidationException(ExceptionMessages.Netlicensing_validationError_unexpectedResponse);
	}

	/**
	 * Checks whether the parsed NetLicensing response having a valid
	 * subscription represents any of the following:
	 * 
	 * <ul>
	 * <li>Valid {@link LicenseType.NODE_LOCKED}</li>
	 * <li>Valid {@link LicenseType.FLOATING}</li>
	 * <li>Non-expired {@link LicenseType.FLOATING} running out of free
	 * sessions.</li>
	 * </ul>
	 * 
	 * @return the {@linkplain NetlicensingValidationResult} represented by the
	 *         parsed response
	 * @throws ValidationException
	 *             if the parsed response doesn't represent a valid Node Locked
	 *             license and has no information about any Floating license.
	 */
	private NetlicensingValidationResult evaluateNonExpiredLicense() throws ValidationException {
		logger.debug("Evaluating non expired license"); //$NON-NLS-1$
		MultiFeatureResponse multiFeature = parser.getMultiFeature();
		SubscriptionResponse subscription = parser.getSubscription();
		ZonedDateTime expireDate = subscription.getExpires();

		if (multiFeature != null && multiFeature.isValid()) {
			return createValidationResult(LicenseType.NODE_LOCKED, true, expireDate, StatusDetail.NODE_LOCKED);
		}

		FloatingResponse floating = parser.getFloating();
		if (floating == null) {
			throw new ValidationException(ExceptionMessages.Netlicensing_validationError_noFloatingPresent);
		}

		if (floating.isValid()) {
			return createValidationResult(LicenseType.FLOATING, true, expireDate, floating.getExpirationTimeStamp(),
					StatusDetail.FLOATING);
		}

		return createValidationResult(LicenseType.FLOATING, false, expireDate, floating.getExpirationTimeStamp(),
				StatusDetail.FLOATING_OUT_OF_SESSIONS);
	}

	private NetlicensingValidationResult evaluatePayPerUse() {
		logger.debug("Evaluating Pay-Per-Use license"); //$NON-NLS-1$
		PayPerUseResponse payPerUse = parser.getPayPerUse();
		StatusDetail status = payPerUse.isValid() ? StatusDetail.PAY_PER_USE : StatusDetail.PAY_PER_USE_OUT_OF_CREDIT;
		ZonedDateTime expirationDate = ZonedDateTime.now()
			.plusYears(1);
		ZonedDateTime offlineExpiration = ZonedDateTime.now()
			.plusMinutes(OFFLINE_VALIDITY_DURATION_MINUTES);
		return new NetlicensingValidationResult(LicenseType.PAY_PER_USE, key, payPerUse.isValid(),
				status.getUserMessage(),
				expirationDate,
				offlineExpiration);
	}

	private NetlicensingValidationResult createValidationResult(LicenseType licenseType, boolean valid,
			ZonedDateTime expireDate, StatusDetail statusInfo) {
		return createValidationResult(licenseType, valid, expireDate, ZonedDateTime.now()
			.plusMinutes(OFFLINE_VALIDITY_DURATION_MINUTES), statusInfo);
	}

	private NetlicensingValidationResult createValidationResult(LicenseType licenseType, boolean valid,
			ZonedDateTime expireDate, ZonedDateTime offlineExpire, StatusDetail statusInfo) {
		logger.debug(
				"Creating validation result with type={}, valid={}, expireDate={}, offlineExpire={}, statusInfo={}", //$NON-NLS-1$
				licenseType, valid, expireDate, offlineExpire, statusInfo);

		return new NetlicensingValidationResult(licenseType, key, valid, statusInfo.getUserMessage(), expireDate,
				offlineExpire);
	}

}
