package eu.jsparrow.sample.preRule;

import static org.junit.Assert.assertThrows;

import java.io.IOException;

import org.junit.jupiter.api.Test;

public class ReplaceJUnit4AssertionsWithJupiterAssertThrowsRule {

	class ConditionallyTransformed {

		@Test
		public void unexpectedException() {
			assertThrows("Expecting IOException.", IOException.class,
					() -> throwsIOException("Simply throw an IOException"));
		}

		private void throwsIOException(String message) throws IOException {
			throw new IOException(message);
		}
	}
}