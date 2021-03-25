package eu.jsparrow.sample.postRule.allRules;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;

import org.junit.jupiter.api.Test;

public class ReplaceJUnit4AssertionsWithJupiterAssertThrowsRule {

	class ConditionallyTransformed {

		@Test
		public void unexpectedException() {
			assertThrows(IOException.class, () -> throwsIOException("Simply throw an IOException"),
					"Expecting IOException.");
		}

		private void throwsIOException(String message) throws IOException {
			throw new IOException(message);
		}
	}
}