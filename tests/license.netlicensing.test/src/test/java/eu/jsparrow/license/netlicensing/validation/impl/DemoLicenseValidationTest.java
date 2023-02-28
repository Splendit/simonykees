package eu.jsparrow.license.netlicensing.validation.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import eu.jsparrow.license.api.LicenseValidationResult;
import eu.jsparrow.license.netlicensing.model.DemoLicenseModel;

public class DemoLicenseValidationTest {

	@Test
	public void validate_withValidDemoLicense_returnsValidStatus() {
		DemoLicenseModel model = new DemoLicenseModel();
		DemoLicenseValidation validation = new DemoLicenseValidation(model);
		LicenseValidationResult result = validation.validate();
		assertEquals(model.getType(), result.getLicenseType());
		assertTrue(result.isValid());
	}
}
