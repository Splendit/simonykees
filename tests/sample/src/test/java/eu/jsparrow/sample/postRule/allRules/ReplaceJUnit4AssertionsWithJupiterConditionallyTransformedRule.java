package eu.jsparrow.sample.postRule.allRules;

import static java.time.Duration.ofMillis;
import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeout;

import java.io.IOException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ReplaceJUnit4AssertionsWithJupiterConditionallyTransformedRule {

	class ConditionallyTransformed {

		@Test
		public void testWithTimeout10000() {
			assertTimeout(ofMillis(10000), () -> assertEquals(10L, 10L));
		}

		@Test
		public void unexpectedException() {
			Assertions.assertEquals(10L, 10L);
			assertThrows(IOException.class, () -> throwsIOException("Simply throw an IOException"));
		}

		private void throwsIOException(String message) throws IOException {
			throw new IOException(message);
		}
	}
}