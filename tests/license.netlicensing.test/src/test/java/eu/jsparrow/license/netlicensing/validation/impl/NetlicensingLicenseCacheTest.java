package eu.jsparrow.license.netlicensing.validation.impl;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.ZonedDateTime;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.license.api.LicenseValidationResult;
import eu.jsparrow.license.netlicensing.testhelper.NetlicensingValidationResultFactory;

class NetlicensingLicenseCacheTest {

	private NetlicensingLicenseCache licenseCache;

	@BeforeEach
	public void setUp() {
		licenseCache = new NetlicensingLicenseCache();
	}

	@AfterEach
	public void tearDown() {
		licenseCache.getEntries()
			.clear();
	}

	@Test
	void get_withNewKey_returnsNull() {
		assertNull(licenseCache.get("key"));
	}

	@Test
	void get_withExpiredEntry_returnsNull() {
		NetlicensingValidationResult result = NetlicensingValidationResultFactory.create(ZonedDateTime.now()
			.minusDays(5));
		String key = "key";
		licenseCache.getEntries()
			.put(key, result);

		assertNull(licenseCache.get(key));
	}

	@Test
	void get_withExistingEntry_returnsEntry() {
		String key = "key";
		NetlicensingValidationResult expected = NetlicensingValidationResultFactory.create(ZonedDateTime.now()
			.plusDays(5));
		licenseCache.getEntries()
			.put(key, expected);

		LicenseValidationResult result = licenseCache.get(key);

		assertEquals(expected, result);
	}

	@Test
	void update_withExistingEntry_replacesOldEntry() {
		String key = "key";
		licenseCache.getEntries().put(key, NetlicensingValidationResultFactory.create());

		NetlicensingValidationResult newResult = NetlicensingValidationResultFactory.create();
		
		licenseCache.updateCache(key, newResult);

		LicenseValidationResult result = licenseCache.getEntries()
			.get(key);
		assertEquals(newResult, result);
	}

	@Test
	void update_withEmptyCache_addsNewEntry() {
		NetlicensingValidationResult expected = NetlicensingValidationResultFactory.create();

		licenseCache.updateCache("key", expected);

		assertEquals(1, licenseCache.getEntries()
			.size());

	}

}
