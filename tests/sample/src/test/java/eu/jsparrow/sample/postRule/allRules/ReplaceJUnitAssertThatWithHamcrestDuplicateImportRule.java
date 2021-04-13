package eu.jsparrow.sample.postRule.allRules;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalToIgnoringCase;

import org.junit.jupiter.api.Test;

public class ReplaceJUnitAssertThatWithHamcrestDuplicateImportRule {

	@Test
	public void usingOnDemandImports() {
		assertThat("Hellow world", equalToIgnoringCase("Hello World!"));
	}
}
