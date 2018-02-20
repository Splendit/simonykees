package eu.jsparrow.sample.test;

import java.util.Arrays;
import java.util.Collection;

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
 * @author Martin Huter
 * @since 0.9.2
 */
@RunWith(Parameterized.class)
public class TestWhileToForEachRule extends AbstractReflectiveMethodTester {

	private static Logger log = LoggerFactory.getLogger(TestWhileToForEachRule.class);

	private static PreAndPostClassHolder holder;

	public TestWhileToForEachRule(ParameterType parameterType, String value) {
		super(parameterType, value);
	}

	@SuppressWarnings("nls")
	@Parameters(name = "{index}: {0}, {1}")
	public static Collection<Object[]> data() {
		return Arrays.asList(new Object[][] { { ParameterType.STRING, "a;b;c" } });
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
		holder = new PreAndPostClassHolder(eu.jsparrow.sample.preRule.TestWhileToForEachRule.class,
				eu.jsparrow.sample.postRule.allRules.TestWhileToForEachRule.class);
	}

	@AfterClass
	public static void printStatistics() {
		log.info(String.format("Test for class [%s] finished with the following stats: %n[%s]", //$NON-NLS-1$
				holder.getPreObject().getClass().getSimpleName(), holder.getCounterToString()));
	}

}
