package eu.jsparrow.sample.preRule;

import static org.hamcrest.Matchers.equalToIgnoringCase;

import static org.junit.Assert.*;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class ReplaceJUnitAssertThatWithHamcrestDuplicateImportRule {

	@Test
	public void usingOnDemandImports() {
		assertThat("Hellow world", equalToIgnoringCase("Hello World!"));
	}
}
