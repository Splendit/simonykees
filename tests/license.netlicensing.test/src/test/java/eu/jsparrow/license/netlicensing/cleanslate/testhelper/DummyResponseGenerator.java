package eu.jsparrow.license.netlicensing.cleanslate.testhelper;

import static java.util.Arrays.asList;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map.Entry;

import com.labs64.netlicensing.domain.vo.Composition;
import com.labs64.netlicensing.domain.vo.ValidationResult;

@SuppressWarnings("nls")
public class DummyResponseGenerator {

	private ZonedDateTime now = ZonedDateTime.now();
	private ZonedDateTime expiresDate = now.plusYears(1);
	private ZonedDateTime expiresTimeStamp = now.plusDays(1);

	public Map<String, Composition> createMultiFeatureProperties(String featureKey, String valid) {
		Map<String, Composition> featureProeprties = createCompositionProperties(
				asList(new SimpleEntry<>("valid", valid)));
		Composition feature = createComposition(featureProeprties);
		Map<String, Composition> properties = new HashMap<>();
		properties.put(featureKey, feature);
		properties.put("licensingModel", new Composition("MultiFeature"));
		return properties;
	}
	
	public ValidationResult createFloatingResponse(String timeStamp, String valid, String expiresDate) {
		return createFloatingResponse("true", timeStamp, valid, expiresDate);
	}
	
	public ValidationResult createFloatingResponse(String floatingValid, String timeStamp, String valid, String expiresDate) {

		Composition floating = createFloatingComposition(timeStamp, floatingValid);
		Composition nodeLocked = createMultiFeatureComposition("featureKey", "false");
		Composition subscription = createSubscriptionComposition(expiresDate, valid);

		return createValidationResult(floating, nodeLocked, subscription);
	}
	
	public ValidationResult createNodeLockedResponse(String featureKey, String valid, String expiresDate) {

		Composition nodeLocked = createMultiFeatureComposition(featureKey, "true");
		Composition subscription = createSubscriptionComposition(expiresDate, valid);

		return createValidationResult(nodeLocked, subscription);
	}

	protected ValidationResult createValidationResult(Composition floating, Composition nodeLocked,
			Composition subscription) {
		Map<String, Composition> content = new HashMap<>();
		content.put("floaing-pmn", floating);
		content.put("subscription-pmn", subscription);
		content.put("multifeature-pmn", nodeLocked);

		return crateValidationResult(content);
	}
	
	protected ValidationResult createValidationResult(Composition nodeLocked,
			Composition subscription) {
		Map<String, Composition> content = new HashMap<>();
		content.put("subscription-pmn", subscription);
		content.put("multifeature-pmn", nodeLocked);

		return crateValidationResult(content);
	}

	public Composition createSubscriptionComposition(String expiresDate, String valid) {

		List<Entry<String, String>> propertyValues = asList(new SimpleEntry<>("valid", valid),
				new SimpleEntry<>("expires", expiresDate), new SimpleEntry<>("licensingModel", "Subscription"));
		Map<String, Composition> subscriptionProperties = createCompositionProperties(propertyValues);
		return createComposition(subscriptionProperties);
	}

	public Composition createMultiFeatureComposition(String featureKey, String valid) {
		Map<String, Composition> multifeaturesProperties = createMultiFeatureProperties(featureKey, valid);
		return createComposition(multifeaturesProperties);
	}

	public Composition createFloatingComposition(String expiresTimestamp, String valid) {
		List<Entry<String, String>> propertyValues = asList(new SimpleEntry<>("valid", valid),
				new SimpleEntry<>("expirationTimestamp", expiresTimestamp),
				new SimpleEntry<>("licensingModel", "Floating"));
		Map<String, Composition> floatingProperties = createCompositionProperties(propertyValues);
		return createComposition(floatingProperties);
	}

	public ValidationResult crateValidationResult(Map<String, Composition> content) {
		ValidationResult result = new ValidationResult();

		for (Entry<String, Composition> entry : content.entrySet()) {
			result.setProductModuleValidation(entry.getKey(), entry.getValue());
		}
		return result;
	}

	public Composition createComposition(Map<String, Composition> properties) {
		Composition composition = new Composition();

		for (Entry<String, Composition> entry : properties.entrySet()) {
			composition.put(entry.getKey(), entry.getValue());
		}

		return composition;
	}

	public Map<String, Composition> createCompositionProperties(List<Entry<String, String>> keyValues) {
		Map<String, Composition> properties = new HashMap<>();
		for (Entry<String, String> entry : keyValues) {
			Composition composition = new Composition(entry.getValue());
			properties.put(entry.getKey(), composition);
		}
		return properties;
	}

}
