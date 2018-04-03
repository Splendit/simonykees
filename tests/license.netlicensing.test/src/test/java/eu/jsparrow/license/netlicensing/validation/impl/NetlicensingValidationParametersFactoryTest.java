package eu.jsparrow.license.netlicensing.validation.impl;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

import java.time.ZonedDateTime;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.labs64.netlicensing.domain.vo.ValidationParameters;

import eu.jsparrow.license.netlicensing.model.NetlicensingLicenseModel;
import eu.jsparrow.license.netlicensing.model.NetlicensingLicenseType;
import eu.jsparrow.license.netlicensing.validation.impl.NetlicensingProperties;
import eu.jsparrow.license.netlicensing.validation.impl.NetlicensingValidationParametersFactory;

@SuppressWarnings("nls")
public class NetlicensingValidationParametersFactoryTest {

	private NetlicensingValidationParametersFactory parametersFactory;
	private ZonedDateTime expireDate;

	@Before
	public void setUp() {
		parametersFactory = new NetlicensingValidationParametersFactory();
		expireDate = ZonedDateTime.now()
			.plusDays(5);
	}

	@Test
	public void createFloatingCheckInParameters() {
		String expectedSessionId = "secret";
		String expectedAction = "checkIn";
		NetlicensingLicenseModel model = new NetlicensingLicenseModel(NetlicensingLicenseType.FLOATING, "key", "name", "product",
				expectedSessionId, expireDate, null);

		ValidationParameters parameters = parametersFactory.createFloatingCheckingParameters(model);

		Map<String, String> map = parameters.getProductModuleValidationParameters(NetlicensingProperties.FLOATING_PRODUCT_MODULE_NUMBER);
		assertTrue(map.containsKey("sessionId"));
		assertTrue(map.containsKey("action"));
		assertTrue(expectedSessionId.equals(map.get("sessionId")));
		assertTrue(expectedAction.equals(map.get("action")));
	}

	@Test
	public void createValidationParameters_nodeLockedLicense() {

		String expectedSecret = "secret";
		String expectedProduct = "product";
		String expectedName = "name";
		NetlicensingLicenseModel model = new NetlicensingLicenseModel(NetlicensingLicenseType.NODE_LOCKED, "key", expectedName,
				expectedProduct, expectedSecret, expireDate, null);

		ValidationParameters parameters = parametersFactory.createValidationParameters(model);

		assertEquals(expectedSecret, parameters.getLicenseeSecret());
		assertEquals(expectedProduct, parameters.getProductNumber());
		assertEquals(expectedName, parameters.getLicenseeName());
	}

	@Test
	public void createValidationParameters_floatingLicense() {
		String expectedSessionId = "secret";
		String expectedAction = "checkOut";

		NetlicensingLicenseModel model = new NetlicensingLicenseModel(NetlicensingLicenseType.FLOATING, "key", "name", "product",
				expectedSessionId, expireDate, null);

		ValidationParameters parameters = parametersFactory.createValidationParameters(model);

		Map<String, String> map = parameters.getProductModuleValidationParameters(NetlicensingProperties.FLOATING_PRODUCT_MODULE_NUMBER);
		assertTrue(map.containsKey("sessionId"));
		assertTrue(map.containsKey("action"));
		assertTrue(expectedSessionId.equals(map.get("sessionId")));
		assertTrue(expectedAction.equals(map.get("action")));
	}

}
