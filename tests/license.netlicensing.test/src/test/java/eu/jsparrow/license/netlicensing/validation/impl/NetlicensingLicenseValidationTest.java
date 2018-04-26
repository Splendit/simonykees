package eu.jsparrow.license.netlicensing.validation.impl;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.labs64.netlicensing.domain.vo.ValidationParameters;

import eu.jsparrow.license.api.LicenseType;
import eu.jsparrow.license.api.LicenseValidationResult;
import eu.jsparrow.license.api.exception.ValidationException;
import eu.jsparrow.license.netlicensing.model.NetlicensingLicenseModel;
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

	@Before
	public void setUp() {
		model = NetlicensingLicenseModelFactory.create();
		netlicensingValidation = new NetlicensingLicenseValidation(model, cache, parametersFactory, request);
	}

	@Test
	public void validate_withInvalidCache_shouldSendRequestAndSaveToCache() throws ValidationException {
		NetlicensingValidationResult validationResult = new NetlicensingValidationResult(model.getType(), null, true,
				null, null);
		ValidationParameters validationParameters = new ValidationParameters();

		when(cache.get(any())).thenReturn(null);
		when(parametersFactory.createValidationParameters(eq(model))).thenReturn(validationParameters);
		when(request.send(eq(model.getKey()), eq(validationParameters))).thenReturn(validationResult);

		LicenseValidationResult result = netlicensingValidation.validate();

		verify(cache).updateCache(eq(model.getKey()), eq(validationResult));
		assertEquals(validationResult, result);
	}

	@Test
	public void validate_withUnknownLicenseType_shouldSendRequestToGetLicenseType() throws ValidationException {
		NetlicensingValidationResult intermediateValidationResult = new NetlicensingValidationResult(
				LicenseType.NODE_LOCKED, "newKey", false, null, null);
		ValidationParameters validationParameters = new ValidationParameters();
		NetlicensingValidationResult finalValidationResult = new NetlicensingValidationResult(LicenseType.NODE_LOCKED,
				"newKey", false, null, null);

		model = NetlicensingLicenseModelFactory.create(LicenseType.NONE);
		netlicensingValidation = new NetlicensingLicenseValidation(model, cache, parametersFactory, request);

		when(cache.get(any())).thenReturn(null);
		when(parametersFactory.createVerifyParameters(any())).thenReturn(validationParameters);
		when(parametersFactory.createValidationParameters(any())).thenReturn(validationParameters);

		when(request.send(any(), any())).thenReturn(intermediateValidationResult)
			.thenReturn(finalValidationResult);

		netlicensingValidation.validate();

		ArgumentCaptor<NetlicensingLicenseModel> argument = ArgumentCaptor.forClass(NetlicensingLicenseModel.class);
		verify(parametersFactory).createValidationParameters(argument.capture());
		assertEquals(LicenseType.NODE_LOCKED, argument.getValue()
			.getType());
	}

	@Test
	public void validate_withValidCache_shouldGetLastResultFromCache() throws ValidationException {
		LicenseValidationResult expected = new LicenseValidationResult(null, null, false, null, null);

		when(cache.get(eq(model.getKey()))).thenReturn(expected);

		LicenseValidationResult result = netlicensingValidation.validate();

		assertEquals(expected, result);
	}

	@Test
	public void checkIn_withFloatingLicensetype_shouldSendRequest() throws ValidationException {
		model = NetlicensingLicenseModelFactory.create(LicenseType.FLOATING);
		netlicensingValidation = new NetlicensingLicenseValidation(model, cache, parametersFactory, request);

		netlicensingValidation.checkIn();

		verify(request).send(eq(model.getKey()), any());
	}

}
