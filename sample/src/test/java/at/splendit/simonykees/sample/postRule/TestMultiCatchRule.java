package at.splendit.simonykees.sample.postRule;

import java.lang.reflect.InvocationTargetException;

import org.junit.Test;

/**
 * @author Martin Huter
 *
 */
@SuppressWarnings("nls")
public class TestMultiCatchRule {

	@Test
	public void tryWithResourceCommentBugTest() {
		// TODO meaningful Asserts?
		try {
			String.class.getConstructor(String.class).newInstance("aa");
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException e) {
			// Auto-generated catch block
			e.printStackTrace();
		}
	}

}
