package eu.jsparrow.license.netlicensing.cleanslate.validation.impl;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.ZonedDateTime;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.labs64.netlicensing.domain.vo.ValidationParameters;

import eu.jsparrow.license.netlicensing.cleanslate.LicenseValidationResult;
import eu.jsparrow.license.netlicensing.cleanslate.model.NetlicensingLicenseModel;
import eu.jsparrow.license.netlicensing.cleanslate.model.NetlicensingLicenseType;
import eu.jsparrow.license.netlicensing.cleanslate.testhelper.DummyLicenseModel;
import eu.jsparrow.license.netlicensing.cleanslate.validation.ValidationStatus;

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
	public void validate_withInvalidCache_shouldSendRequestAndSaveToCache() {
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
	public void validate_withValidCache_shouldGetLastResultFromCache() {
		LicenseValidationResult expected = new LicenseValidationResult(null, null);
		when(cache.getValidationResultFor(eq(model))).thenReturn(expected);

		LicenseValidationResult result = netlicensingValidation.validate();

		assertEquals(expected, result);
	}

}
