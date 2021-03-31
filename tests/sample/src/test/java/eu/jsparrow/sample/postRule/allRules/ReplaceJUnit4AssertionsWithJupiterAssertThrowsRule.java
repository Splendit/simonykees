package eu.jsparrow.sample.postRule.allRules;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

public class ReplaceJUnit4AssertionsWithJupiterAssertThrowsRule {

	@Test
	public void expectingIOException() {
		assertThrows(IOException.class, () -> throwsIOException("Simply throw an IOException"),
				"Expecting IOException.");
	}

	@Test
	public void expectingIOExceptionUsingThrowingRunnableVariable() {
		final Executable runnable = () -> throwsIOException("Simply throw an IOException");
		assertThrows(IOException.class, runnable, "Expecting IOException.");
	}

	private void throwsIOException(String message) throws IOException {
		throw new IOException(message);
	}
}