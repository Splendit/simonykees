package eu.jsparrow.license.netlicensing.cleanslate.validation.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.time.ZonedDateTime;

import org.junit.Before;
import org.junit.Test;

import com.labs64.netlicensing.domain.vo.ValidationResult;

import eu.jsparrow.license.netlicensing.cleanslate.LicenseValidationResult;
import eu.jsparrow.license.netlicensing.cleanslate.model.StatusDetail;
import eu.jsparrow.license.netlicensing.cleanslate.model.NetlicensingLicenseType;
import eu.jsparrow.license.netlicensing.cleanslate.testhelper.DummyResponseGenerator;
import eu.jsparrow.license.netlicensing.cleanslate.model.NetlicensingLicenseModel;
import eu.jsparrow.license.netlicensing.cleanslate.validation.ValidationStatus;

@SuppressWarnings("nls")
public class ResponseEvaluatorTest {

	private ResponseEvaluator responseEvaluator;
	private DummyResponseGenerator responseGenerator;

	@Before
	public void setUp() {
		responseGenerator = new DummyResponseGenerator();
		NetlicensingLicenseModel model = new NetlicensingLicenseModel(NetlicensingLicenseType.NODE_LOCKED, "key", "name", "product", "secret", ZonedDateTime.now());
		responseEvaluator = new ResponseEvaluator(model);
	}

	@Test
	public void evaluateResult_validFloating() {
		ZonedDateTime now = ZonedDateTime.now();
		ZonedDateTime expireDate = ZonedDateTime.now().plusDays(1);
		ValidationResult response = responseGenerator.createFloatingResponse(now.toString(), "true", expireDate.toString());

		LicenseValidationResult result = responseEvaluator.evaluateResult(response);

		ValidationStatus status = result.getStatus();
		assertTrue(status.isValid());
		assertEquals(StatusDetail.FLOATING, status.getStatusDetail());
	}
	
	@Test
	public void evaluateResult_validFloating_outOfSessions() {
		ZonedDateTime now = ZonedDateTime.now();
		ZonedDateTime expireDate = ZonedDateTime.now().plusDays(1);
		ValidationResult response = responseGenerator.createFloatingResponse("false", now.toString(), "true", expireDate.toString());

		LicenseValidationResult result = responseEvaluator.evaluateResult(response);

		ValidationStatus status = result.getStatus();
		assertFalse(status.isValid());
		assertEquals(StatusDetail.FLOATING_OUT_OF_SESSIONS, status.getStatusDetail());
	}

	@Test
	public void evaluateResult_expiredFloating() {
		ZonedDateTime now = ZonedDateTime.now();
		ZonedDateTime expireDate = ZonedDateTime.now().minusDays(1);

		ValidationResult response = responseGenerator.createFloatingResponse(now.toString(), "false", expireDate.toString());

		LicenseValidationResult result = responseEvaluator.evaluateResult(response);

		ValidationStatus status = result.getStatus();
		assertFalse(status.isValid());
		assertEquals(StatusDetail.FLOATING_EXPIRED, status.getStatusDetail());
	}

	@Test
	public void evaluateResult_validNodeLocked() {
		ZonedDateTime expireDate = ZonedDateTime.now().plusDays(1);
		ValidationResult response = responseGenerator.createNodeLockedResponse("featureKey", "true", expireDate.toString());

		LicenseValidationResult result = responseEvaluator.evaluateResult(response);

		ValidationStatus status = result.getStatus();
		assertTrue(status.isValid());
		assertEquals(StatusDetail.NODE_LOCKED, status.getStatusDetail());
	}

	@Test
	public void evaluateResult_expiredNodeLocked() {
		ZonedDateTime expireDate = ZonedDateTime.now().minusDays(1);
		ValidationResult response = responseGenerator.createNodeLockedResponse("featureKey", "false", expireDate.toString());

		LicenseValidationResult result = responseEvaluator.evaluateResult(response);

		ValidationStatus status = result.getStatus();
		assertFalse(status.isValid());
		assertEquals(StatusDetail.NODE_LOCKED_EXPIRED, status.getStatusDetail());
	}
	
	@Test
	public void evaluateResult_undefinedLicense() {
		ZonedDateTime now = ZonedDateTime.now();
		ZonedDateTime expireDate = ZonedDateTime.now().minusDays(1);
		ValidationResult response = responseGenerator.createFloatingResponse("false", now.toString(), "false", expireDate.toString());

		LicenseValidationResult result = responseEvaluator.evaluateResult(response);

		ValidationStatus status = result.getStatus();
		assertFalse(status.isValid());
		assertEquals(StatusDetail.UNDEFINED, status.getStatusDetail());
	}

}
