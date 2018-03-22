package eu.jsparrow.license.netlicensing;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.isEmptyString;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@SuppressWarnings("nls")
@RunWith(MockitoJUnitRunner.class)
public class NetLicensingLicenseValidationServiceTest {

	@Mock(answer = Answers.RETURNS_DEEP_STUBS)
	LicenseManager licenseManager;

	private NetLicensingLicenseValidationService netLicensingLicenseValidationService;

	@Before
	public void setUp() {
		netLicensingLicenseValidationService = new NetLicensingLicenseValidationService();
		netLicensingLicenseValidationService.setLicenseManager(licenseManager);
	}

	@Ignore
	@Test
	public void startValidation_isCalled_initializeLicenseManager() {
		netLicensingLicenseValidationService.startValidation();

		verify(licenseManager, times(1)).initManager();
	}

	@Test
	public void stopValidation_isCalled_checkInIsCalled() {
		netLicensingLicenseValidationService.stopValidation();

		verify(licenseManager, times(1)).checkIn();
	}

	@Test
	public void isValid_withValidLicense_returnsTrue() {
		when(licenseManager.getValidationData()
			.isValid()).thenReturn(true);

		Boolean result = netLicensingLicenseValidationService.isValid();

		assertTrue(result);
	}

	@Test
	public void isExpired_licenseFreeExpired_returnsTrue() {
		when(licenseManager.getValidationData()
			.getLicenseStatus()).thenReturn(LicenseStatus.FREE_EXPIRED);

		Boolean result = netLicensingLicenseValidationService.isExpired();

		assertTrue(result);
	}

	@Test
	public void isExpired_licenseFloatingExpired_returnsTrue() {
		when(licenseManager.getValidationData()
			.getLicenseStatus()).thenReturn(LicenseStatus.FLOATING_EXPIRED);

		Boolean result = netLicensingLicenseValidationService.isExpired();

		assertTrue(result);
	}

	@Test
	public void isExpired_licenseNodeLockedExpired_returnsTrue() {
		when(licenseManager.getValidationData()
			.getLicenseStatus()).thenReturn(LicenseStatus.NODE_LOCKED_EXPIRED);

		Boolean result = netLicensingLicenseValidationService.isExpired();

		assertTrue(result);
	}

	@Test
	public void isExpired_anyOtherLicense_returnsFalse() {
		when(licenseManager.getValidationData()
			.getLicenseStatus()).thenReturn(LicenseStatus.FREE_REGISTERED);

		Boolean result = netLicensingLicenseValidationService.isExpired();

		assertFalse(result);
	}

	@Test
	public void updateLicenseNumber_withAnyParameters_updateCalled() {
		netLicensingLicenseValidationService.updateLicenseeNumber("key", "name");

		verify(licenseManager).updateLicenseeNumber("key", "name");
	}

	@Test
	public void getDisplayableLicenseInformation_noLicenseType_resultEmpty() {
		when(licenseManager.getValidationData()
			.getType()).thenReturn(null);

		String result = netLicensingLicenseValidationService.getDisplayableLicenseInformation();

		assertThat(result, isEmptyString());
	}

	@Test
	public void getDisplayableLicenseInformation_anyLicense_resultContainsKey() {
		when(licenseManager.getValidationData()
			.getType()).thenReturn(LicenseType.FLOATING);
		when(licenseManager.getValidationData()
			.getExpirationDate()).thenReturn(ZonedDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneId.of("GMT+0")));
		when(licenseManager.getLicensee()
			.getLicenseeNumber()).thenReturn("1234");

		String result = netLicensingLicenseValidationService.getDisplayableLicenseInformation();

		assertThat(result, containsString("1234"));
	}

	@Test
	public void getDisplayableLicenseInformation_withTryAndBuy_noKeyInResult() {
		when(licenseManager.getValidationData()
			.getType()).thenReturn(LicenseType.TRY_AND_BUY);
		when(licenseManager.getValidationData()
			.getExpirationDate()).thenReturn(ZonedDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneId.of("GMT+0")));
		when(licenseManager.getLicensee()
			.getLicenseeNumber()).thenReturn("1234");

		String result = netLicensingLicenseValidationService.getDisplayableLicenseInformation();

		assertThat(result, not(containsString("1234")));
	}

	@Test
	public void isFullValidLicense_isValid_returnsTrue() {
		when(licenseManager.getValidationData()
			.getType()).thenReturn(LicenseType.FLOATING);
		when(licenseManager.getValidationData()
			.isValid()).thenReturn(true);

		Boolean result = netLicensingLicenseValidationService.isFullValidLicense();

		assertTrue(result);
	}

	@Test
	public void isFullValidLicense_isTryAndBuyLicense_returnsFalse() {
		when(licenseManager.getValidationData()
			.getType()).thenReturn(LicenseType.TRY_AND_BUY);
		when(licenseManager.getValidationData()
			.isValid()).thenReturn(true);

		Boolean result = netLicensingLicenseValidationService.isFullValidLicense();

		assertFalse(result);
	}

	@Test
	public void isFullValidLicense_isInvalid_returnsFalse() {
		when(licenseManager.getValidationData()
			.getType()).thenReturn(LicenseType.FLOATING);
		when(licenseManager.getValidationData()
			.isValid()).thenReturn(false);

		Boolean result = netLicensingLicenseValidationService.isFullValidLicense();

		assertFalse(result);
	}

	@Test
	public void isDemoType_isTryAndBuyLicenseType_returnsTrue() {
		when(licenseManager.getValidationData()
			.getType()).thenReturn(LicenseType.TRY_AND_BUY);

		Boolean result = netLicensingLicenseValidationService.isDemoType();

		assertTrue(result);
	}
}
