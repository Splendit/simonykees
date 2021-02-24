package eu.jsparrow.core.visitor.junit.jupiter.assertions;

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

	/**
	 * SIM-1892: This test is expected to fail as soon as the visitor will carry
	 * out re-factoring operations
	 * 
	 */
	@Test
	public void visit_assertArrayEqualsForObjectArray_shouldNotTransform() throws Exception {
		defaultFixture.addImport(org.junit.Assert.class.getName());
		defaultFixture.addImport(org.junit.Test.class.getName());
		String original = "" +
				"	@Test\n" +
				"	void test() {\n" +
				"		Assert.assertEquals(new Object[] {}, new Object[] {});\n" +
				"	}";
		assertNoChange(original);
	}

	/**
	 * SIM-1892: This test is expected to fail as soon as the visitor will carry
	 * out re-factoring operations
	 * 
	 */
	@Test
	public void visit_assertArrayEqualsMethodReference_shouldNotTransform() throws Exception {
		defaultFixture.addImport(java.util.function.BiConsumer.class.getName());
		defaultFixture.addImport(org.junit.Assert.class.getName());
		defaultFixture.addImport(org.junit.Test.class.getName());

		String original = "" +
				"	@Test\n" +
				"	void test() {\n" +
				"		BiConsumer<Byte[], Byte[]> assertByteArrayEquals = Assert::assertArrayEquals;\n" +
				"	}";

		assertNoChange(original);
	}

}
