package at.splendit.simonykees.core.license;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 * This test class only ensures that the test environment is being used, because we basically run our license tests against
 * the Net Licensing test account.    
 * @author ali
 *
 */
public class LicensePropertiesTest {

	// Make sure tests are running in the Test environment
	@Test
	public void testLicenseProperties() {
				
	    assertEquals( "Test", LicenseProperties.LICENSE_ENVIRONMENT);
	}

}
