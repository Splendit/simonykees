package eu.jsparrow.core.visitor.junit.jupiter.assertions;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.core.visitor.junit.jupiter.AbstractReplaceJUnit4AnnotationsWithJupiterASTVisitorTest;

public class ReplaceJUnit4AssertWithJupiterASTVisitorTest
		extends AbstractReplaceJUnit4AnnotationsWithJupiterASTVisitorTest {

	@BeforeEach
	public void setUpVisitor() throws Exception {
		addDependency("junit", "junit", "4.13");
		addDependency("org.junit.jupiter", "junit-jupiter-api", "5.4.0");
		setDefaultVisitor(new ReplaceJUnit4AssertWithJupiterASTVisitor());
	}

	@AfterEach
	public void tearDown() throws Exception {
		fixtureProject.clear();
	}

	@Test
	public void visit_assertArrayEqualsForObjectArray_shouldTransform() throws Exception {
		defaultFixture.addImport(org.junit.Assert.class.getName());
		defaultFixture.addImport(org.junit.jupiter.api.Test.class.getName());
		String original = "" +
				"	@Test\n" +
				"	void test() {\n" +
				"		Assert.assertEquals(new Object[] {}, new Object[] {});\n" +
				"	}";
		String expected = "" +
				"	@Test\n" +
				"	void test() {\n" +
				"		assertArrayEquals(new Object[]{},new Object[]{});\n" +
				"	}";

		List<String> expectedImports = Arrays.asList("import org.junit.Assert;", "import org.junit.jupiter.api.Test;",
				"import static org.junit.jupiter.api.Assertions.assertArrayEquals;");
		assertChange(original, expected, expectedImports);
	}

	@Test
	public void visit_withoutChangingAssertEqualsInvocation_shouldTransform() throws Exception {
		defaultFixture.addImport("org.junit.Assert.assertEquals", true, false);
		defaultFixture.addImport(org.junit.jupiter.api.Test.class.getName());
		String original = "" +
				"@Test\n"
				+ "	void test() {\n"
				+ "		assertEquals(10L, 10L);\n"
				+ "	}";

		List<String> expectedImports = Arrays.asList("import org.junit.jupiter.api.Test;",
				"import static org.junit.jupiter.api.Assertions.assertEquals;");
		assertChange(original, original, expectedImports);
	}
}
