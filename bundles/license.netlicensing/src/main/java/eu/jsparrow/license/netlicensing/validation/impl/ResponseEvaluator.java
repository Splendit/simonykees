package eu.jsparrow.license.netlicensing.validation.impl;

import java.lang.invoke.MethodHandles;
import java.time.ZonedDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.labs64.netlicensing.domain.vo.ValidationResult;

import eu.jsparrow.i18n.ExceptionMessages;
import eu.jsparrow.license.api.LicenseModel;
import eu.jsparrow.license.api.exception.ValidationException;
import eu.jsparrow.license.netlicensing.model.*;
import eu.jsparrow.license.netlicensing.validation.impl.response.Parser;
import eu.jsparrow.license.netlicensing.validation.impl.response.model.*;

public class ResponseEvaluator {

	private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup()
		.lookupClass());

	private static final int OFFLINE_VALIDITY_DURATION_MINUTES = 60;

	private NetlicensingLicenseModel netlicensingModel;

	private Parser parser;

	public ResponseEvaluator(NetlicensingLicenseModel model) {
		this.netlicensingModel = model;
		this.parser = new Parser();
	}

	public NetlicensingLicenseModel getLicensingModel() {
		return this.netlicensingModel;
	}

	public NetlicensingValidationResult evaluateResult(ValidationResult response) throws ValidationException {
		logger.debug("Evaluating {}", response); //$NON-NLS-1$
		parser.parseValidationResult(response);

		SubscriptionResponse subscription = parser.getSubscription();

		logger.debug("Received subscription {}", subscription); //$NON-NLS-1$
		if (subscription == null) {
			throw new ValidationException(ExceptionMessages.Netlicensing_validationError_noSubscriptionReceived);
		}

		if (subscription.isValid()) {
			return evaluateNonExpiredLicense();
		} else {
			return evaluateExpiredLicense();
		}
	}

	private NetlicensingValidationResult evaluateExpiredLicense() throws ValidationException {
		logger.debug("Evaluating expired license"); //$NON-NLS-1$
		MultiFeatureResponse multiFeature = parser.getMultiFeature();
		SubscriptionResponse subscription = parser.getSubscription();
		ZonedDateTime expireDate = subscription.getExpires();

		if (multiFeature != null && multiFeature.isValid()) {
			return createValidationResult(NetlicensingLicenseType.NODE_LOCKED, false, expireDate,
					StatusDetail.NODE_LOCKED_EXPIRED);
		}

		FloatingResponse floating = parser.getFloating();
		if (floating != null && floating.isValid()) {
			return createValidationResult(NetlicensingLicenseType.FLOATING, false, expireDate,
					floating.getExpirationTimeStamp(), StatusDetail.FLOATING_EXPIRED);
		}

		if (multiFeature != null && ZonedDateTime.now()
			.isBefore(expireDate)) {
			return createValidationResult(NetlicensingLicenseType.NODE_LOCKED, false, expireDate,
					StatusDetail.NODE_LOCKED_HARDWARE_MISMATCH);
		}

		logger.warn("No fitting validation result found for validation response"); //$NON-NLS-1$
		throw new ValidationException(ExceptionMessages.Netlicensing_validationError_unexpectedResponse);
	}

	private NetlicensingValidationResult evaluateNonExpiredLicense() throws ValidationException {
		logger.debug("Evaluating non expired license"); //$NON-NLS-1$
		MultiFeatureResponse multiFeature = parser.getMultiFeature();
		SubscriptionResponse subscription = parser.getSubscription();
		ZonedDateTime expireDate = subscription.getExpires();

		if (multiFeature != null && multiFeature.isValid()) {
			return createValidationResult(NetlicensingLicenseType.NODE_LOCKED, true, expireDate,
					StatusDetail.NODE_LOCKED);
		}

		FloatingResponse floating = parser.getFloating();
		if (floating == null) {
			throw new ValidationException(ExceptionMessages.Netlicensing_validationError_noFloatingPresent);
		}

		if (floating.isValid()) {
			return createValidationResult(NetlicensingLicenseType.FLOATING, true, expireDate,
					floating.getExpirationTimeStamp(), StatusDetail.FLOATING);
		}

		return createValidationResult(NetlicensingLicenseType.FLOATING, false, expireDate,
				floating.getExpirationTimeStamp(), StatusDetail.FLOATING_OUT_OF_SESSIONS);
	}

	private NetlicensingValidationResult createValidationResult(NetlicensingLicenseType licenseType, boolean valid,
			ZonedDateTime expireDate, StatusDetail statusInfo) {
		return createValidationResult(licenseType, valid, expireDate, ZonedDateTime.now()
			.plusMinutes(OFFLINE_VALIDITY_DURATION_MINUTES), statusInfo);
	}

	private NetlicensingValidationResult createValidationResult(NetlicensingLicenseType licenseType, boolean valid,
			ZonedDateTime expireDate, ZonedDateTime offlineExpire, StatusDetail statusInfo) {
		logger.debug(
				"Creating validation result with type={}, valid={}, expireDate={}, offlineExpire={},statusInfo ={}", //$NON-NLS-1$
				licenseType, valid, expireDate, offlineExpire, statusInfo);

		String key = netlicensingModel.getKey();
		String name = netlicensingModel.getName();
		String product = netlicensingModel.getProduct();
		String secret = netlicensingModel.getSecret();

		LicenseModel model = new NetlicensingLicenseModel(licenseType, key, name, product, secret, expireDate);
		return new NetlicensingValidationResult(model, valid, statusInfo.getUserMessage(), offlineExpire);
	}

}
