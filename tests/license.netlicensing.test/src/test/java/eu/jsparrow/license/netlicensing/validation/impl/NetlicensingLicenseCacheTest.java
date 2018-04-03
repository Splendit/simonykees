package eu.jsparrow.license.netlicensing.validation.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.time.ZonedDateTime;

import org.junit.Before;
import org.junit.Test;

import eu.jsparrow.license.api.LicenseValidationResult;
import eu.jsparrow.license.netlicensing.model.NetlicensingLicenseModel;

public class NetlicensingLicenseCacheTest {

	private NetlicensingLicenseCache licenseCache;

	@Before
	public void setUp() {
		licenseCache = new NetlicensingLicenseCache();
	}
	
	@Test
	public void getValidationResultFor_withNewModel_returnsNull() {
		NetlicensingLicenseModel model = createWithOfflineExpire(null);

		assertNull(licenseCache.getValidationResultFor(model));
	}

	@Test
	public void getValidationResultFor_withOfflineExpiredModel_returnsNull() {
		NetlicensingLicenseModel model = createWithOfflineExpire(ZonedDateTime.now()
			.minusDays(1));

		assertNull(licenseCache.getValidationResultFor(model));
	}
	
	@Test
	public void getValidationResultFor_withExistingResultAndOfflineInvalidModel_returnsNull() {
		NetlicensingLicenseModel model = createWithOfflineExpire(ZonedDateTime.now().minusDays(1));
		LicenseValidationResult result = new LicenseValidationResult();
		
		licenseCache.updateCache(result);

		assertNull(licenseCache.getValidationResultFor(model));
	}
	
	@Test
	public void getValidationResultFor_withExistingResultAndOfflineValidModel_returnsValidationResult() {
		NetlicensingLicenseModel model = createWithOfflineExpire(ZonedDateTime.now().plusDays(1));
		LicenseValidationResult expected = new LicenseValidationResult();
		
		licenseCache.updateCache(expected);

		assertEquals(expected, licenseCache.getValidationResultFor(model));
	}


	private NetlicensingLicenseModel createWithOfflineExpire(ZonedDateTime offlineExpire) {
		return new NetlicensingLicenseModel(null, "", "", "", "", null, offlineExpire);
	}

}
