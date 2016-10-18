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
public class TestArithmethicAssignmentRule extends AbstractReflectiveMethodTester {
	
	private static Logger log = LogManager.getLogger(TestArithmethicAssignmentRule.class);

	// we want this to be initialized only once for all parameterized values
	private static PreAndPostClassHolder holder;

	public TestArithmethicAssignmentRule(ParameterType parameterType, Number value) {
		super(parameterType, value);
	}

	@Parameters(name = "{index}: {0}, {1}")
	public static Collection<Object[]> data() {
		return Arrays.asList(new Object[][] {
			{ParameterType.INTEGER, Integer.MIN_VALUE},
//			{ParameterType.INTEGER, -256},
//			{ParameterType.INTEGER, 0},
//			{ParameterType.INTEGER, 1},
//			{ParameterType.INTEGER, 2},
//			{ParameterType.INTEGER, 17},
//			{ParameterType.INTEGER, Integer.MAX_VALUE}, 
//			
			{ParameterType.DOUBLE, 2}
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
		holder = new PreAndPostClassHolder(at.splendit.simonykees.sample.preRule.ArithmethicAssignmentRule.class,
				at.splendit.simonykees.sample.postRule.ArithmethicAssignmentRule.class);
	}
	
	@AfterClass
	public static void printStatistics() {
		log.info(String.format("Test for class [%s] with the following values: \n\t[%s]", //$NON-NLS-1$
				holder.getPreObject().getClass().getSimpleName(), holder.getCounterToString()));
	}
}
