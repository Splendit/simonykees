package eu.jsparrow.sample.postRule.allRules;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;

import org.junit.jupiter.api.Test;

public class ReplaceJUnit4AssertionsWithJupiterAssertThrowsRule {

	@Test
	public void testUsingAndLambda() {
		assertThrows(IOException.class, () -> throwsIOException("Simply throw an IOException"));
	}

	@Test
	public void testUsingMessageAndLambdaWithMessage() {
		assertThrows(IOException.class, () -> throwsIOException("Simply throw an IOException"),
				"Expecting IOException.");
	}

	@Test
	public void testUsingMessageAndLambdaWithoutMessage() {
		assertThrows(IOException.class, this::throwsIOException, "Expecting IOException.");
	}

	@Test
	public void testUsingExpressionMethodReference() {
		assertThrows(IOException.class, this::throwsIOException);
	}

	@Test
	public void testUsingMessageAndExpressionMethodReference() {
		assertThrows(IOException.class, this::throwsIOException, "Expecting IOException.");
	}

	private void throwsIOException(String message) throws IOException {
		throw new IOException(message);
	}

	private void throwsIOException() throws IOException {
		throw new IOException();
	}
}