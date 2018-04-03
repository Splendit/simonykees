package eu.jsparrow.license.netlicensing.validation.impl;

import java.time.ZonedDateTime;

import com.labs64.netlicensing.domain.vo.ValidationResult;

import eu.jsparrow.license.api.LicenseModel;
import eu.jsparrow.license.api.LicenseValidationResult;
import eu.jsparrow.license.api.exception.ValidationException;
import eu.jsparrow.license.netlicensing.model.*;
import eu.jsparrow.license.netlicensing.validation.impl.response.Parser;
import eu.jsparrow.license.netlicensing.validation.impl.response.model.*;

public class ResponseEvaluator {

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

	public LicenseValidationResult evaluateResult(ValidationResult response) throws ValidationException {

		parser.parseValidationResult(response);

		SubscriptionResponse subscription = parser.getSubscription();

		if (subscription == null) {
			throw new ValidationException("No subscription received from license server.");
		}

		if (subscription.isValid()) {
			return evaluateNonExpiredLicense();
		} else {
			return evaluateExpiredLicense();
		}
	}

	private LicenseValidationResult evaluateExpiredLicense() throws ValidationException {

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
		
		throw new ValidationException("Unexpected response from license server.");
	}

	private LicenseValidationResult evaluateNonExpiredLicense() throws ValidationException {
		MultiFeatureResponse multiFeature = parser.getMultiFeature();
		SubscriptionResponse subscription = parser.getSubscription();
		ZonedDateTime expireDate = subscription.getExpires();

		if (multiFeature != null && multiFeature.isValid()) {
			return createValidationResult(NetlicensingLicenseType.NODE_LOCKED, true, expireDate,
					StatusDetail.NODE_LOCKED);
		}

		FloatingResponse floating = parser.getFloating();
		if (floating == null) {
			throw new ValidationException("License server response does not contain floating license.");
		}

		if (floating.isValid()) {
			return createValidationResult(NetlicensingLicenseType.FLOATING, true, expireDate,
					floating.getExpirationTimeStamp(), StatusDetail.FLOATING);
		}

		return createValidationResult(NetlicensingLicenseType.FLOATING, false, expireDate,
				floating.getExpirationTimeStamp(), StatusDetail.FLOATING_OUT_OF_SESSIONS);
	}

	private LicenseValidationResult createValidationResult(NetlicensingLicenseType licenseType, boolean valid,
			ZonedDateTime expireDate, ZonedDateTime offlineExpire, StatusDetail statusInfo) {

		String key = netlicensingModel.getKey();
		String name = netlicensingModel.getName();
		String product = netlicensingModel.getProduct();
		String secret = netlicensingModel.getSecret();

		LicenseModel model = new NetlicensingLicenseModel(licenseType, key, name, product, secret, expireDate,
				offlineExpire);
		return new LicenseValidationResult(model, valid, statusInfo.getUserMessage());
	}

	private LicenseValidationResult createValidationResult(NetlicensingLicenseType licenseType, boolean valid,
			ZonedDateTime expireDate, StatusDetail statusInfo) {

		return createValidationResult(licenseType, valid, expireDate, ZonedDateTime.now()
			.plusMinutes(OFFLINE_VALIDITY_DURATION_MINUTES), statusInfo);
	}

}
