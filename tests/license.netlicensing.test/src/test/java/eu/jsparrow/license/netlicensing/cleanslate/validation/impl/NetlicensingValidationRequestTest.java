package eu.jsparrow.license.netlicensing.cleanslate.validation.impl;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.labs64.netlicensing.domain.vo.ValidationParameters;
import com.labs64.netlicensing.domain.vo.ValidationResult;
import com.labs64.netlicensing.exception.RestException;

import static org.mockito.Matchers.*;

import eu.jsparrow.license.netlicensing.LicenseStatus;
import eu.jsparrow.license.netlicensing.cleanslate.LicenseValidationResult;
import eu.jsparrow.license.netlicensing.cleanslate.validation.ValidationStatus;

@RunWith(MockitoJUnitRunner.class)
public class NetlicensingValidationRequestTest {

	@Mock
	ResponseEvaluator responseEvaluator;

	@Mock
	LicenseeServiceWrapper licenseeService;

	private NetlicensingValidationRequest validationRequest;

	@Before
	public void setUp() {
		validationRequest = new NetlicensingValidationRequest(responseEvaluator, licenseeService);
	}

	@Test
	public void validate_withRequestToNetlicensing_returnsValidationResult() throws Exception {
		ValidationParameters validationParameters = new ValidationParameters();
		ValidationResult validationResult = new ValidationResult();
		LicenseValidationResult expected = new LicenseValidationResult(null, null);
		when(licenseeService.validate(any(), anyString(), eq(validationParameters))).thenReturn(validationResult);
		when(responseEvaluator.evaluateResult(validationResult)).thenReturn(expected);

		LicenseValidationResult result = validationRequest.send("key", validationParameters);

		assertEquals(expected, result);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void validate_withNetlicensingException_returnsInvalidValidationResult() throws Exception {
		when(licenseeService.validate(any(), anyString(), any(ValidationParameters.class)))
			.thenThrow(RestException.class);

		LicenseValidationResult result = validationRequest.send("key", null);

		ValidationStatus status = result.getStatus();
		assertEquals(false, status.isValid());
	}

}
