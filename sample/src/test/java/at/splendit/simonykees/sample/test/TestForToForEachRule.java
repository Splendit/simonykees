package at.splendit.simonykees.sample.test;

import java.util.Arrays;
import java.util.Collection;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class TestForToForEachRule extends AbstractReflectiveMethodTester {

	private static Logger log = LogManager.getLogger(TestForToForEachRule.class);

	private static PreAndPostClassHolder holder;

	public TestForToForEachRule(ParameterType parameterType, String value) {
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
		holder = new PreAndPostClassHolder(at.splendit.simonykees.sample.preRule.TestForToForEachRule.class,
				at.splendit.simonykees.sample.postRule.allRules.TestForToForEachRule.class);
	}

	@AfterClass
	public static void printStatistics() {
		log.info(String.format("Test for class [%s] finished with the following stats: %n[%s]", //$NON-NLS-1$
				holder.getPreObject().getClass().getSimpleName(), holder.getCounterToString()));
	}

}
