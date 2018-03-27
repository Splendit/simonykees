package eu.jsparrow.license.netlicensing.cleanslate.validation.impl;

import static org.junit.Assert.*;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;

import org.junit.Before;
import org.junit.Test;

import eu.jsparrow.license.netlicensing.cleanslate.LicenseValidationResult;

public class NetlicensingLicenseCacheTest {

	private NetlicensingLicenseCache licenseCache;
	
	@Before
	public void setUp() {
		licenseCache = new NetlicensingLicenseCache();
	}
	
	@Test
	public void isInvalid_withNewCache_returnsTrue() {
		assertTrue(licenseCache.isInvalid());
	}
	
	@Test
	public void isInvalid_withLastAccessLongAgo_returnsTrue() {
		licenseCache.setLastUpdate(ZonedDateTime.now().minus(61, ChronoUnit.MINUTES));
		
		assertTrue(licenseCache.isInvalid());
	}
	
	@Test
	public void isInvalid_withLastAccessLessThanHourAgo_returnsFalse() {
		licenseCache.setLastUpdate(ZonedDateTime.now().minus(59, ChronoUnit.MINUTES));
		
		assertFalse(licenseCache.isInvalid());
	}
	
	@Test
	public void updateCache_withValidationResult_makesCacheValid() {
		LicenseValidationResult result = new LicenseValidationResult(null, null);
		licenseCache.updateCache(result);
		
		assertFalse(licenseCache.isInvalid());
		
		assertEquals(result, licenseCache.getLastResult());
	}
	
}
