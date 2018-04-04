package eu.jsparrow.license.netlicensing.validation.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.reset;

import java.time.ZonedDateTime;

import org.junit.*;

import eu.jsparrow.license.api.LicenseValidationResult;
import eu.jsparrow.license.netlicensing.model.NetlicensingLicenseModel;
import eu.jsparrow.license.netlicensing.testhelper.NetlicensingValidationResultFactory;

@SuppressWarnings("nls")
public class NetlicensingLicenseCacheTest {

	private NetlicensingLicenseCache licenseCache;

	@Before
	public void setUp() {
		licenseCache = new NetlicensingLicenseCache();
	}

	@After
	public void tearDown() {
		licenseCache.getEntries()
			.clear();
	}

	@Test
	public void get_withNewKey_returnsNull() {
		assertNull(licenseCache.get("key"));
	}

	@Test
	public void get_withExpiredEntry_returnsNull() {
		NetlicensingValidationResult result = NetlicensingValidationResultFactory.create(ZonedDateTime.now()
			.minusDays(5));
		String key = "key";
		licenseCache.getEntries()
			.put(key, result);

		assertNull(licenseCache.get(key));
	}

	@Test
	public void get_withExistingEntry_returnsEntry() {
		String key = "key";
		NetlicensingValidationResult expected = NetlicensingValidationResultFactory.create(ZonedDateTime.now()
			.plusDays(5));
		licenseCache.getEntries()
			.put(key, expected);

		LicenseValidationResult result = licenseCache.get(key);

		assertEquals(expected, result);
	}

	@Test
	public void update_withExistingEntry_replacesOldEntry() {
		String key = "key";
		licenseCache.getEntries().put(key, NetlicensingValidationResultFactory.create());

		NetlicensingValidationResult newResult = NetlicensingValidationResultFactory.create();
		
		licenseCache.updateCache(key, newResult);

		LicenseValidationResult result = licenseCache.getEntries()
			.get(key);
		assertEquals(newResult, result);
	}

	@Test
	public void update_withEmptyCache_addsNewEntry() {
		NetlicensingValidationResult expected = NetlicensingValidationResultFactory.create();

		licenseCache.updateCache("key", expected);

		assertEquals(1, licenseCache.getEntries()
			.size());

	}

}
