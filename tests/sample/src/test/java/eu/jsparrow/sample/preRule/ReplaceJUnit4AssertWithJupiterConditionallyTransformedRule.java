package eu.jsparrow.sample.preRule;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.Test;

public class ReplaceJUnit4AssertWithJupiterConditionallyTransformedRule {

	class ConditionallyTransformed {

		@Test(timeout = 10000)
		public void testWithTimeout10000() {
			assertEquals(10L, 10L);
		}

		@Test(expected = IOException.class)
		public void unexpectedException() throws IOException {
			assertEquals(10L, 10L);
			throwsIOException("Simply throw an IOException");
		}

		private void throwsIOException(String message) throws IOException {
			throw new IOException(message);
		}
	}
}