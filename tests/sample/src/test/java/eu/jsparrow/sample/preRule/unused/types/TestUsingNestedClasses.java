package eu.jsparrow.sample.preRule.unused.types;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

class TestUsingNestedClasses {

	@Test
	void test() {
		assertNotNull(new ClassesUsedByTestExclusively().new ClassOnlyUsedByTest());
		assertNotNull(new ClassesUsedByTestExclusively().new ClassOnlyUsedByTest2());
	}
}
