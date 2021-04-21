package eu.jsparrow.sample.preRule;

import static org.junit.Assert.assertThat;

import org.junit.Assert;

import static org.hamcrest.Matchers.equalToIgnoringCase;

import org.junit.Test;

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
		Assert.assertThat("value", equalToIgnoringCase("value"));
	}
	
	@Test
	public void replaceFullyQualifiedName() {
		org.junit.Assert.assertThat("value", equalToIgnoringCase("value"));
	}
}
