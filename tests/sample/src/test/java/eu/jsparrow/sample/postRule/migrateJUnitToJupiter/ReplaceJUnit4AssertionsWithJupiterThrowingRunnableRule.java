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
	public void testInitializationWithLambda() {
		Executable runnable = () -> throwsIOException("Simply throw an IOException");
		Assertions.assertThrows(IOException.class, runnable, "Expecting IOException.");
	}

	@Test
	public void testInitializationWithFieldThisRunnable() {
		ThrowingRunnable runnable = this.runnable;
		assertThrows("Expecting IOException.", IOException.class, runnable);
	}

	@Test
	public void testInitializationWithNull() {
		Executable runnable = null;
		Assertions.assertThrows(IOException.class, runnable, "Expecting IOException.");
	}

	@Test
	public void testInitializationWithAnonymousClass() {
		ThrowingRunnable runnable = new ThrowingRunnable() {

			@Override
			public void run() throws Throwable {
				throwsIOException("Simply throw an IOException");
			}
		};
		assertThrows("Expecting IOException.", IOException.class, runnable);
	}

	@Test
	public void testNoInitialization() {
		Executable runnable;
		runnable = () -> throwsIOException("Simply throw an IOException");
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
	public void testHidingLocalThrowingRunnable() {
		{
			ThrowingRunnable runnable = () -> throwsIOException("Simply throw an IOException");
		}
		assertThrows("Expecting IOException.", IOException.class, runnable);
	}

	@Test
	public void testUsingFieldRunnable() {
		assertThrows("Expecting IOException.", IOException.class, runnable);
	}

	@Test
	public void testUsingFieldThisRunnable() {
		assertThrows("Expecting IOException.", IOException.class, this.runnable);
	}

	@Test
	public void testUsingParameterRunnable(ThrowingRunnable runnable) {
		assertThrows("Expecting IOException.", IOException.class, runnable);
	}

	private void throwsIOException(String message) throws IOException {
		throw new IOException(message);
	}
}