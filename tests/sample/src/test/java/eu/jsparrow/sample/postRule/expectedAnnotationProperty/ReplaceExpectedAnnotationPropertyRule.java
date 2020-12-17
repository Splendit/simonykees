package eu.jsparrow.sample.postRule.expectedAnnotationProperty;

import java.io.IOException;

import org.junit.Test;
import static org.junit.Assert.assertThrows;

public class ReplaceExpectedAnnotationPropertyRule {
	
	@Test
	public void unexpectedException() {
		assertThrows(IOException.class, () -> throwsIOException("Simply throw an IOException"));
	}
	
	private void throwsIOException(String message) throws IOException {
		throw new IOException(message);
	}

}
