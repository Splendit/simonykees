package at.splendit.simonykees.core.license;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

public class LicenseManagerTest {
	
	@Test
	public void testInitLicenseManager() {
		LicenseManager instance = LicenseManager.getInstance();
		LicenseModel licenseModel = instance.getLicenseModel();
		LicenseeEntity licensee = instance.getLicensee();
		
		assertNotNull(licensee);
		assertNotNull(licenseModel);
		assertNotNull(licenseModel.getType());
		assertNotNull(licensee.getLicenseeName());
		assertNotNull(licensee.getLicenseeNumber());
		
	}
}
