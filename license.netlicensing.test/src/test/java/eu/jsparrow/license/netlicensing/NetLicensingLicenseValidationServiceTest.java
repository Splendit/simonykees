package eu.jsparrow.license.netlicensing;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

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

	@Test
	public void startValidation_IsCalled_InitializeLicenseManager() {
		netLicensingLicenseValidationService.startValidation();

		verify(licenseManager, times(1)).initManager();
	}

	@Test
	public void stopValidation_IsCalled_CheckInIsCalled() {
		netLicensingLicenseValidationService.stopValidation();

		verify(licenseManager, times(1)).checkIn();
	}

	@Test
	public void isValid_IsCalled_GetValidationDataAndIsValidIsCalled() {
		LicenseChecker licenseCheckerMock = mock(LicenseChecker.class);
		when(licenseManager.getValidationData()).thenReturn(licenseCheckerMock);
		when(licenseCheckerMock.isValid()).thenReturn(true);
		
		Boolean result = netLicensingLicenseValidationService.isValid();

		verify(licenseCheckerMock, times(1)).isValid();
		assertTrue(result);
	}
}
