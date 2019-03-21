package eu.jsparrow.license.netlicensing.validation.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.ZonedDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.labs64.netlicensing.domain.vo.ValidationResult;

import eu.jsparrow.license.api.LicenseValidationResult;
import eu.jsparrow.license.api.exception.ValidationException;
import eu.jsparrow.license.netlicensing.model.NetlicensingLicenseModel;
import eu.jsparrow.license.netlicensing.model.StatusDetail;
import eu.jsparrow.license.netlicensing.testhelper.DummyResponseGenerator;
import eu.jsparrow.license.netlicensing.testhelper.NetlicensingLicenseModelFactory;

@SuppressWarnings("nls")
public class ResponseEvaluatorTest {

	private ResponseEvaluator responseEvaluator;
	private DummyResponseGenerator responseGenerator;

	@BeforeEach
	public void setUp() {
		responseGenerator = new DummyResponseGenerator();
		NetlicensingLicenseModel model = NetlicensingLicenseModelFactory.create();
		responseEvaluator = new ResponseEvaluator(model.getKey());
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

	@Test
	public void evaluateResult_undefined() {
		ZonedDateTime now = ZonedDateTime.now();
		ZonedDateTime expireDate = ZonedDateTime.now()
			.minusDays(1);
		ValidationResult response = responseGenerator.createNodeLockedResponse("false", now.toString(), "false",
				expireDate.toString());

		assertThrows(ValidationException.class, () -> responseEvaluator.evaluateResult(response));
	}

	@Test
	public void evaluateResult_undefined_incompleteResponse() throws ValidationException {
		ValidationResult response = new ValidationResult();

		assertThrows(ValidationException.class, () -> responseEvaluator.evaluateResult(response));
	}
}
