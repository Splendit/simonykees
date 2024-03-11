package eu.jsparrow.license.netlicensing;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.license.api.LicenseType;
import eu.jsparrow.license.netlicensing.model.DemoLicenseModel;
import eu.jsparrow.license.netlicensing.model.NetlicensingLicenseModel;

public class NetlicensingLicenseServiceTest {

	private NetlicensingLicenseService netlicensingLicenseValidation;

	@BeforeEach
	public void setUp() {
		netlicensingLicenseValidation = new NetlicensingLicenseService();
	}

	@Test
	public void test_verifySecretDemoLicense_shouldReturnTrue() {
		String secret = "HARDWARE-INFORMATION-ID";
		DemoLicenseModel licenseMode = new DemoLicenseModel();

		boolean valid = netlicensingLicenseValidation.verifySecretKey(licenseMode, secret);

		assertTrue(valid);
	}

	@Test
	public void test_verifySecret_shouldReturnTrue() {
		String secret = "HARDWARE-INFORMATION-ID";
		NetlicensingLicenseModel licenseMode = new NetlicensingLicenseModel("key", secret, "product-nr", "module-nr",
				LicenseType.NODE_LOCKED);

		boolean valid = netlicensingLicenseValidation.verifySecretKey(licenseMode, secret);

		assertTrue(valid);
	}

	@Test
	public void test_verifySecret_shouldReturnFalse() {
		String expectedSecret = "HARDWARE-INFORMATION-ID";
		String actualSecret = "ACTUAL-SECRET-ID-DIFFERENT-FROM-ORIGINAL";
		NetlicensingLicenseModel licenseMode = new NetlicensingLicenseModel("key", expectedSecret, "product-nr",
				"module-nr", LicenseType.NODE_LOCKED);

		boolean valid = netlicensingLicenseValidation.verifySecretKey(licenseMode, actualSecret);

		assertFalse(valid);
	}

}
