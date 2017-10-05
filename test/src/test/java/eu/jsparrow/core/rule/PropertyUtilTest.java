package eu.jsparrow.core.rule;

import java.util.Arrays;
import java.util.Collection;

import org.apache.commons.lang3.JavaVersion;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import eu.jsparrow.core.util.PropertyUtil;

/**
 * Tests for getting the JavaVersion
 * 
 * @author Martin Huter
 * @since 2.2.1
 */
@RunWith(Parameterized.class)
public class PropertyUtilTest {

	@SuppressWarnings("nls")
	@Parameters(name = "{index}: InputString[{0}]")
	public static Collection<Object[]> data() {
		return Arrays.asList(new Object[][] { { "Other", JavaVersion.JAVA_1_1 }, { "1.1", JavaVersion.JAVA_1_1 },
				{ "1.2", JavaVersion.JAVA_1_2 }, { "1.3", JavaVersion.JAVA_1_3 }, { "1.4", JavaVersion.JAVA_1_4 },
				{ "1.5", JavaVersion.JAVA_1_5 }, { "1.6", JavaVersion.JAVA_1_6 }, { "1.7", JavaVersion.JAVA_1_7 },
				{ "1.8", JavaVersion.JAVA_1_8 }, { "9", JavaVersion.JAVA_1_8 }, { "1.9", JavaVersion.JAVA_1_8 } });
	}

	private String input;

	private JavaVersion expected;

	public PropertyUtilTest(String input, JavaVersion expected) {
		this.input = input;
		this.expected = expected;
	}

	@Test
	public void possibleInputStringTest() {
		Assert.assertEquals(expected, PropertyUtil.stringToJavaVersion(input));
	}
}
