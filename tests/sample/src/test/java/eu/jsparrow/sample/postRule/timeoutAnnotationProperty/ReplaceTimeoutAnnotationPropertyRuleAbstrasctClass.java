package eu.jsparrow.sample.postRule.timeoutAnnotationProperty;

import java.io.IOException;

import org.junit.Test;

public abstract class ReplaceTimeoutAnnotationPropertyRuleAbstrasctClass {

	@Test(timeout = 500)
	public abstract void combinedAnnotationProperties() throws IOException;

}
