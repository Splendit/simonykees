package eu.jsparrow.sample.preRule;

import static org.junit.Assert.assertThrows;

import java.io.IOException;

import org.junit.function.ThrowingRunnable;
import org.junit.jupiter.api.Test;

public class ReplaceJUnit4AssertionsWithJupiterThrowingRunnableRule {

	ThrowingRunnable runnable = () -> throwsIOException("Simply throw an IOException");

	@Test
	public void testThrowingRunnableUsedExactlyOnce() {
		ThrowingRunnable runnable = () -> throwsIOException("Simply throw an IOException");
		assertThrows("Expecting IOException.", IOException.class, runnable);
	}

	@Test
	public void testThrowingRunnableAsAnonymousClass() {
		ThrowingRunnable runnable = new ThrowingRunnable() {

			@Override
			public void run() throws Throwable {
				throwsIOException("Simply throw an IOException");
			}
		};
		assertThrows("Expecting IOException.", IOException.class, runnable);
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
		ThrowingRunnable runnable;
		runnable = () -> throwsIOException("Simply throw an IOException");
		assertThrows("Expecting IOException.", IOException.class, runnable);
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