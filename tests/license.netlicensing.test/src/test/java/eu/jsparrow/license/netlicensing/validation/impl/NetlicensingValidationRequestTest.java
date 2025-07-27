package eu.jsparrow.license.netlicensing.validation.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.labs64.netlicensing.domain.vo.ValidationParameters;
import com.labs64.netlicensing.domain.vo.ValidationResult;
import com.labs64.netlicensing.exception.RestException;

import eu.jsparrow.license.api.LicenseValidationResult;
import eu.jsparrow.license.api.exception.ValidationException;
import eu.jsparrow.license.netlicensing.testhelper.NetlicensingValidationResultFactory;

public class NetlicensingValidationRequestTest {

	ResponseEvaluator responseEvaluator;

	LicenseeServiceWrapper licenseeService;

	private NetlicensingValidationRequest validationRequest;

	@BeforeEach
	public void setUp() {
		responseEvaluator = mock(ResponseEvaluator.class);
		licenseeService = mock(LicenseeServiceWrapper.class);
		validationRequest = new NetlicensingValidationRequest(responseEvaluator, licenseeService);
	}

	@Test
	public void validate_withRequestToNetlicensing_returnsValidationResult() throws Exception {
		ValidationParameters validationParameters = new ValidationParameters();
		ValidationResult validationResult = new ValidationResult();
		NetlicensingValidationResult expected = NetlicensingValidationResultFactory.create();
		when(licenseeService.validate(any(), anyString(), eq(validationParameters))).thenReturn(validationResult);
		when(responseEvaluator.evaluateResult(validationResult)).thenReturn(expected);

		LicenseValidationResult result = validationRequest.send("key", validationParameters);

		assertEquals(expected, result);
	}

	@Test
	public void validate_withNetlicensingException_returnsInvalidValidationResult() throws Exception {
		assertThrows(ValidationException.class, () -> {

			when(licenseeService.validate(any(), anyString(), nullable(ValidationParameters.class)))
					.thenThrow(RestException.class);

			validationRequest.send("key", null);
		});
	}

}
