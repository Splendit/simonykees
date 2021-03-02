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
				"	void test(){\n" +
				"		Assertions.assertArrayEquals(new Object[]{},new Object[]{});\n" +
				"	}";

		List<String> expectedImports = Arrays.asList("import org.junit.Assert;", "import org.junit.Test;",
				"import org.junit.jupiter.api.Assertions;");
		assertChange(original, expected, expectedImports);
	}
}
