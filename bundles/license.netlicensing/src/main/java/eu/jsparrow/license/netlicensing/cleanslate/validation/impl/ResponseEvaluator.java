package eu.jsparrow.license.netlicensing.cleanslate.validation.impl;

import java.time.ZonedDateTime;
import java.util.List;

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
		this.parser = createParser();
	}
	
	public LicenseValidationResult evaluateResult(ValidationResult response) {

		List<Subscription> subscriptions = parser.extractModels(response, Subscription.LICENSING_MODEL,
				parser::buildSubscription);
		List<MultiFeature> multiFeatures = parser.extractModels(response, MultiFeature.LICENSING_MODEL,
				parser::buildMultiFeature);
		List<Floating> floatings = parser.extractModels(response, Floating.LICENSING_MODEL, parser::buildFloating);

		subscriptions.sort((s1, s2) -> s1.getExpires().compareTo(s2.getExpires()));
		Subscription subscription = subscriptions.get(0);

		if (subscription.isValid()) {
			return evaluateNonExpiredLicense(multiFeatures, floatings, subscription.getExpires());
		} else {
			return evaluateExpiredLicense(multiFeatures, floatings, subscription.getExpires());
		}
	}

	protected LicenseValidationResult evaluateExpiredLicense(List<MultiFeature> multiFeatures, List<Floating> floatings,
			ZonedDateTime expires) {
		MultiFeature multiFeature = multiFeatures.stream()
			.filter(MultiFeature::isValid)
			.findAny()
			.orElse(null);
		if (multiFeature != null) {
			// nodeLocked expired
			return createValidationResult(LicenseType.NODE_LOCKED, false, expires, "Expired Node-locked");
		}

		Floating floating = floatings.stream()
			.filter(Floating::isValid)
			.findAny()
			.orElse(null);
		if (floating != null) {
			return createValidationResult(LicenseType.FLOATING, false, expires, "Expired Floating");// expired Floating
		}

		return createValidationResult(LicenseType.NONE, false, expires, "Undefined"); // undefined
	}

	protected LicenseValidationResult evaluateNonExpiredLicense(List<MultiFeature> multiFeatures, List<Floating> floatings,
			ZonedDateTime expireDate) {
		MultiFeature validMultifeature = multiFeatures.stream()
			.filter(MultiFeature::isValid)
			.findAny()
			.orElse(null);
		if (validMultifeature != null) {
			return createValidationResult(LicenseType.NODE_LOCKED, true, expireDate, "Valid node-locked");
		}

		Floating floating = floatings.stream()
			.filter(Floating::isValid)
			.findAny()
			.orElse(null);
		if (floating != null) {
			return createValidationResult(LicenseType.FLOATING, true, expireDate, "Valid floating");
		}

		return createValidationResult(LicenseType.FLOATING, false, expireDate, "Floating out of sessions");
	}

	private LicenseValidationResult createValidationResult(LicenseType nodeLocked, boolean b, ZonedDateTime expireDate,
			String string) {

		String key = netlicensingModel.getKey();
		String name = netlicensingModel.getName();
		String product = netlicensingModel.getProduct();
		String secret = netlicensingModel.getSecret();

		LicenseModel model = new NetlicensingLicenseModel(nodeLocked, key, name, product, secret, expireDate);
		ValidationStatus status = new ValidationStatus(b, string);

		return new LicenseValidationResult(model, status);
	}
	
	private Parser createParser() {
		return new Parser();
	}

}
