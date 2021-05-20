package eu.jsparrow.sample.postRule.migrateJUnitToJupiter;

import org.junit.Test;
import org.junit.jupiter.api.Tag;

public class ReplaceJUnit4CategoryWithJupiterTagRemoveCategoryImportRule {

	@Test
	@Tag("eu.jsparrow.sample.preRule.ReplaceJUnit4CategoryWithJupiterTagRemoveCategoryImportRule.ExampleCategory")
	void exampleMethod() {

	}

	interface ExampleCategory {

	}
}