package eu.jsparrow.license.netlicensing.cleanslate.validation.impl;

import static org.mockito.Mockito.*;

import java.time.ZonedDateTime;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.labs64.netlicensing.domain.vo.Context;
import com.labs64.netlicensing.domain.vo.ValidationParameters;
import com.labs64.netlicensing.domain.vo.ValidationResult;
import com.labs64.netlicensing.exception.NetLicensingException;

import eu.jsparrow.license.netlicensing.cleanslate.LicenseValidationResult;

import eu.jsparrow.license.netlicensing.cleanslate.model.LicenseType;
import eu.jsparrow.license.netlicensing.cleanslate.model.NetlicensingLicenseModel;
import eu.jsparrow.license.netlicensing.cleanslate.validation.ValidationStatus;

public class NetlicensingLicenseValidationTest {

	private NetlicensingLicenseValidation validation;
	private NetlicensingLicenseModel model;
	private ValidationResult netlicensingResponse;
	private LicenseValidationResult validationResult;

	@Mock
	NetlicensingLicenseCache cache;

	@Mock
	ResponseEvaluator evaluator;

	@Before
	public void setUp() {
		model = new NetlicensingLicenseModel(LicenseType.NODE_LOCKED, "key", "name", "product", "secret",
				ZonedDateTime.now());
		validationResult = new LicenseValidationResult(model, new ValidationStatus(true));
		netlicensingResponse = new ValidationResult();
		cache = mock(NetlicensingLicenseCache.class);
		evaluator = mock(ResponseEvaluator.class);
		validation = new TestableNetlicensingLicenseValidation(model);
	}

	@Test
	public void validate_invalidCache() {

		when(cache.isInvalid()).thenReturn(true);
		when(evaluator.evaluateResult(netlicensingResponse)).thenReturn(validationResult);
		validation.validate();
		verify(cache).updateCache(validationResult);
	}

	@Test
	public void validate_validCache() {

		ValidationResult netlicensingResponse = new ValidationResult();
		when(cache.isInvalid()).thenReturn(false);

		validation.validate();

		verify(evaluator, never()).evaluateResult(netlicensingResponse);
		verify(cache).getLastResult();
	}

	class TestableNetlicensingLicenseValidation extends NetlicensingLicenseValidation {
		public TestableNetlicensingLicenseValidation(NetlicensingLicenseModel model) {
			super(model);
		}

		@Override
		protected NetlicensingLicenseCache getLicenseCacheInstance() {
			return cache;
		}

		@Override
		protected ResponseEvaluator createResponseEvaluator(NetlicensingLicenseModel model) {
			return evaluator;
		}

		@Override
		protected ValidationResult sendValidationRequest(String licenseeNumber,
				ValidationParameters validationParameters, Context context) throws NetLicensingException {
			return netlicensingResponse;
		}

	}
}
