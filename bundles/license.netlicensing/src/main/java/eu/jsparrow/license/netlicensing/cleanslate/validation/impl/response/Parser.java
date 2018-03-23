package eu.jsparrow.license.netlicensing.cleanslate.validation.impl.response;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.labs64.netlicensing.domain.vo.Composition;
import com.labs64.netlicensing.domain.vo.ValidationResult;

import eu.jsparrow.license.netlicensing.cleanslate.validation.impl.response.model.Floating;
import eu.jsparrow.license.netlicensing.cleanslate.validation.impl.response.model.LicensingModel;
import eu.jsparrow.license.netlicensing.cleanslate.validation.impl.response.model.MultiFeature;
import eu.jsparrow.license.netlicensing.cleanslate.validation.impl.response.model.Subscription;

public class Parser {

	private static final String LICENSING_MODEL_KEY = "licensingModel"; //$NON-NLS-1$

	public <T extends LicensingModel> List<T> extractModels(ValidationResult response, String model,
			Function<Composition, T> modelBuilder) {

		return response.getValidations()
			.values()
			.stream()
			.filter(composition -> isLicenseModel(composition, model))
			.map(modelBuilder)
			.collect(Collectors.toList());
	}

	public boolean isLicenseModel(Composition composition, String licensingModel) {
		Map<String, Composition> properties = composition.getProperties();
		if (!properties.containsKey(LICENSING_MODEL_KEY)) {
			return false;
		}
		Composition model = properties.get(LICENSING_MODEL_KEY);
		String value = model.getValue();
		return licensingModel.equals(value);
	}

	public Subscription buildSubscription(Composition composition) {
		Map<String, Composition> properties = composition.getProperties();
		if (!properties.containsKey(Subscription.EXPIRES_KEY)) {
			return null;
		}

		Composition expiresComposition = properties.get(Subscription.EXPIRES_KEY);
		ZonedDateTime expires = ZonedDateTime.parse(expiresComposition.getValue());

		if (!properties.containsKey(Subscription.VALID_KEY)) {
			return null;
		}
		Composition validComposition = properties.get(Subscription.VALID_KEY);
		boolean valid = Boolean.parseBoolean(validComposition.getValue());

		return new Subscription(expires, valid);
	}

	public Floating buildFloating(Composition composition) {
		Map<String, Composition> properties = composition.getProperties();
		if (!properties.containsKey(Floating.EXPIRATION_TIME_STAMP_KEY)) {
			return null;
		}

		Composition expiresTimeStampComposition = properties.get(Subscription.EXPIRES_KEY);
		ZonedDateTime expiresTimeStamp = ZonedDateTime.parse(expiresTimeStampComposition.getValue());

		if (!properties.containsKey(Subscription.VALID_KEY)) {
			return null;
		}
		Composition validComposition = properties.get(Floating.VALID_KEY);
		boolean valid = Boolean.parseBoolean(validComposition.getValue());

		return new Floating(expiresTimeStamp, valid);
	}

	public MultiFeature buildMultiFeature(Composition composition) {
		Map<String, Composition> properties = composition.getProperties();

		String feature = ""; //$NON-NLS-1$
		boolean valid = false;

		for (Map.Entry<String, Composition> entry : properties.entrySet()) {
			Composition value = entry.getValue();
			Map<String, Composition> valueProperties = value.getProperties();
			if (!valueProperties.isEmpty()) {
				Composition validValue = valueProperties.get(MultiFeature.VALID_KEY);
				valid = Boolean.getBoolean(validValue.getValue());
				feature = entry.getKey();
				break;
			}
		}

		if (feature.isEmpty()) {
			return null;
		}

		return new MultiFeature(feature, valid);
	}

}
