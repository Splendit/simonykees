package eu.jsparrow.sample.preRule;

import org.junit.Test;
import org.junit.experimental.categories.Category;

public class ReplaceJUnit4CategoryWithJupiterTagCannotRemoveCategoryImportRule {

	@Test
	@Category(ExampleCategory.class)
	void exampleMethod() {
		Class<Category> clazz = Category.class;
	}

	interface ExampleCategory {

	}
}