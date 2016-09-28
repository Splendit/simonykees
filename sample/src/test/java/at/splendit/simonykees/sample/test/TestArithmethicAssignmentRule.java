package at.splendit.simonykees.sample.test;

import java.util.Arrays;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class TestArithmethicAssignmentRule extends AbstractReflectiveMethodTester {

	public TestArithmethicAssignmentRule(Integer value) {
		super(at.splendit.simonykees.sample.preRule.TestArithmethicAssignmentRule.class,
				at.splendit.simonykees.sample.postRule.TestArithmethicAssignmentRule.class, value);
	}

	@Parameters(name = "{index}: {0}")
	public static Iterable<? extends Integer> data() {
		return Arrays.asList(Integer.MIN_VALUE, -256, 1, 2, 17, Integer.MAX_VALUE);
	}

	@Test
	public void test() throws Exception {
		super.test();
	}
}
