package eu.jsparrow.sample.postRule.allRules;

import static org.junit.Assert.assertThrows;

import java.io.IOException;

import org.junit.function.ThrowingRunnable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

public class ReplaceJUnit4AssertionsWithJupiterThrowingRunnableRule {

	ThrowingRunnable runnable = () -> throwsIOException("Simply throw an IOException");

	@Test
	public void testThrowingRunnableUsedExactlyOnce() {
		final Executable runnable = () -> throwsIOException("Simply throw an IOException");
		Assertions.assertThrows(IOException.class, runnable, "Expecting IOException.");
	}

	@Test
	public void testThrowingRunnableUsedTwice() {
		final ThrowingRunnable runnable = () -> throwsIOException("Simply throw an IOException");
		assertThrows("Expecting IOException.", IOException.class, runnable);
		assertThrows("Expecting IOException.", IOException.class, runnable);
	}

	@Test
	public void testThrowingRunnableTwoFragments() {
		final Executable runnable1 = () -> throwsIOException("Simply throw an IOException");
		final Executable runnable2 = () -> throwsIOException("Simply throw an IOException");
		Assertions.assertThrows(IOException.class, runnable1, "Expecting IOException.");
		Assertions.assertThrows(IOException.class, runnable2, "Expecting IOException.");
	}

	@Test
	public void testUsingThrowingRunnableField() {
		assertThrows("Expecting IOException.", IOException.class, runnable);
	}

	@Test
	public void testHidingLocalThrowingRunnable() {
		{
			final ThrowingRunnable runnable = () -> throwsIOException("Simply throw an IOException");
		}
		assertThrows("Expecting IOException.", IOException.class, runnable);
	}

	@Test
	public void testAssignToNotInitializedThrowingRunnable() {
		final Executable runnable;
		runnable = () -> throwsIOException("Simply throw an IOException");
		Assertions.assertThrows(IOException.class, runnable, "Expecting IOException.");
	}

	@Test
	public void testInitializationOfLocalRunnableThisRunnable() {
		final ThrowingRunnable runnable = this.runnable;
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