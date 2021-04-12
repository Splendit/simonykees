package eu.jsparrow.sample.postRule.allRules;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalToIgnoringCase;

import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;

public class ReplaceJUnitAssertThatWithHamcrestRule {

	@Test
	public void replacingAssertThat() {
		assertThat("value", equalToIgnoringCase("value"));
	}

	@Test
	public void replacingAssertThatWithReason() {
		assertThat("Reason", "value", equalToIgnoringCase("value"));
	}

	@Test
	public void replaceQualifier() {
		MatcherAssert.assertThat("value", equalToIgnoringCase("value"));
	}

	@Test
	public void replaceFullyQualifiedName() {
		MatcherAssert.assertThat("value", equalToIgnoringCase("value"));
	}
}
