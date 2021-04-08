package eu.jsparrow.sample.postRule.assertThat;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalToIgnoringCase;

import org.junit.Test;

public class ReplaceJunitAssertThatWithHamcrestRule {

	@Test
	public void replacingAssertThat() {
		assertThat("value", equalToIgnoringCase("value"));
	}
}
