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

import eu.jsparrow.license.netlicensing.cleanslate.testhelper.DummyResponseGenerator;
import eu.jsparrow.license.netlicensing.cleanslate.validation.impl.response.model.Floating;
import eu.jsparrow.license.netlicensing.cleanslate.validation.impl.response.model.MultiFeature;
import eu.jsparrow.license.netlicensing.cleanslate.validation.impl.response.model.Subscription;

@SuppressWarnings("nls")
public class ParserTest {

	private Parser parser;
	private DummyResponseGenerator responseGenerator;
	ZonedDateTime now = ZonedDateTime.now();
	ZonedDateTime expireDate = ZonedDateTime.now().plusDays(1);

	@Before
	public void setUp() {
		parser = new Parser();
		now = ZonedDateTime.now();
		expireDate = ZonedDateTime.now().plusDays(1);
		responseGenerator = new DummyResponseGenerator();
	}

	/*
	 * Filtering license models from validation result...
	 */

	@Test
	public void extractModels_shouldReturnFloating() {
		ValidationResult response = responseGenerator.createFloatingResponse(now.toString(), "true", expireDate.toString());
		Floating floatingModel = parser.extractModels(response, "Floating", parser::buildFloating);
		assertNotNull(floatingModel);
	}

	@Test
	public void extractModels_shouldReturnNodeLocked() {
		ValidationResult response = responseGenerator.createFloatingResponse(now.toString(), "true", expireDate.toString());
		MultiFeature multiFeatureModel = parser.extractModels(response, "MultiFeature", parser::buildMultiFeature);
		assertNotNull(multiFeatureModel);
	}

	@Test
	public void extractModels_shouldReturnSubscriptions() {
		ValidationResult response = responseGenerator.createFloatingResponse(now.toString(), "true", expireDate.toString());
		Subscription subscription = parser.extractModels(response, "Subscription", parser::buildSubscription);
		assertNotNull(subscription);
	}

	/*
	 * Checking license type
	 */

	@Test
	public void isLicenseModel_subscription() {
		Map<String, Composition> properties = responseGenerator
			.createCompositionProperties(asList(new SimpleEntry<>("licensingModel", "Subscription")));
		boolean expected = parser.isLicenseModel(properties, "Subscription");
		assertTrue(expected);
	}

	@Test
	public void isLicenseModel_floating() {
		Map<String, Composition> properties = responseGenerator
			.createCompositionProperties(asList(new SimpleEntry<>("licensingModel", "Floating")));
		boolean expected = parser.isLicenseModel(properties, "Floating");
		assertTrue(expected);
	}

	@Test
	public void isLicenseModel_nodeLocked() {
		Map<String, Composition> properties = responseGenerator
			.createCompositionProperties(asList(new SimpleEntry<>("licensingModel", "MultiFeature")));
		boolean expected = parser.isLicenseModel(properties, "MultiFeature");
		assertTrue(expected);
	}

	@Test
	public void isLicenseModel_noLicensingModelKey() {
		Map<String, Composition> properties = responseGenerator
			.createCompositionProperties(asList(new SimpleEntry<>("noLicensingModelKey", "Floating")));
		boolean expected = parser.isLicenseModel(properties, "Floating");
		assertFalse(expected);
	}

	/*
	 * Parsing subscription license
	 */

	@Test
	public void buildSubscription_validComposition() {
		ZonedDateTime nextYear = ZonedDateTime.now()
			.plusYears(1);
		List<Entry<String, String>> propertyValues = asList(new SimpleEntry<>("expires", nextYear.toString()),
				new SimpleEntry<>("valid", "true"));
		Map<String, Composition> properties = responseGenerator.createCompositionProperties(propertyValues);

		Subscription subscription = parser.buildSubscription(properties);

		assertTrue(subscription.isValid());
		assertEquals(nextYear, subscription.getExpires());
	}

	@Test
	public void buildSubscription_missingExpires() {
		Map<String, Composition> properties = responseGenerator
			.createCompositionProperties(asList(new SimpleEntry<>("valid", "false")));

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
	 * Parsing floating license
	 */

	@Test
	public void buildFloating() {
		ZonedDateTime nextDay = ZonedDateTime.now()
			.plusDays(1);
		List<Entry<String, String>> propertyValues = asList(
				new SimpleEntry<>("expirationTimestamp", nextDay.toString()), new SimpleEntry<>("valid", "true"));
		Map<String, Composition> properties = responseGenerator.createCompositionProperties(propertyValues);

		Floating floating = parser.buildFloating(properties);

		assertTrue(floating.isValid());
		assertEquals(nextDay, floating.getExpirationTimeStamp());
	}

	@Test
	public void buildFloating_missingExpiresTimeStamp() {
		Map<String, Composition> properties = responseGenerator
			.createCompositionProperties(asList(new SimpleEntry<>("valid", "false")));

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
		Map<String, Composition> properties = responseGenerator.createMultiFeatureProperties(featureKey, valid);

		MultiFeature multifeature = parser.buildMultiFeature(properties);

		assertNotNull(multifeature);
		assertEquals(featureKey, multifeature.getFeatureName());
		assertTrue(multifeature.isValid());
	}

	@Test
	public void buildMultiFeature_missingFeature() {
		Map<String, Composition> properties = responseGenerator
			.createCompositionProperties(asList(new SimpleEntry<>("valid", "false")));
		MultiFeature multifeature = parser.buildMultiFeature(properties);
		assertNull(multifeature);
	}

}
