package eu.jsparrow.maven.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class BannerUtilTest {

	private static String EXPECTED_VERSION_STRING = "jSparrow Maven Plugin                      v2.0.0"; //$NON-NLS-1$
	private static int EXPECTED_VERSION_INFORMATION_SIZE = EXPECTED_VERSION_STRING.length();

	@Test
	public void getVersionInformation_specificVersion_expectCertainString() throws Exception {
		String versionInfo = BannerUtil.getVersionInformation("2.0.0"); //$NON-NLS-1$
		assertEquals(EXPECTED_VERSION_STRING, versionInfo);
	}
	
	@Test
	public void getVersionInformation_longVersion_expectCertainLength() throws Exception {
		String versionInfo = BannerUtil.getVersionInformation("12.10.199-SNAPSHOT"); //$NON-NLS-1$
		assertEquals(EXPECTED_VERSION_INFORMATION_SIZE, versionInfo.length());
	}

	@Test
	public void getVersionInformation_shortVersion_expectCertainLength() throws Exception {
		String versionInfo = BannerUtil.getVersionInformation("1"); //$NON-NLS-1$
		assertEquals(EXPECTED_VERSION_INFORMATION_SIZE, versionInfo.length());
	}

}
