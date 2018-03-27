package eu.jsparrow.license.netlicensing.cleanslate.validation.impl.response;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static java.util.AbstractMap.SimpleEntry;
import static java.util.Map.Entry;
import static java.util.Arrays.asList;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.labs64.netlicensing.domain.vo.Composition;
import com.labs64.netlicensing.domain.vo.ValidationResult;

import eu.jsparrow.license.netlicensing.cleanslate.validation.impl.response.model.Floating;
import eu.jsparrow.license.netlicensing.cleanslate.validation.impl.response.model.MultiFeature;
import eu.jsparrow.license.netlicensing.cleanslate.validation.impl.response.model.Subscription;

@SuppressWarnings("nls")
public class ParserTest {
	
	private Parser parser;
	
	@Before
	public void setUp() {
		parser = new Parser();
	}
	
	/*
	 * Filtering license models from validation result...
	 */
	
	@Test
	public void extractModels_shouldReturnFloating() {
		String expiresDate = ZonedDateTime.now().plusYears(1).toString();
		String expiresTimestamp = ZonedDateTime.now().plusDays(1).toString();
		ValidationResult response = createValidationResponse("true", expiresTimestamp, "featureKey", "false", expiresDate, "true");
		
		Floating floatingModel = parser.extractModels(response, "Floating", parser::buildFloating);
		
		assertNotNull(floatingModel);
	}
	
	@Test
	public void extractModels_shouldReturnNodeLocked() {
		String expiresDate = ZonedDateTime.now().plusYears(1).toString();
		String expiresTimestamp = ZonedDateTime.now().plusDays(1).toString();

		String featureKey = "featureKey";
		ValidationResult response = createValidationResponse("false", expiresTimestamp, featureKey, "true", expiresDate, "true");
		
		MultiFeature multiFeatureModel = parser.extractModels(response, "MultiFeature", parser::buildMultiFeature);
		
		assertNotNull(multiFeatureModel);
	}
	
	@Test
	public void extractModels_shouldReturnSubscriptions() {
		String expiresDate = ZonedDateTime.now().plusYears(1).toString();
		String expiresTimestamp = ZonedDateTime.now().plusDays(1).toString();

		ValidationResult response = createValidationResponse("false", expiresTimestamp, "featureKey", "true", expiresDate, "true");
		
		Subscription subscription = parser.extractModels(response, "Subscription", parser::buildSubscription);
		assertNotNull(subscription);
	}
	
	/*
	 * Checking license type
	 */

	@Test
	public void isLicenseModel_subscription() {
		Map<String, Composition> properties = createCompositionProperties(asList(new SimpleEntry<>("licensingModel", "Subscription")));
		boolean expected = parser.isLicenseModel(properties, "Subscription");
		assertTrue(expected);
	}
	
	@Test
	public void isLicenseModel_floating() {
		Map<String, Composition> properties = createCompositionProperties(asList(new SimpleEntry<>("licensingModel", "Floating")));
		boolean expected = parser.isLicenseModel(properties, "Floating");
		assertTrue(expected);
	}
	
	@Test
	public void isLicenseModel_nodeLocked() {
		Map<String, Composition> properties = createCompositionProperties(asList(new SimpleEntry<>("licensingModel", "MultiFeature")));
		boolean expected = parser.isLicenseModel(properties, "MultiFeature");
		assertTrue(expected);
	}
	
	@Test
	public void isLicenseModel_noLicensingModelKey() {
		Map<String, Composition> properties = createCompositionProperties(asList(new SimpleEntry<>("noLicensingModelKey", "Floating")));
		boolean expected = parser.isLicenseModel(properties, "Floating");
		assertFalse(expected);
	}
	
	/*
	 * Parsing subscription license
	 */
	
	@Test
	public void buildSubscription_validComposition() {
		ZonedDateTime nextYear = ZonedDateTime.now().plusYears(1);
		List<Entry<String, String>> propertyValues = asList(
				new SimpleEntry<>("expires", nextYear.toString()), new SimpleEntry<>("valid", "true"));
		Map<String, Composition> properties = createCompositionProperties(propertyValues);
				
		Subscription subscription = parser.buildSubscription(properties);
		
		assertTrue(subscription.isValid());
		assertEquals(nextYear, subscription.getExpires());
	}

	@Test
	public void buildSubscription_missingExpires() {
		Map<String, Composition> properties = createCompositionProperties(asList(new SimpleEntry<>("valid", "false")));
		
		Subscription subscription = parser.buildSubscription(properties);
		
		assertFalse(subscription.isValid());
		assertNull(subscription.getExpires());
	}
	
