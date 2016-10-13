package at.splendit.simonykees.sample.test;

import java.util.Arrays;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class TestStringUtilsRefactorRule extends AbstractReflectiveMethodTester {

	private static PreAndPostClassHolder holder;

	public TestStringUtilsRefactorRule(String value) {
		super(value);
	}

	@SuppressWarnings("nls")
	@Parameters(name = "{index}: {0}")
	public static Iterable<? extends String> data() {
		return Arrays.asList("", "   ", "	", "notEmpty", "  trimMe  ", "equal", "endsWith", "startWith", "contains",
				"replaceMe", "lowerCASE", "UPPERcase", "please,dont,split,me", "please;dont split,me",
				"5|12345|value1|value2|value3|value4+5|777|value1|value2|value3|value4?5|777|value1|value2|value3|value4+",
				String.valueOf(Integer.MAX_VALUE), String.valueOf(Double.MAX_VALUE),
				RandomStringUtils.randomAscii(Short.MAX_VALUE));
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
		holder = new PreAndPostClassHolder(at.splendit.simonykees.sample.preRule.StringUtilsRefactorRule.class,
				at.splendit.simonykees.sample.postRule.StringUtilsRefactorRule.class, String.class);
	}

}
