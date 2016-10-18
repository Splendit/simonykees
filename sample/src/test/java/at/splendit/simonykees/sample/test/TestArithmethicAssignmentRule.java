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

	public TestArithmethicAssignmentRule(ParameterType parameterType, Object value) {
		super(parameterType, value);
	}

	@Parameters(name = "{index}: {0}, {1}")
	public static Collection<Object[]> data() {
		return Arrays.asList(new Object[][] {
			{ParameterType.INTEGER, Integer.MIN_VALUE},
			{ParameterType.INTEGER, -256},
			{ParameterType.INTEGER, 0},
			{ParameterType.INTEGER, 1},
			{ParameterType.INTEGER, 2},
			{ParameterType.INTEGER, 17},
			{ParameterType.INTEGER, Integer.MAX_VALUE}, 
			
			{ParameterType.DOUBLE, Double.valueOf(0)},
			{ParameterType.DOUBLE, Double.valueOf(1)},
			{ParameterType.DOUBLE, Double.valueOf(2)},
			{ParameterType.DOUBLE, Double.MAX_VALUE},
			{ParameterType.DOUBLE, Double.MIN_VALUE},
			{ParameterType.DOUBLE, Double.valueOf(Integer.MAX_VALUE)}, 
			
			{ParameterType.FLOAT, Float.valueOf(0)},
			{ParameterType.FLOAT, Float.valueOf(1)},
			{ParameterType.FLOAT, Float.valueOf(2)},
			{ParameterType.FLOAT, Float.MAX_VALUE},
			{ParameterType.FLOAT, Float.MIN_VALUE},
			{ParameterType.FLOAT, Float.valueOf(Integer.MAX_VALUE)},
			
			{ParameterType.LONG, Long.valueOf(0)},
			{ParameterType.LONG, Long.valueOf(1)},
			{ParameterType.LONG, Long.valueOf(2)},
			{ParameterType.LONG, Long.MAX_VALUE},
			{ParameterType.LONG, Long.MIN_VALUE},
			{ParameterType.LONG, Long.valueOf(Integer.MAX_VALUE)},
			
			{ParameterType.SHORT, Short.valueOf("0")}, //$NON-NLS-1$
			{ParameterType.SHORT, Short.valueOf("1")}, //$NON-NLS-1$
			{ParameterType.SHORT, Short.valueOf("2")}, //$NON-NLS-1$
			{ParameterType.SHORT, Short.MAX_VALUE},
			{ParameterType.SHORT, Short.MIN_VALUE},
			
			{ParameterType.BYTE, Byte.valueOf("0")}, //$NON-NLS-1$
			{ParameterType.BYTE, Byte.valueOf("1")}, //$NON-NLS-1$
			{ParameterType.BYTE, Byte.valueOf("2")}, //$NON-NLS-1$
			{ParameterType.BYTE, Byte.MAX_VALUE},
			{ParameterType.BYTE, Byte.MIN_VALUE},
			
			// Note: Character is not a subclass of Number
			{ParameterType.CHARACTER, '0'},
			{ParameterType.CHARACTER, '1'},
			{ParameterType.CHARACTER, '2'},
			{ParameterType.CHARACTER, 'a'},
			{ParameterType.CHARACTER, 'Z'},
			{ParameterType.CHARACTER, Character.MAX_VALUE},
			{ParameterType.CHARACTER, Character.MIN_VALUE}

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
