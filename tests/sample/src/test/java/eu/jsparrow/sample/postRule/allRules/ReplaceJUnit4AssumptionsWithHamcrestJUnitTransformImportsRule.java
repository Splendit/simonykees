package eu.jsparrow.sample.postRule.allRules;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.everyItem;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.equalToIgnoringCase;
import static org.hamcrest.junit.MatcherAssume.assumeThat;

import org.junit.jupiter.api.Test;

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