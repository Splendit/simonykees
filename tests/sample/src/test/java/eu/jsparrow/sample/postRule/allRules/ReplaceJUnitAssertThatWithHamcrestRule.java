package eu.jsparrow.sample.postRule.allRules;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalToIgnoringCase;

import org.junit.jupiter.api.Test;

public class ReplaceJUnitAssertThatWithHamcrestRule {

	@Test
	public void replacingAssertThat() {
		assertThat("value", equalToIgnoringCase("value"));
	}
}
