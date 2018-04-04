package eu.jsparrow.license.netlicensing.validation.impl;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.ZonedDateTime;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.labs64.netlicensing.domain.vo.ValidationParameters;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;

import eu.jsparrow.license.api.LicenseValidationResult;
import eu.jsparrow.license.api.exception.ValidationException;
import eu.jsparrow.license.netlicensing.model.NetlicensingLicenseModel;
import eu.jsparrow.license.netlicensing.model.NetlicensingLicenseType;
import eu.jsparrow.license.netlicensing.testhelper.NetlicensingLicenseModelFactory;

@RunWith(MockitoJUnitRunner.class)
public class NetlicensingLicenseValidationTest {

	@Mock
	NetlicensingLicenseCache cache;

	@Mock
	NetlicensingValidationParametersFactory parametersFactory;

	@Mock
	NetlicensingValidationRequest request;

	private NetlicensingLicenseModel model;

	private NetlicensingLicenseValidation netlicensingValidation;

	@SuppressWarnings("nls")
	@Before
	public void setUp() {
		model = NetlicensingLicenseModelFactory.create();
		netlicensingValidation = new NetlicensingLicenseValidation(model, cache, parametersFactory, request);
	}

	@Test
	public void validate_withInvalidCache_shouldSendRequestAndSaveToCache() throws ValidationException {
		NetlicensingValidationResult validationResult = new NetlicensingValidationResult(model, true, null);
		ValidationParameters validationParameters = new ValidationParameters();

		when(cache.get(any())).thenReturn(null);
		when(parametersFactory.createValidationParameters(eq(model))).thenReturn(validationParameters);
		when(request.send(eq(model.getKey()), eq(validationParameters))).thenReturn(validationResult);

		LicenseValidationResult result = netlicensingValidation.validate();

		verify(cache).updateCache(eq(model.getKey()), eq(validationResult));
		assertEquals(validationResult, result);
	}

	@Test
	public void validate_withValidCache_shouldGetLastResultFromCache() throws ValidationException {
		LicenseValidationResult expected = new LicenseValidationResult();
		when(cache.get(eq(model.getKey()))).thenReturn(expected);

		LicenseValidationResult result = netlicensingValidation.validate();

		assertEquals(expected, result);
	}

	@Test(expected = ValidationException.class)
	public void checkIn_withBadLicenseType_shouldThrowException() throws ValidationException {
		model = NetlicensingLicenseModelFactory.create(NetlicensingLicenseType.NODE_LOCKED);

		netlicensingValidation.checkIn();

	}

	@Test
	public void checkIn_withFloatingLicensetype_shouldSendRequest() throws ValidationException {
		model = NetlicensingLicenseModelFactory.create(NetlicensingLicenseType.FLOATING);
		netlicensingValidation = new NetlicensingLicenseValidation(model, cache, parametersFactory, request);

		netlicensingValidation.checkIn();

		verify(request).send(eq(model.getKey()), any());
	}

}
