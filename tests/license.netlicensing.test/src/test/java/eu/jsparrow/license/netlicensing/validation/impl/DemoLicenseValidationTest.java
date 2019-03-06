package eu.jsparrow.license.netlicensing.validation.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.ZonedDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.license.api.LicenseValidationResult;
import eu.jsparrow.license.netlicensing.model.DemoLicenseModel;

public class DemoLicenseValidationTest {

	private DemoLicenseModel model;

	private DemoLicenseValidation validation;

	@BeforeEach
	public void setUp() {
		model = new DemoLicenseModel(null);
		validation = new DemoLicenseValidation(model);
	}

	@Test
	public void validate_withValidDemoLicense_returnsValidStatus() {
		model.setExpirationDate(ZonedDateTime.now().plusDays(3));

		LicenseValidationResult result = validation.validate();
		
		assertEquals(model.getType(), result.getLicenseType());
		assertTrue(result.isValid());
	}

	@Test
	public void validate_withInvalidDemoLicense_returnsValidStatus() {
		model.setExpirationDate(ZonedDateTime.now().minusDays(3));

		LicenseValidationResult result = validation.validate();
		
		assertEquals(model.getType(), result.getLicenseType());
		assertFalse(result.isValid());
		
	}
}
