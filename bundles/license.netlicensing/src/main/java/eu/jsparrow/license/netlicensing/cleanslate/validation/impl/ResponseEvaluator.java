package eu.jsparrow.license.netlicensing.cleanslate.validation.impl;

import java.time.ZonedDateTime;

import com.labs64.netlicensing.domain.vo.ValidationResult;

import eu.jsparrow.license.netlicensing.cleanslate.LicenseValidationResult;
import eu.jsparrow.license.netlicensing.cleanslate.model.LicenseModel;
import eu.jsparrow.license.netlicensing.cleanslate.model.NetlicensingLicenseType;
import eu.jsparrow.license.netlicensing.cleanslate.model.StatusDetail;
import eu.jsparrow.license.netlicensing.cleanslate.model.NetlicensingLicenseModel;
import eu.jsparrow.license.netlicensing.cleanslate.validation.ValidationStatus;
import eu.jsparrow.license.netlicensing.cleanslate.validation.impl.response.Parser;
import eu.jsparrow.license.netlicensing.cleanslate.validation.impl.response.model.Floating;
import eu.jsparrow.license.netlicensing.cleanslate.validation.impl.response.model.MultiFeature;
import eu.jsparrow.license.netlicensing.cleanslate.validation.impl.response.model.Subscription;

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

	public LicenseValidationResult evaluateResult(ValidationResult response) {

		parser.parseValidationResult(response);

		Subscription subscription = parser.getSubscription();

		if (subscription == null) {
			return createUndefinedValidationResult();
		}

		if (subscription.isValid()) {
			return evaluateNonExpiredLicense();
		} else {
			return evaluateExpiredLicense();
		}
	}

	private LicenseValidationResult evaluateExpiredLicense() {

		MultiFeature multiFeature = parser.getMultiFeature();
		Subscription subscription = parser.getSubscription();
		ZonedDateTime expireDate = subscription.getExpires();

		if (multiFeature != null && multiFeature.isValid()) {
			return createValidationResult(NetlicensingLicenseType.NODE_LOCKED, false, expireDate,
					StatusDetail.NODE_LOCKED_EXPIRED);
		}

		Floating floating = parser.getFloating();
		if (floating != null && floating.isValid()) {
			return createValidationResult(NetlicensingLicenseType.FLOATING, false, expireDate, floating.getExpirationTimeStamp(),
					StatusDetail.FLOATING_EXPIRED);
		}

		if (multiFeature != null && ZonedDateTime.now().isBefore(expireDate)) {
			return createValidationResult(NetlicensingLicenseType.NODE_LOCKED, false, expireDate,
					StatusDetail.NODE_LOCKED_HARDWARE_MISMATCH);
		}

		return createUndefinedValidationResult();
	}

	private LicenseValidationResult evaluateNonExpiredLicense() {
		MultiFeature multiFeature = parser.getMultiFeature();
		Subscription subscription = parser.getSubscription();
		ZonedDateTime expireDate = subscription.getExpires();

		if (multiFeature != null && multiFeature.isValid()) {
			return createValidationResult(NetlicensingLicenseType.NODE_LOCKED, true, expireDate, StatusDetail.NODE_LOCKED);
		}
		
		Floating floating = parser.getFloating();
		if(floating == null) {
			return createUndefinedValidationResult();
		}

		if (floating.isValid()) {
			return createValidationResult(NetlicensingLicenseType.FLOATING, true, expireDate,
					floating.getExpirationTimeStamp(), StatusDetail.FLOATING);
		}

		return createValidationResult(NetlicensingLicenseType.FLOATING, false, expireDate, floating.getExpirationTimeStamp(),
				StatusDetail.FLOATING_OUT_OF_SESSIONS);
	}

	private LicenseValidationResult createValidationResult(NetlicensingLicenseType licenseType, boolean valid,
			ZonedDateTime expireDate, ZonedDateTime offlineExpire, StatusDetail statusInfo) {

		String key = netlicensingModel.getKey();
		String name = netlicensingModel.getName();
		String product = netlicensingModel.getProduct();
		String secret = netlicensingModel.getSecret();

		LicenseModel model = new NetlicensingLicenseModel(licenseType, key, name, product, secret, expireDate,
				offlineExpire);
		ValidationStatus status = new ValidationStatus(valid, statusInfo);

		return new LicenseValidationResult(model, status);
	}
	
	private LicenseValidationResult createValidationResult(NetlicensingLicenseType licenseType, boolean valid,
			ZonedDateTime expireDate, StatusDetail statusInfo) {

		return createValidationResult(licenseType, valid, expireDate, ZonedDateTime.now().plusMinutes(OFFLINE_VALIDITY_DURATION_MINUTES), statusInfo);
	}
	
	private LicenseValidationResult createUndefinedValidationResult() {
		
		ValidationStatus status = new ValidationStatus(false, StatusDetail.UNDEFINED);
		LicenseModel model = new NetlicensingLicenseModel(netlicensingModel.getType(), netlicensingModel.getKey(), netlicensingModel.getSecret());
		return new LicenseValidationResult(model, status);
	}
}
