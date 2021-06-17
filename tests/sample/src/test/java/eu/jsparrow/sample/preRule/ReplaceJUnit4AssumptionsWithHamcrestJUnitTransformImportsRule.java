package eu.jsparrow.sample.preRule;

import static org.hamcrest.Matchers.equalToIgnoringCase;
import static org.junit.Assume.assumeNoException;
import static org.junit.Assume.assumeNotNull;
import static org.junit.Assume.assumeThat;

import org.junit.Test;

public class ReplaceJUnit4AssumptionsWithHamcrestJUnitTransformImportsRule {

	@Test
	public void testAssumeNoException() {
		assumeNoException("It is assumed that no Exception has been thrown.", null);
	}

	@Test
	public void testAssumeNotNull() {
		assumeNotNull(new Object(), new Object());
	}

	@Test
	public void testAssumeThat() {
		assumeThat("value", equalToIgnoringCase("value"));
	}
}