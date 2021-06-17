package eu.jsparrow.sample.postRule.migrateJUnitToJupiter;

import static org.hamcrest.Matchers.equalToIgnoringCase;
import org.junit.Test;
import static org.hamcrest.junit.MatcherAssume.assumeThat;
import static org.hamcrest.CoreMatchers.everyItem;
import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.CoreMatchers.notNullValue;

public class ReplaceJUnit4AssumptionsWithHamcrestJUnitTransformImportsRule {

	@Test
	public void testAssumeNoException() {
		assumeThat("It is assumed that no Exception has been thrown.", null, nullValue());
	}

	@Test
	public void testAssumeNotNull() {
		assumeThat(asList(new Object(), new Object()), everyItem(notNullValue()));
	}

	@Test
	public void testAssumeThat() {
		assumeThat("value", equalToIgnoringCase("value"));
	}
}