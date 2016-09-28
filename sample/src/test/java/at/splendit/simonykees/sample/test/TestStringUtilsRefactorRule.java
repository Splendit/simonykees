package at.splendit.simonykees.sample.test;

import java.util.Arrays;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class TestStringUtilsRefactorRule extends AbstractReflectiveMethodTester {

	public TestStringUtilsRefactorRule(String value) {
		super(at.splendit.simonykees.sample.preRule.TestStringUtilsRefactorRule.class,
				at.splendit.simonykees.sample.postRule.TestStringUtilsRefactorRule.class, value);
	}

	@SuppressWarnings("nls")
	@Parameters(name = "{index}: {0}")
	public static Iterable<? extends String> data() {
//		return Arrays.asList(""); // FIXME this case does not work. See JIRA: https://jira.splendit.loc/browse/LJA-119.
		return Arrays.asList("notEmpty", "  trimMe  ", "equal", "endsWith", "startWith", "contains", "replaceMe",
				"lowerCASE", "UPPERcase", "please,dont,split,me", "please;dont split,me");
	}

	@Test
	public void test() throws Exception {
		super.test();
	}

}
