package eu.jsparrow.license.netlicensing;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Test cases for converting enumerations from/to string.
 * 
 * @author Ardit Ymeri
 * @since 1.0
 *
 */
@SuppressWarnings("nls")
public class EnumsTests {
	
	@Test
	public void licenseTypeFromString() {
		assertEquals(LicenseType.FLOATING, LicenseType.fromString("floating"));
		assertEquals(LicenseType.NODE_LOCKED, LicenseType.fromString("multifeature"));
		assertEquals(LicenseType.SUBSCRIPTION, LicenseType.fromString("subscription"));
		assertEquals(LicenseType.TRY_AND_BUY, LicenseType.fromString("tryandbuy"));
		assertEquals(LicenseType.TRY_AND_BUY, LicenseType.fromString("noneOfTheAboce"));
		assertEquals(LicenseType.TRY_AND_BUY, LicenseType.fromString(null));

	}
	
	@Test
	public void licenseTypeToString() {
		assertEquals("Floating", LicenseType.FLOATING.toString());
		assertEquals("MultiFeature", LicenseType.NODE_LOCKED.toString());
		assertEquals("Subscription", LicenseType.SUBSCRIPTION.toString());
		assertEquals("TryAndBuy", LicenseType.TRY_AND_BUY.toString());
	}
	
	@Test
	public void licenseStatusToString() {
		assertEquals(LicenseStatus.CONNECTION_FAILURE, LicenseStatus.fromString("conncection-failure"));
		
		assertEquals(LicenseStatus.FLOATING_CHECKED_IN, LicenseStatus.fromString("floating-checked-in"));
		assertEquals(LicenseStatus.FLOATING_CHECKED_OUT, LicenseStatus.fromString("floating-checked-out"));
		assertEquals(LicenseStatus.FLOATING_EXPIRED, LicenseStatus.fromString("floating-expired"));
		assertEquals(LicenseStatus.FLOATING_OUT_OF_SESSION, LicenseStatus.fromString("floating-out-of-session"));
		
		assertEquals(LicenseStatus.NODE_LOCKED_EXPIRED, LicenseStatus.fromString("node-locked-expired"));
		assertEquals(LicenseStatus.NODE_LOCKED_HW_ID_FAILURE, LicenseStatus.fromString("node-locked-hw-id-failure"));
		assertEquals(LicenseStatus.NODE_LOCKED_REGISTERED, LicenseStatus.fromString("node-locked-registered"));
		
		assertEquals(LicenseStatus.FREE_EXPIRED, LicenseStatus.fromString("free-expired"));
		assertEquals(LicenseStatus.FREE_HW_ID_FAILURE, LicenseStatus.fromString("free-hw-id-failure"));
		assertEquals(LicenseStatus.FREE_REGISTERED, LicenseStatus.fromString("free-registered"));
		
		assertEquals(LicenseStatus.NONE, LicenseStatus.fromString("none"));
		assertEquals(LicenseStatus.NONE, LicenseStatus.fromString(null));
		assertEquals(LicenseStatus.NONE, LicenseStatus.fromString("none-of-the-above"));
	}
	
	@Test
	public void validationActionFromString() {
		assertEquals(ValidationAction.CHECK_IN, ValidationAction.fromString("check-in"));
		assertEquals(ValidationAction.CHECK_OUT, ValidationAction.fromString("check-out"));
		assertEquals(ValidationAction.NONE, ValidationAction.fromString("none"));
		assertEquals(ValidationAction.NONE, ValidationAction.fromString("none-of-the-above"));
		assertEquals(ValidationAction.NONE, ValidationAction.fromString(null));
	}
	
	@Test
	public void validationActionToString() {
		assertEquals("check-in", ValidationAction.CHECK_IN.toString());
		assertEquals("check-out", ValidationAction.CHECK_OUT.toString());
		assertEquals("none", ValidationAction.NONE.toString());
	}

}
