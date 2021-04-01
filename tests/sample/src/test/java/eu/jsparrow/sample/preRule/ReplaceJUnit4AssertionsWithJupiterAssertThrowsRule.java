package eu.jsparrow.sample.preRule;

import static org.junit.Assert.assertThrows;

import java.io.IOException;

import org.junit.jupiter.api.Test;

public class ReplaceJUnit4AssertionsWithJupiterAssertThrowsRule {

	@Test
	public void testUsingAndLambda() {
		assertThrows(IOException.class, () -> throwsIOException("Simply throw an IOException"));
	}

	@Test
	public void testUsingMessageAndLambdaWithMessage() {
		assertThrows("Expecting IOException.", IOException.class,
				() -> throwsIOException("Simply throw an IOException"));
	}

	@Test
	public void testUsingMessageAndLambdaWithoutMessage() {
		assertThrows("Expecting IOException.", IOException.class, () -> throwsIOException());
	}

	@Test
	public void testUsingExpressionMethodReference() {
		assertThrows(IOException.class, this::throwsIOException);
	}

	@Test
	public void testUsingMessageAndExpressionMethodReference() {
		assertThrows("Expecting IOException.", IOException.class, this::throwsIOException);
	}

	private void throwsIOException(String message) throws IOException {
		throw new IOException(message);
	}

	private void throwsIOException() throws IOException {
		throw new IOException();
	}
}