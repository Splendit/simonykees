package eu.jsparrow.license.netlicensing.validation.impl;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.ZonedDateTime;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.labs64.netlicensing.domain.vo.ValidationParameters;

import eu.jsparrow.license.api.LicenseType;
import eu.jsparrow.license.netlicensing.model.NetlicensingLicenseModel;

@SuppressWarnings("nls")
public class NetlicensingValidationParametersFactoryTest {

	private static final String ACTION = "action";
	private static final String SECRET = "secret";
	private static final String SESSION_ID = "sessionId";
	private static final String MODULE = "module";
	private static final String PRODUCT = "product";

	private NetlicensingValidationParametersFactory parametersFactory;
	private ZonedDateTime expireDate;

	@BeforeEach
	public void setUp() {
		parametersFactory = new NetlicensingValidationParametersFactory();
		expireDate = ZonedDateTime.now()
			.plusDays(5);
	}

	@Test
	public void createFloatingCheckInParameters() {
		String expectedSessionId = SECRET;
		String expectedAction = "checkIn";
		NetlicensingLicenseModel model = new NetlicensingLicenseModel("key", expectedSessionId, PRODUCT, MODULE,
				LicenseType.FLOATING, "name", expireDate);

		ValidationParameters parameters = parametersFactory.createFloatingCheckInParameters(model);

		Map<String, String> map = parameters.getProductModuleValidationParameters(model.getModuleNr());
		assertTrue(map.containsKey(SESSION_ID));
		assertTrue(map.containsKey(ACTION));
		assertTrue(expectedSessionId.equals(map.get(SESSION_ID)));
		assertTrue(expectedAction.equals(map.get(ACTION)));
	}

	@Test
	public void createValidationParameters_nodeLockedLicense() {

		String expectedSecret = SECRET;
		String expectedName = "name";
		NetlicensingLicenseModel model = new NetlicensingLicenseModel("key", expectedSecret, PRODUCT, MODULE,
				LicenseType.NODE_LOCKED, expectedName, expireDate);

		ValidationParameters parameters = parametersFactory.createValidationParameters(model);

		assertEquals(expectedSecret, parameters.getLicenseeSecret());
		assertEquals(model.getProductNr(), parameters.getProductNumber());
		assertEquals(expectedName, parameters.getLicenseeName());
	}

	@Test
	public void createValidationParameters_floatingLicense() {
		String expectedSessionId = SECRET;
		String expectedAction = "checkOut";

		NetlicensingLicenseModel model = new NetlicensingLicenseModel("key", expectedSessionId, PRODUCT, MODULE,
				LicenseType.FLOATING, "name", expireDate);

		ValidationParameters parameters = parametersFactory.createValidationParameters(model);

		Map<String, String> map = parameters.getProductModuleValidationParameters(model.getModuleNr());
		assertTrue(map.containsKey(SESSION_ID));
		assertTrue(map.containsKey(ACTION));
		assertTrue(expectedSessionId.equals(map.get(SESSION_ID)));
		assertTrue(expectedAction.equals(map.get(ACTION)));
	}

}
