package eu.jsparrow.sample.preRule;

import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.Test;

public class ReplaceTimeoutAnnotationPropertyRule {

	@Test(timeout = 500)
	public void shouldTimeOut() throws InterruptedException {
		Thread.sleep(2);
	}

	@Test(timeout = 500)
	public void multipleStatements() throws InterruptedException {
		Thread.sleep(2);
		assertTrue(true);
	}

	@Test(expected = IOException.class, timeout = 500)
	public void combinedAnnotationProperties() throws IOException {
		assertTrue(true);
		throwsIOException("new IOException");
	}

	private void throwsIOException(String message) throws IOException {
		throw new IOException(message);
	}
}
