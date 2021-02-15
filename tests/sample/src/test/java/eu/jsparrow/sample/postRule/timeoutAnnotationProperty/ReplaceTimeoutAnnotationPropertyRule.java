package eu.jsparrow.sample.postRule.timeoutAnnotationProperty;

import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.Test;
import static org.junit.jupiter.api.Assertions.assertTimeout;
import static java.time.Duration.ofMillis;

public class ReplaceTimeoutAnnotationPropertyRule {

	@Test
	public void shouldTimeOut() throws InterruptedException {
		assertTimeout(ofMillis(500), () -> Thread.sleep(2));
	}

	@Test
	public void multipleStatements() throws InterruptedException {
		assertTimeout(ofMillis(500), () -> {
			Thread.sleep(2);
			assertTrue(true);
		});
	}

	@Test(expected = IOException.class)
	public void combinedAnnotationProperties() throws IOException {
		assertTimeout(ofMillis(500), () -> {
			assertTrue(true);
			throwsIOException("new IOException");
		});
	}

	private void throwsIOException(String message) throws IOException {
		throw new IOException(message);
	}

	abstract class AbstractTestClass {
		@Test(timeout = 500)
		public abstract void combinedAnnotationProperties() throws IOException;
	}

}
