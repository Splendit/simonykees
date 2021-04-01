package eu.jsparrow.sample.preRule;

import static org.junit.Assert.assertThrows;

import java.io.IOException;

import org.junit.function.ThrowingRunnable;
import org.junit.jupiter.api.Test;

public class ReplaceJUnit4AssertionsWithJupiterThrowingRunnableRule {

	ThrowingRunnable runnable = () -> throwsIOException("Simply throw an IOException");

	@Test
	public void testInitializationWithLambda() {
		ThrowingRunnable runnable = () -> throwsIOException("Simply throw an IOException");
		assertThrows("Expecting IOException.", IOException.class, runnable);
	}
	
	@Test
	public void testInitializationWithExpressionMethodReference() {
		ThrowingRunnable runnable = this::throwsIOException;
		assertThrows("Expecting IOException.", IOException.class, runnable);
	}

	@Test
	public void testInitializationWithFieldThisRunnable() {
		ThrowingRunnable runnable = this.runnable;
		assertThrows("Expecting IOException.", IOException.class, runnable);
	}

	@Test
	public void testInitializationWithNull() {
		ThrowingRunnable runnable = null;
		assertThrows("Expecting IOException.", IOException.class, runnable);
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
		ThrowingRunnable runnable;
		runnable = () -> throwsIOException("Simply throw an IOException");
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
	
	private void throwsIOException() throws IOException {
		throw new IOException();
	}
}