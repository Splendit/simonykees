package eu.jsparrow.license.netlicensing.validation.impl;

import static org.junit.Assert.*;

import java.time.ZonedDateTime;

import org.junit.Before;
import org.junit.Test;

import eu.jsparrow.license.netlicensing.LicenseValidationResult;
import eu.jsparrow.license.netlicensing.model.DemoLicenseModel;
import eu.jsparrow.license.netlicensing.validation.impl.DemoLicenseValidation;

public class DemoLicenseValidationTest {

	private DemoLicenseModel model;

	private DemoLicenseValidation validation;

	@Before
	public void setUp() {
		model = new DemoLicenseModel(null);
		validation = new DemoLicenseValidation(model);
	}

	@Test
	public void validate_withValidDemoLicense_returnsValidStatus() {
		model.setExpirationDate(ZonedDateTime.now().plusDays(3));

		LicenseValidationResult result = validation.validate();
		
		assertEquals(model, result.getModel());
		assertTrue(result.getStatus().isValid());
	}

	@Test
	public void validate_withInvalidDemoLicense_returnsValidStatus() {
		model.setExpirationDate(ZonedDateTime.now().minusDays(3));

		LicenseValidationResult result = validation.validate();
		
		assertEquals(model, result.getModel());
		assertFalse(result.getStatus().isValid());
		
	}
}
