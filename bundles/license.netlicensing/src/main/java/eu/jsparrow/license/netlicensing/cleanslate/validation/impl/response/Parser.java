package eu.jsparrow.license.netlicensing.cleanslate.validation.impl.response;

import java.time.ZonedDateTime;
import java.util.Map;
import java.util.function.Function;

import com.labs64.netlicensing.domain.vo.Composition;
import com.labs64.netlicensing.domain.vo.ValidationResult;

import eu.jsparrow.license.netlicensing.cleanslate.validation.impl.response.model.FloatingResponse;
import eu.jsparrow.license.netlicensing.cleanslate.validation.impl.response.model.NetlicensingResponse;
import eu.jsparrow.license.netlicensing.cleanslate.validation.impl.response.model.MultiFeatureResponse;
import eu.jsparrow.license.netlicensing.cleanslate.validation.impl.response.model.SubscriptionResponse;

public class Parser {
	private static final String LICENSING_MODEL_KEY = "licensingModel"; //$NON-NLS-1$
	
	private SubscriptionResponse subscription;
	private FloatingResponse floating;
	private MultiFeatureResponse multiFeature;
	
	public void parseValidationResult(ValidationResult validationResult) {
		subscription = extractModels(validationResult, SubscriptionResponse.LICENSING_MODEL,
				this::buildSubscription);
		multiFeature = extractModels(validationResult, MultiFeatureResponse.LICENSING_MODEL,
				this::buildMultiFeature);
		floating = extractModels(validationResult, FloatingResponse.LICENSING_MODEL, this::buildFloating);
	}

	public <T extends NetlicensingResponse> T extractModels(ValidationResult response, String model,
			Function<Map<String, Composition>, T> responseModelBuilder) {

		return response.getValidations()
			.values()
			.stream()
			.map(Composition::getProperties)
			.filter(properties -> isLicenseModel(properties, model))
			.map(responseModelBuilder)
			.findFirst()
			.orElse(null);
	}

	public boolean isLicenseModel(Map<String, Composition> properties, String licensingModel) {
		if (!properties.containsKey(LICENSING_MODEL_KEY)) {
			return false;
		}
		Composition model = properties.get(LICENSING_MODEL_KEY);
		String value = model.getValue();
		return licensingModel.equals(value);
	}

	public SubscriptionResponse buildSubscription(Map<String, Composition> properties) {
		
		if (!properties.containsKey(NetlicensingResponse.VALID_KEY)) {
			return null;
		}
		Composition validComposition = properties.get(NetlicensingResponse.VALID_KEY);
		boolean valid = Boolean.parseBoolean(validComposition.getValue());
		
		if (!properties.containsKey(SubscriptionResponse.EXPIRES_KEY)) {
			return new SubscriptionResponse(valid);
		}

		Composition expiresComposition = properties.get(SubscriptionResponse.EXPIRES_KEY);
		ZonedDateTime expires = ZonedDateTime.parse(expiresComposition.getValue());

		return new SubscriptionResponse(expires, valid);
	}

	public FloatingResponse buildFloating(Map<String, Composition> properties) {
		if (!properties.containsKey(NetlicensingResponse.VALID_KEY)) {
			return null;
		}
		Composition validComposition = properties.get(NetlicensingResponse.VALID_KEY);
		boolean valid = Boolean.parseBoolean(validComposition.getValue());
		
		if (!properties.containsKey(FloatingResponse.EXPIRATION_TIME_STAMP_KEY)) {
			return new FloatingResponse(valid);
		}

		Composition expiresTimeStampComposition = properties.get(FloatingResponse.EXPIRATION_TIME_STAMP_KEY);
		ZonedDateTime expiresTimeStamp = ZonedDateTime.parse(expiresTimeStampComposition.getValue());

		return new FloatingResponse(expiresTimeStamp, valid);
	}

	public MultiFeatureResponse buildMultiFeature(Map<String, Composition> properties) {

		String feature = ""; //$NON-NLS-1$
		boolean valid = false;

		for (Map.Entry<String, Composition> entry : properties.entrySet()) {
			Composition value = entry.getValue();
			Map<String, Composition> valueProperties = value.getProperties();
			if (!valueProperties.isEmpty()) {
				Composition validValue = valueProperties.get(NetlicensingResponse.VALID_KEY);
				valid = Boolean.parseBoolean(validValue.getValue());
				feature = entry.getKey();
				break;
			}
		}

		if (feature.isEmpty()) {
			return null;
		}

		return new MultiFeatureResponse(feature, valid);
	}
	
	public SubscriptionResponse getSubscription() {
		return subscription;
	}

	public FloatingResponse getFloating() {
		return floating;
	}

	public MultiFeatureResponse getMultiFeature() {
		return multiFeature;
	}

}
