package eu.jsparrow.sample.test;

import java.util.Arrays;
import java.util.Collection;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO SIM-103 add class description
 * 
 * @author Martin Huter, Ludwig Werzowa
 * @since 0.9
 */
@RunWith(Parameterized.class)
public class TestStringUtilsRefactorRule extends AbstractReflectiveMethodTester {
	
	private static Logger log = LoggerFactory.getLogger(TestStringUtilsRefactorRule.class);

	private static PreAndPostClassHolder holder;

	public TestStringUtilsRefactorRule(ParameterType parameterType, String value) {
		super(parameterType, value);
	}

	@SuppressWarnings("nls")
	@Parameters(name = "{index}: {0}, {1}")
	public static Collection<Object[]> data() {
		return Arrays.asList(new Object[][] {
			{ ParameterType.STRING, "" },
			{ ParameterType.STRING, "   " },
			{ ParameterType.STRING, "" },
			{ ParameterType.STRING, "notEmpty" },
			{ ParameterType.STRING, "  trimMe  " },
			{ ParameterType.STRING, "equal" },
			{ ParameterType.STRING, "endsWith" },
			{ ParameterType.STRING, "startWith" },
			{ ParameterType.STRING, "contains" },
			{ ParameterType.STRING, "replaceMe" },
			{ ParameterType.STRING, "lowerCASE" },
			{ ParameterType.STRING, "UPPERcase" },
			{ ParameterType.STRING, "please,dont,split,me" },
			{ ParameterType.STRING, "please;dont split,me" },
			{ ParameterType.STRING, "5|12345|value1|value2|value3|value4+5|777|value1|value2|value3|value4?5|777|value1|value2|value3|value4+" },
			{ ParameterType.STRING, String.valueOf(Integer.MAX_VALUE) },
			{ ParameterType.STRING, String.valueOf(Double.MAX_VALUE) },
			{ ParameterType.STRING, RandomStringUtils.randomAscii(Short.MAX_VALUE) }
		});
	}

	@Test
	public void test() throws Exception {
		super.test();
	}

	@Override
	protected PreAndPostClassHolder getHolder() {
		return holder;
	}

	@BeforeClass
	public static void setUptHolderInstance() throws Exception {
		holder = new PreAndPostClassHolder(eu.jsparrow.sample.preRule.StringUtilsRefactorRule.class,
				eu.jsparrow.sample.postRule.allRules.StringUtilsRefactorRule.class);
	}
	
	@AfterClass
	public static void printStatistics() {
		log.info(String.format("Test for class [%s] finished with the following stats: %n[%s]", //$NON-NLS-1$
				holder.getPreObject().getClass().getSimpleName(), holder.getCounterToString()));
	}

}
