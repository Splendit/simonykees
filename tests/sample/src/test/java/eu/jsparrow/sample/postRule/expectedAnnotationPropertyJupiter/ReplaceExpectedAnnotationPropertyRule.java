package eu.jsparrow.sample.postRule.expectedAnnotationPropertyJupiter;

import java.io.IOException;

import org.junit.Test;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ReplaceExpectedAnnotationPropertyRule {
	
	@Test
	public void unexpectedException() {
		assertThrows(IOException.class, () -> throwsIOException("Simply throw an IOException"));
	}
	
	private void throwsIOException(String message) throws IOException {
		throw new IOException(message);
	}

}
