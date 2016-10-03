package at.splendit.simonykees.sample.test;

import java.util.Arrays;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class TestArithmethicAssignmentRule extends AbstractReflectiveMethodTester {

	// we want this to be initialized only once for all parameterized values
	private static PreAndPostClassHolder holder;

	public TestArithmethicAssignmentRule(Integer value) {
		super(value);
	}

	@Parameters(name = "{index}: {0}")
	public static Iterable<? extends Integer> data() {
		return Arrays.asList(Integer.MIN_VALUE, -256, 1, 2, 17, Integer.MAX_VALUE);
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
				at.splendit.simonykees.sample.postRule.ArithmethicAssignmentRule.class, Integer.class);
	}
}
