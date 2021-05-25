package eu.jsparrow.sample.postRule.allRules;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

public class ReplaceJUnit4CategoryWithJupiterTagRemoveCategoryImportRule {

	@Test
	@Tag("eu.jsparrow.sample.preRule.ReplaceJUnit4CategoryWithJupiterTagRemoveCategoryImportRule.ExampleCategory")
	void exampleMethod() {

	}

	interface ExampleCategory {

	}
}