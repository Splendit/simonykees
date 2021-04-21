package eu.jsparrow.sample.postRule.assertThat;

import static eu.jsparrow.sample.utilities.HelloWorld.HELLO_WORLD;
import static org.hamcrest.Matchers.equalToIgnoringCase;
import static org.junit.Assert.*;

import org.junit.Test;
import org.hamcrest.MatcherAssert;

public class ReplaceJUnitAssertThatWithHamcrestOnDemandImportsRule {
	
	@Test
	public void usingOnDemandImports() {
		MatcherAssert.assertThat(HELLO_WORLD, equalToIgnoringCase("Hello World!"));
	}
	
	@Test
	public void usingAssertions() {
		assertEquals(HELLO_WORLD, "Hello World!");
	}

}
