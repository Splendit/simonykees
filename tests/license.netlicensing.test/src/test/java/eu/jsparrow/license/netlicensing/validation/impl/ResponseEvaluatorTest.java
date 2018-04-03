package eu.jsparrow.license.netlicensing.validation.impl;

import static org.junit.Assert.*;

import java.time.ZonedDateTime;

import org.junit.Before;
import org.junit.Test;

import com.labs64.netlicensing.domain.vo.ValidationResult;

import eu.jsparrow.license.api.LicenseValidationResult;
import eu.jsparrow.license.api.exception.ValidationException;
import eu.jsparrow.license.netlicensing.model.*;
import eu.jsparrow.license.netlicensing.testhelper.DummyResponseGenerator;

@SuppressWarnings("nls")
public class ResponseEvaluatorTest {

	private ResponseEvaluator responseEvaluator;
	private DummyResponseGenerator responseGenerator;

	@Before
	public void setUp() {
		responseGenerator = new DummyResponseGenerator();
		NetlicensingLicenseModel model = new NetlicensingLicenseModel(NetlicensingLicenseType.NODE_LOCKED, "key",
				"name", "product", "secret", ZonedDateTime.now(), null);
		responseEvaluator = new ResponseEvaluator(model);
	}

	@Test
	public void evaluateResult_validFloating() throws ValidationException {
		ZonedDateTime now = ZonedDateTime.now();
		ZonedDateTime expireDate = ZonedDateTime.now()
			.plusDays(1);
		ValidationResult response = responseGenerator.createFloatingResponse(now.toString(), "true",
				expireDate.toString());

		LicenseValidationResult result = responseEvaluator.evaluateResult(response);

		assertTrue(result.isValid());
		assertEquals(StatusDetail.FLOATING.getUserMessage(), result.getDetail());
	}

	@Test
	public void evaluateResult_validFloating_outOfSessions() throws ValidationException {
		ZonedDateTime now = ZonedDateTime.now();
		ZonedDateTime expireDate = ZonedDateTime.now()
			.plusDays(1);
		ValidationResult response = responseGenerator.createFloatingResponse("false", now.toString(), "true",
				expireDate.toString());

		LicenseValidationResult result = responseEvaluator.evaluateResult(response);

		assertFalse(result.isValid());
		assertEquals(StatusDetail.FLOATING_OUT_OF_SESSIONS.getUserMessage(), result.getDetail());
	}

	@Test
	public void evaluateResult_expiredFloating() throws ValidationException {
		ZonedDateTime now = ZonedDateTime.now();
		ZonedDateTime expireDate = ZonedDateTime.now()
			.minusDays(1);

		ValidationResult response = responseGenerator.createFloatingResponse(now.toString(), "false",
				expireDate.toString());

		LicenseValidationResult result = responseEvaluator.evaluateResult(response);

		assertFalse(result.isValid());
		assertEquals(StatusDetail.FLOATING_EXPIRED.getUserMessage(), result.getDetail());
	}

	@Test
	public void evaluateResult_validNodeLocked() throws ValidationException {
		ZonedDateTime expireDate = ZonedDateTime.now()
			.plusDays(1);
		ValidationResult response = responseGenerator.createNodeLockedResponse("featureKey", "true",
				expireDate.toString());

		LicenseValidationResult result = responseEvaluator.evaluateResult(response);

		assertTrue(result.isValid());
		assertEquals(StatusDetail.NODE_LOCKED.getUserMessage(), result.getDetail());
	}

	@Test
	public void evaluateResult_expiredNodeLocked() throws ValidationException {
		ZonedDateTime expireDate = ZonedDateTime.now()
			.minusDays(1);
		ValidationResult response = responseGenerator.createNodeLockedResponse("featureKey", "false",
				expireDate.toString());

		LicenseValidationResult result = responseEvaluator.evaluateResult(response);

		assertFalse(result.isValid());
		assertEquals(StatusDetail.NODE_LOCKED_EXPIRED.getUserMessage(), result.getDetail());
	}

	@Test
	public void evaluateResult_hardwareIdMismatch() throws ValidationException {
		ZonedDateTime now = ZonedDateTime.now();
		ZonedDateTime expireDate = ZonedDateTime.now()
			.plusDays(1);
		ValidationResult response = responseGenerator.createNodeLockedResponse("false", now.toString(), "false",
				expireDate.toString());

		LicenseValidationResult result = responseEvaluator.evaluateResult(response);

		assertFalse(result.isValid());
		assertEquals(StatusDetail.NODE_LOCKED_HARDWARE_MISMATCH.getUserMessage(), result.getDetail());
	}

	@Test(expected = ValidationException.class)
	public void evaluateResult_undefined() throws ValidationException {
		ZonedDateTime now = ZonedDateTime.now();
		ZonedDateTime expireDate = ZonedDateTime.now()
			.minusDays(1);
		ValidationResult response = responseGenerator.createNodeLockedResponse("false", now.toString(), "false",
				expireDate.toString());

		responseEvaluator.evaluateResult(response);
	}

	@Test(expected = ValidationException.class)
	public void evaluateResult_undefined_incompleteResponse() throws ValidationException {
		ValidationResult response = new ValidationResult();

		responseEvaluator.evaluateResult(response);

	}
}
