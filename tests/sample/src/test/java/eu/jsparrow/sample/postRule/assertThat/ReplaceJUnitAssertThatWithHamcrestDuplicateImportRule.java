package eu.jsparrow.sample.postRule.assertThat;

import static org.hamcrest.Matchers.equalToIgnoringCase;

import static org.junit.Assert.*;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.Test;

public class ReplaceJUnitAssertThatWithHamcrestDuplicateImportRule {

	@Test
	public void usingOnDemandImports() {
		assertThat("Hellow world", equalToIgnoringCase("Hello World!"));
	}
}
