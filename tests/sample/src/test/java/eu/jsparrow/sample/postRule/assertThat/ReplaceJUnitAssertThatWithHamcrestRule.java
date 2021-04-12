package eu.jsparrow.sample.postRule.assertThat;

import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.Assert;

import static org.hamcrest.Matchers.equalToIgnoringCase;

import org.junit.Test;
import org.hamcrest.MatcherAssert;

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
