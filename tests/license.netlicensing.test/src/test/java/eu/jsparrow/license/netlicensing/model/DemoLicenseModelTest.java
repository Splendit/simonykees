package eu.jsparrow.license.netlicensing.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Month;

import org.junit.jupiter.api.Test;

import eu.jsparrow.license.api.LicenseType;

class DemoLicenseModelTest {
	
	@Test
	void testGetExpirationDate() {
		DemoLicenseModel model = new DemoLicenseModel();
		assertEquals(999999999, model.getExpirationDate().getYear());
		assertEquals(Month.DECEMBER, model.getExpirationDate().getMonth());
	}
	
	
	@Test
	void testGetType() {
		DemoLicenseModel model = new DemoLicenseModel();
		assertEquals(LicenseType.DEMO, model.getType());
	}
}
