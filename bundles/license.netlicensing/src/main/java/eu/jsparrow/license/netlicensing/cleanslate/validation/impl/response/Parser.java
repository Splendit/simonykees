package eu.jsparrow.license.netlicensing.cleanslate.validation.impl.response;

import java.time.ZonedDateTime;
import java.util.Map;
import java.util.function.Function;

import com.labs64.netlicensing.domain.vo.Composition;
import com.labs64.netlicensing.domain.vo.ValidationResult;

import eu.jsparrow.license.netlicensing.cleanslate.validation.impl.response.model.Floating;
import eu.jsparrow.license.netlicensing.cleanslate.validation.impl.response.model.LicensingModel;
import eu.jsparrow.license.netlicensing.cleanslate.validation.impl.response.model.MultiFeature;
import eu.jsparrow.license.netlicensing.cleanslate.validation.impl.response.model.Subscription;

public class Parser {
	private static final String LICENSING_MODEL_KEY = "licensingModel"; //$NON-NLS-1$
	
	private Subscription subscription;
	private Floating floating;
	private MultiFeature multiFeature;
	
	public void parseValidationResult(ValidationResult validationResult) {
		subscription = extractModels(validationResult, Subscription.LICENSING_MODEL,
				this::buildSubscription);
		multiFeature = extractModels(validationResult, MultiFeature.LICENSING_MODEL,
				this::buildMultiFeature);
		floating = extractModels(validationResult, Floating.LICENSING_MODEL, this::buildFloating);
	}

	public <T extends LicensingModel> T extractModels(ValidationResult response, String model,
			Function<Map<String, Composition>, T> modelBuilder) {

		return response.getValidations()
			.values()
			.stream()
			.map(Composition::getProperties)
			.filter(properties -> isLicenseModel(properties, model))
			.map(modelBuilder)
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

	public Subscription buildSubscription(Map<String, Composition> properties) {
		
		if (!properties.containsKey(LicensingModel.VALID_KEY)) {
			return null;
		}
		Composition validComposition = properties.get(LicensingModel.VALID_KEY);
		boolean valid = Boolean.parseBoolean(validComposition.getValue());
		
		if (!properties.containsKey(Subscription.EXPIRES_KEY)) {
			return new Subscription(valid);
		}

		Composition expiresComposition = properties.get(Subscription.EXPIRES_KEY);
		ZonedDateTime expires = ZonedDateTime.parse(expiresComposition.getValue());

		return new Subscription(expires, valid);
	}

	public Floating buildFloating(Map<String, Composition> properties) {
		if (!properties.containsKey(LicensingModel.VALID_KEY)) {
			return null;
		}
		Composition validComposition = properties.get(LicensingModel.VALID_KEY);
		boolean valid = Boolean.parseBoolean(validComposition.getValue());
		
		if (!properties.containsKey(Floating.EXPIRATION_TIME_STAMP_KEY)) {
			return new Floating(valid);
		}

		Composition expiresTimeStampComposition = properties.get(Floating.EXPIRATION_TIME_STAMP_KEY);
		ZonedDateTime expiresTimeStamp = ZonedDateTime.parse(expiresTimeStampComposition.getValue());

		return new Floating(expiresTimeStamp, valid);
	}

	public MultiFeature buildMultiFeature(Map<String, Composition> properties) {

		String feature = ""; //$NON-NLS-1$
		boolean valid = false;

		for (Map.Entry<String, Composition> entry : properties.entrySet()) {
			Composition value = entry.getValue();
			Map<String, Composition> valueProperties = value.getProperties();
			if (!valueProperties.isEmpty()) {
				Composition validValue = valueProperties.get(LicensingModel.VALID_KEY);
				valid = Boolean.parseBoolean(validValue.getValue());
				feature = entry.getKey();
				break;
			}
		}

		if (feature.isEmpty()) {
			return null;
		}

		return new MultiFeature(feature, valid);
	}
	
	public Subscription getSubscription() {
		return subscription;
	}

	public Floating getFloating() {
		return floating;
	}

	public MultiFeature getMultiFeature() {
		return multiFeature;
	}

}
