package eu.jsparrow.sample.postRule.allRules;

import static eu.jsparrow.sample.utilities.HelloWorld.HELLO_WORLD;
import static org.hamcrest.Matchers.equalToIgnoringCase;

import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ReplaceJUnitAssertThatWithHamcrestOnDemandImportsRule {

	@Test
	public void usingOnDemandImports() {
		MatcherAssert.assertThat(HELLO_WORLD, equalToIgnoringCase("Hello World!"));
	}

	@Test
	public void usingAssertions() {
		Assertions.assertEquals(HELLO_WORLD, "Hello World!");
	}

}
