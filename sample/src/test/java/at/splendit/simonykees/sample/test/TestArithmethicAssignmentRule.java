package at.splendit.simonykees.sample.test;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class TestArithmethicAssignmentRule {
	
	// parametrized
	private int value;

	private at.splendit.simonykees.sample.preRule.TestArithmethicAssignmentRule preObj = new at.splendit.simonykees.sample.preRule.TestArithmethicAssignmentRule();
	private at.splendit.simonykees.sample.postRule.TestArithmethicAssignmentRule postObj = new at.splendit.simonykees.sample.postRule.TestArithmethicAssignmentRule();
	
	private Map<String, Method> preMethods;
	private Map<String, Method> postMethods;
	
	public TestArithmethicAssignmentRule(int value) {
		this.value = value;
		preMethods = initMethodMap(preObj.getClass());
		postMethods = initMethodMap(postObj.getClass());
	}
	
	@Parameters(name = "{index}: {0}")
	public static Iterable<? extends Integer> data() {
	    return Arrays.asList(1, 2, 17, Integer.MAX_VALUE);
	}
	
	private Map<String, Method> initMethodMap(Class<?> clazz) {
		Map<String, Method> retVal = new HashMap<>();
		for (Method m : clazz.getDeclaredMethods()) {
			retVal.put(m.getName(), m);
		}
		return retVal;
	}
	
	@SuppressWarnings("nls")
	@Test
	public void test() throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		for (Method m : preMethods.values()) {
			Object preRetVal = m.invoke(preObj, value);
			Object postRetVal = postMethods.get(m.getName()).invoke(postObj, value);
			System.out.println(String.format("Method: %s, value: %d, preRetVal: %s, postRetVal: %s", m.getName(), value, preRetVal, postRetVal));
			assertEquals(preRetVal, postRetVal);
		}
	}
}
