package eu.jsparrow.license.netlicensing.validation.impl.response;

import java.lang.invoke.MethodHandles;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.labs64.netlicensing.domain.vo.Composition;
import com.labs64.netlicensing.domain.vo.ValidationResult;

import eu.jsparrow.license.netlicensing.validation.impl.response.model.*;

/**
 * Parses a {@link ValidationResult} into a number of
 * {@link NetlicensingResponse}s. A ValidationResult from NetLicensing contains
 * all licenses for a given licensee. The parser filters out the ones that are
 * relevant to us and transforms them into objects.
 */
public class Parser {

	private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup()
		.lookupClass());

	private static final String LICENSING_MODEL_KEY = "licensingModel"; //$NON-NLS-1$

	private SubscriptionResponse subscription;
	private FloatingResponse floating;
	private MultiFeatureResponse multiFeature;
	private PayPerUseResponse payPerUse;

	public void parseValidationResult(ValidationResult validationResult) {
		logger.debug("Parsing validation result"); //$NON-NLS-1$
		payPerUse = extractModels(validationResult, PayPerUseResponse.LICENSING_MODEL, this::buildPayPerUse);
		subscription = extractModels(validationResult, SubscriptionResponse.LICENSING_MODEL, this::buildSubscription);
		multiFeature = extractModels(validationResult, MultiFeatureResponse.LICENSING_MODEL, this::buildMultiFeature);
		floating = extractModels(validationResult, FloatingResponse.LICENSING_MODEL, this::buildFloating);
	}

	public <T extends NetlicensingResponse> T extractModels(ValidationResult response, String model,
			Function<Map<String, Composition>, T> responseModelBuilder) {
		logger.debug("Extracting models of type '{}'", model); //$NON-NLS-1$
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

	public PayPerUseResponse buildPayPerUse(Map<String, Composition> properties) {

		if (!properties.containsKey(NetlicensingResponse.VALID_KEY)) {
			return null;
		}
		Composition validComposition = properties.get(NetlicensingResponse.VALID_KEY);
		boolean valid = Boolean.parseBoolean(validComposition.getValue());

		if (!properties.containsKey(PayPerUseResponse.REMAINING_QUANTITY_KEY)) {
			return new PayPerUseResponse(valid);
		}

		Composition remainingQuantityComposition = properties.get(PayPerUseResponse.REMAINING_QUANTITY_KEY);
		Integer remainingQuantity = Integer.parseInt(remainingQuantityComposition.getValue());

		return new PayPerUseResponse(remainingQuantity, valid);
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

	public PayPerUseResponse getPayPerUse() {
		return payPerUse;
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
