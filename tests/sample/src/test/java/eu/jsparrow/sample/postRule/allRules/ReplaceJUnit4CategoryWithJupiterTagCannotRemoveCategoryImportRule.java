package eu.jsparrow.sample.postRule.allRules;

import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.jupiter.api.Tag;

public class ReplaceJUnit4CategoryWithJupiterTagCannotRemoveCategoryImportRule {

	@Test
	@Tag("eu.jsparrow.sample.preRule.ReplaceJUnit4CategoryWithJupiterTagCannotRemoveCategoryImportRule.ExampleCategory")
	void exampleMethod() {
		final Class<Category> clazz = Category.class;
	}

	interface ExampleCategory {

	}
}