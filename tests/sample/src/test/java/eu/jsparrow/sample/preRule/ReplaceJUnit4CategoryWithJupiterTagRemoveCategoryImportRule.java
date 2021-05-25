package eu.jsparrow.sample.preRule;

import org.junit.Test;
import org.junit.experimental.categories.Category;

public class ReplaceJUnit4CategoryWithJupiterTagRemoveCategoryImportRule {

	@Test
	@Category(ExampleCategory.class)
	void exampleMethod() {

	}

	interface ExampleCategory {

	}
}