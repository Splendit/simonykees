package eu.jsparrow.sample.preRule;

import static org.junit.Assert.assertThrows;

import java.io.IOException;

import org.junit.function.ThrowingRunnable;
import org.junit.jupiter.api.Test;

public class ReplaceJUnit4AssertionsWithJupiterAssertThrowsRule {

	@Test
	public void expectingIOException() {
		assertThrows("Expecting IOException.", IOException.class,
				() -> throwsIOException("Simply throw an IOException"));
	}

	@Test
	public void expectingIOExceptionUsingThrowingRunnableVariable() {
		ThrowingRunnable runnable = () -> throwsIOException("Simply throw an IOException");
		assertThrows("Expecting IOException.", IOException.class,
				runnable);
	}

	private void throwsIOException(String message) throws IOException {
		throw new IOException(message);
	}
}