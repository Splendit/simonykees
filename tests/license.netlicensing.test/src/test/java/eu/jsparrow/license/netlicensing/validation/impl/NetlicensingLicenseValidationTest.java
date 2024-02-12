package eu.jsparrow.license.netlicensing.validation.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.labs64.netlicensing.domain.vo.ValidationParameters;

import eu.jsparrow.license.api.LicenseType;
import eu.jsparrow.license.api.LicenseValidationResult;
import eu.jsparrow.license.api.exception.ValidationException;
import eu.jsparrow.license.netlicensing.model.NetlicensingLicenseModel;
import eu.jsparrow.license.netlicensing.testhelper.NetlicensingLicenseModelFactory;

public class NetlicensingLicenseValidationTest {

	NetlicensingLicenseCache cache;

	NetlicensingValidationParametersFactory parametersFactory;

	NetlicensingValidationRequest request;

	private NetlicensingLicenseModel model;

	private NetlicensingLicenseValidation netlicensingValidation;

	@BeforeEach
	public void setUp() {
		cache = mock(NetlicensingLicenseCache.class);
		parametersFactory = mock(NetlicensingValidationParametersFactory.class);
		request = mock(NetlicensingValidationRequest.class);
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
				LicenseType.NODE_LOCKED, "newKey", false, null, null); //$NON-NLS-1$
		ValidationParameters validationParameters = new ValidationParameters();
		NetlicensingValidationResult finalValidationResult = new NetlicensingValidationResult(LicenseType.NODE_LOCKED,
				"newKey", false, null, null); //$NON-NLS-1$

		model = NetlicensingLicenseModelFactory.create(LicenseType.NONE);
		netlicensingValidation = new NetlicensingLicenseValidation(model, cache, parametersFactory, request);

		when(cache.get(any())).thenReturn(null);
		when(parametersFactory.createVerifyParameters(any())).thenReturn(validationParameters);
		when(parametersFactory.createValidationParameters(any())).thenReturn(validationParameters);

		when(request.send(any(), any())).thenReturn(intermediateValidationResult).thenReturn(finalValidationResult);

		netlicensingValidation.validate();

		ArgumentCaptor<NetlicensingLicenseModel> argument = ArgumentCaptor.forClass(NetlicensingLicenseModel.class);
		verify(parametersFactory).createValidationParameters(argument.capture());
		assertEquals(LicenseType.NODE_LOCKED, argument.getValue().getType());
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
