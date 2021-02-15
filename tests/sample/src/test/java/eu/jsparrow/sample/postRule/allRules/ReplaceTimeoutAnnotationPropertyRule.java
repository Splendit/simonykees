package eu.jsparrow.sample.postRule.allRules;

import static java.time.Duration.ofMillis;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeout;

import java.io.IOException;

import org.junit.Test;

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

	@Test
	public void combinedAnnotationProperties() {
		assertTimeout(ofMillis(500), () -> {
			assertTrue(true);
			assertThrows(IOException.class, () -> throwsIOException("new IOException"));
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
