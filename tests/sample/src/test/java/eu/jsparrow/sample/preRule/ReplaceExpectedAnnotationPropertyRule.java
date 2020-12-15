package eu.jsparrow.sample.preRule;

import java.io.IOException;

import org.junit.Test;

public class ReplaceExpectedAnnotationPropertyRule {
	
	@Test(expected = IOException.class)
	public void unexpectedException() throws IOException {
		throwsIOException("Simply throw an IOException");
	}
	
	private void throwsIOException(String message) throws IOException {
		throw new IOException(message);
	}

}
