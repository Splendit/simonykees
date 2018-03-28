package eu.jsparrow.license.netlicensing.cleanslate.validation.impl;

import java.time.ZonedDateTime;

import com.labs64.netlicensing.domain.vo.ValidationResult;

import eu.jsparrow.license.netlicensing.cleanslate.LicenseValidationResult;
import eu.jsparrow.license.netlicensing.cleanslate.model.LicenseModel;
import eu.jsparrow.license.netlicensing.cleanslate.model.LicenseType;
import eu.jsparrow.license.netlicensing.cleanslate.model.NetlicensingLicenseModel;
import eu.jsparrow.license.netlicensing.cleanslate.validation.ValidationStatus;
import eu.jsparrow.license.netlicensing.cleanslate.validation.impl.response.Parser;
import eu.jsparrow.license.netlicensing.cleanslate.validation.impl.response.model.Floating;
import eu.jsparrow.license.netlicensing.cleanslate.validation.impl.response.model.MultiFeature;
import eu.jsparrow.license.netlicensing.cleanslate.validation.impl.response.model.Subscription;

public class ResponseEvaluator {

	private NetlicensingLicenseModel netlicensingModel;
	private Parser parser;

	public ResponseEvaluator(NetlicensingLicenseModel model) {
		this.netlicensingModel = model;
		this.parser = new Parser();
	}
	
	public LicenseValidationResult evaluateResult(ValidationResult response) {

		parser.parseValidationResult(response);

		Subscription subscription = parser.getSubscription();

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
			// nodeLocked expired
			return createValidationResult(LicenseType.NODE_LOCKED, false, expireDate, "Expired Node-locked");
		}

		Floating floating = parser.getFloating();
		if (floating != null && floating.isValid()) {
			return createValidationResult(LicenseType.FLOATING, false, expireDate, "Expired Floating");// expired Floating
		}

		return createValidationResult(LicenseType.NONE, false, expireDate, "Undefined"); // undefined
	}

	private LicenseValidationResult evaluateNonExpiredLicense() {
		MultiFeature multiFeature = parser.getMultiFeature();
		Subscription subscription = parser.getSubscription();
		ZonedDateTime expireDate = subscription.getExpires();
		
		if (multiFeature != null && multiFeature.isValid()) {
			return createValidationResult(LicenseType.NODE_LOCKED, true, expireDate, "Valid node-locked");
		}

		Floating floating = parser.getFloating();
		if (floating != null && floating.isValid()) {
			return createValidationResult(LicenseType.FLOATING, true, expireDate, "Valid floating");
		}

		return createValidationResult(LicenseType.FLOATING, false, expireDate, "Floating out of sessions");
	}

	private LicenseValidationResult createValidationResult(LicenseType nodeLocked, boolean b, ZonedDateTime expireDate,
			String statusInfo) {

		String key = netlicensingModel.getKey();
		String name = netlicensingModel.getName();
		String product = netlicensingModel.getProduct();
		String secret = netlicensingModel.getSecret();

		LicenseModel model = new NetlicensingLicenseModel(nodeLocked, key, name, product, secret, expireDate);
		ValidationStatus status = new ValidationStatus(b, statusInfo);

		return new LicenseValidationResult(model, status);
	}

}
