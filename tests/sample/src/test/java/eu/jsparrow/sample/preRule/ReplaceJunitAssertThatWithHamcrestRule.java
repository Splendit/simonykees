package eu.jsparrow.sample.preRule;

import static org.junit.Assert.assertThat;
import static org.hamcrest.Matchers.equalToIgnoringCase;

import org.junit.Test;

public class ReplaceJunitAssertThatWithHamcrestRule {

	@Test
	public void replacingAssertThat() {
		assertThat("value", equalToIgnoringCase("value"));
	}
}
