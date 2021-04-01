package eu.jsparrow.sample.postRule.migrateJUnitToJupiter;

import static org.junit.Assert.assertThrows;

import java.io.IOException;

import org.junit.function.ThrowingRunnable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.api.Assertions;

public class ReplaceJUnit4AssertionsWithJupiterThrowingRunnableRule {

	ThrowingRunnable runnable = () -> throwsIOException("Simply throw an IOException");

	@Test
	public void testThrowingRunnableUsedExactlyOnce() {
		Executable runnable = () -> throwsIOException("Simply throw an IOException");
		Assertions.assertThrows(IOException.class, runnable, "Expecting IOException.");
	}

	@Test
	public void testThrowingRunnableUsedTwice() {
		ThrowingRunnable runnable = () -> throwsIOException("Simply throw an IOException");
		assertThrows("Expecting IOException.", IOException.class, runnable);
		assertThrows("Expecting IOException.", IOException.class, runnable);
	}

	@Test
	public void testThrowingRunnableTwoFragments() {
		ThrowingRunnable runnable1 = () -> throwsIOException("Simply throw an IOException"),
				runnable2 = () -> throwsIOException("Simply throw an IOException");
		assertThrows("Expecting IOException.", IOException.class, runnable1);
		assertThrows("Expecting IOException.", IOException.class, runnable2);
	}

	@Test
	public void testUsingThrowingRunnableField() {
		assertThrows("Expecting IOException.", IOException.class, runnable);
	}

	@Test
	public void testHidingLocalThrowingRunnable() {
		{
			ThrowingRunnable runnable = () -> throwsIOException("Simply throw an IOException");
		}
		assertThrows("Expecting IOException.", IOException.class, runnable);
	}

	@Test
	public void testAssignToNotInitializedThrowingRunnable() {
		Executable runnable;
		runnable = () -> throwsIOException("Simply throw an IOException");
		Assertions.assertThrows(IOException.class, runnable, "Expecting IOException.");
	}

	@Test
	public void testInitializationOfLocalRunnableThisRunnable() {
		ThrowingRunnable runnable = this.runnable;
		assertThrows("Expecting IOException.", IOException.class, runnable);
	}

	@Test
	public void testUseThisRunnable() {
		assertThrows("Expecting IOException.", IOException.class, this.runnable);
	}

	private void throwsIOException(String message) throws IOException {
		throw new IOException(message);
	}
}