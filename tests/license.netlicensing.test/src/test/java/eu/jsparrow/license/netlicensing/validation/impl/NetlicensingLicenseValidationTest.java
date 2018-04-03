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

import eu.jsparrow.license.netlicensing.LicenseValidationResult;
import eu.jsparrow.license.netlicensing.exception.ValidationException;
import eu.jsparrow.license.netlicensing.model.NetlicensingLicenseModel;
import eu.jsparrow.license.netlicensing.model.NetlicensingLicenseType;
import eu.jsparrow.license.netlicensing.validation.ValidationStatus;
import eu.jsparrow.license.netlicensing.validation.impl.*;

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
		model = new NetlicensingLicenseModel(NetlicensingLicenseType.NODE_LOCKED, "key", "name", "product", "secret",
				ZonedDateTime.now(), null);
		netlicensingValidation = new NetlicensingLicenseValidation(model, cache, parametersFactory, request);
	}

	@Test
	public void validate_withInvalidCache_shouldSendRequestAndSaveToCache() throws ValidationException {
		LicenseValidationResult validationResult = new LicenseValidationResult(model, new ValidationStatus(true));
		ValidationParameters valiationParameters = new ValidationParameters();

		when(cache.getValidationResultFor(any())).thenReturn(null);
		when(parametersFactory.createValidationParameters(eq(model))).thenReturn(valiationParameters);
		when(request.send(eq(model.getKey()), eq(valiationParameters))).thenReturn(validationResult);

		LicenseValidationResult result = netlicensingValidation.validate();

		verify(cache).updateCache(validationResult);
		assertEquals(validationResult, result);
	}

	@Test
	public void validate_withValidCache_shouldGetLastResultFromCache() throws ValidationException {
		LicenseValidationResult expected = new LicenseValidationResult(null, null);
		when(cache.getValidationResultFor(eq(model))).thenReturn(expected);

		LicenseValidationResult result = netlicensingValidation.validate();

		assertEquals(expected, result);
	}
	
	@Test(expected = ValidationException.class)
	public void checkIn_withBadLicenseType_shouldThrowException() throws ValidationException {
		model = new NetlicensingLicenseModel(NetlicensingLicenseType.NODE_LOCKED, "key", "name", "product", "secret",
				ZonedDateTime.now(), null);
	
		netlicensingValidation.checkIn();

	}
	
	@Test
	public void checkIn_withFloatingLicensetype_shouldSendRequestAndSaveToCache() throws ValidationException {
		model = new NetlicensingLicenseModel(NetlicensingLicenseType.FLOATING, "key", "name", "product", "secret",
				ZonedDateTime.now(), null);
		netlicensingValidation = new NetlicensingLicenseValidation(model, cache, parametersFactory, request);
		
		netlicensingValidation.checkIn();
		
		verify(request).send(eq(model.getKey()), any());
		verify(cache).updateCache(null);
	}

}
