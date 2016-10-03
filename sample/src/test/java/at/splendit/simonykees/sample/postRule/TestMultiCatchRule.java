package at.splendit.simonykees.sample.postRule;

import java.lang.reflect.InvocationTargetException;

import org.junit.Test;

/**
 * This test is a manual test to provide tests for the TryWithResource
 * replacement
 * 
 * 
 * How to Test: Do unit Test -> All tests should pass. Apply the [Usage] on the
 * file. Test the file again -> All tests should still pass and all methods of
 * Strings should be replaced by the corresponding StrinUtils implementation.
 * 
 * Usage: [Right Click in Editor] -> [Simoneykees/SelectRuleWizardHandler] ->
 * [MultiCatchTest auswählen] -> Finish This triggers the Event.
 * 
 * @author mgh
 *
 */
@SuppressWarnings("nls")
public class TestMultiCatchRule {

	@Test
	public void tryWithResourceCommentBugTest() {
		// TODO meaningful Asserts?
		try {
			String.class.getConstructor(String.class).newInstance("aa");
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
			// Auto-generated catch block
			e.printStackTrace();
		}
	}

}