	@Test
	public void buildSubscription_missingValid() {
		Map<String, Composition> properties = new HashMap<>();
		Subscription subscription = parser.buildSubscription(properties);
		assertNull(subscription);
	}
	
	/*
	 * Parsing floaing license
	 */
	
	@Test
	public void buildFloating() {
		ZonedDateTime nextDay = ZonedDateTime.now().plusDays(1);
		List<Entry<String, String>> propertyValues = asList(new SimpleEntry<>("expirationTimestamp", nextDay.toString()), new SimpleEntry<>("valid", "true"));
		Map<String, Composition> properties = createCompositionProperties(propertyValues);
		
		Floating floating = parser.buildFloating(properties);
		
		assertTrue(floating.isValid());
		assertEquals(nextDay, floating.getExpirationTimeStamp());
	}
	
	@Test
	public void buildFloating_missingExpiresTimeStamp() {
		Map<String, Composition> properties = createCompositionProperties(asList(new SimpleEntry<>("valid", "false")));
		
		Floating floating = parser.buildFloating(properties);
		
		assertFalse(floating.isValid());
		assertNull(floating.getExpirationTimeStamp());
	}
	
	@Test
	public void buildFloating_missingValidKey() {
		Map<String, Composition> properties = new HashMap<>();
		Floating floating = parser.buildFloating(properties);
		assertNull(floating);
	}
	
	/*
	 * Parsing node-locked license
	 */
	
	@Test
	public void buildMultiFeature() {
		String featureKey = "featureKey";
		String valid = "true";
		Map<String, Composition> properties = createMultiFeatureProperties(featureKey, valid);
		
		MultiFeature multifeature = parser.buildMultiFeature(properties);
		
		assertNotNull(multifeature);
		assertEquals(featureKey, multifeature.getFeatureName());
		assertTrue(multifeature.isValid());
	}
	
	@Test
	public void buildMultiFeature_missingFeature() {
		Map<String, Composition> properties = createCompositionProperties(asList(new SimpleEntry<>("valid", "false")));
		MultiFeature multifeature = parser.buildMultiFeature(properties);
		assertNull(multifeature);
	}
	
	protected Map<String, Composition> createMultiFeatureProperties(String featureKey, String valid) {
		Map<String, Composition> properties = new HashMap<>();
		Composition feature = new Composition();
		Map<String, Composition> featureProeprties = feature.getProperties();
		featureProeprties.putAll(createCompositionProperties(asList(new SimpleEntry<>("valid", valid))));
		properties.put(featureKey, feature);
		properties.put("licensingModel", new Composition("MultiFeature"));
		return properties;
	}
	
	private ValidationResult createValidationResponse(String floatingValid, String expiresTimestamp,
			String featureKey, String featureValid, String expiresDate, String subscriptionValid) {
		ValidationResult result = new ValidationResult();

		Composition floatingComposition = new Composition();
		Map<String, Composition> floatingProperties = floatingComposition.getProperties();
		floatingProperties.putAll(createCompositionProperties(asList(new SimpleEntry<>("valid", floatingValid), new SimpleEntry<>("expirationTimestamp", expiresTimestamp), new SimpleEntry<>("licensingModel", "Floating"))));		
		result.setProductModuleValidation("floating-pmn", floatingComposition);
		
		Composition multifeatureComposition = new Composition();
		Map<String, Composition> multifeaturesProperties = multifeatureComposition.getProperties();
		multifeaturesProperties.putAll(createMultiFeatureProperties(featureKey, featureValid));		
		result.setProductModuleValidation("multifeature-pmn", multifeatureComposition);
		
		Composition subscriptionComposition = new Composition();
		Map<String, Composition> subscriptionProperties = subscriptionComposition.getProperties();
		subscriptionProperties.putAll(createCompositionProperties(asList(new SimpleEntry<>( "valid", subscriptionValid), new SimpleEntry<>("expires", expiresDate), new SimpleEntry<>("licensingModel", "Subscription"))));		
		result.setProductModuleValidation("subscription-pmn", subscriptionComposition);
		
		return result;
	}
	
	private Map<String, Composition> createCompositionProperties(List<Entry<String, String>> keyValues) {
		Map<String, Composition> properties = new HashMap<>();
		for(Entry<String, String> entry : keyValues) {
			Composition composition = new Composition(entry.getValue());
			properties.put(entry.getKey(), composition);
		}
		return properties;
	}

}
