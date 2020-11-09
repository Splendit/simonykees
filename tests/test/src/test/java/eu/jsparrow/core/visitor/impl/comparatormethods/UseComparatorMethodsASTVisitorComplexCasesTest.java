package eu.jsparrow.core.visitor.impl.comparatormethods;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.core.visitor.impl.UsesJDTUnitFixture;

public class UseComparatorMethodsASTVisitorComplexCasesTest extends UsesJDTUnitFixture {
	
	@BeforeEach
	public void setUp() throws Exception {
		setDefaultVisitor(new UseComparatorMethodsASTVisitor());
		defaultFixture.addImport(java.util.Comparator.class.getName());
	}

	@AfterEach
	public void tearDown() throws Exception {
		fixtureProject.clear();
	}
	
	/**
	 * No valid transformation by UseComparatorMethodsRulesTest
	 * @throws Exception
	 */
	@Test
	public void visit_Comparator4InnerClass_shouldTransform() throws Exception {
		String original = "" +
				"class InnerClass {\n" +
				"	String getString(){\n" +
				"		return \"\";\n" +
				"	}\n" +
				"}\n" +
				"void test() {\n" +
				"	Comparator<InnerClass> comparator = (lhs, rhs) -> lhs.getString().compareTo(rhs.getString());\n" +
				"}";

		String expected = "" +
				"class InnerClass {\n" +
				"	String getString(){\n" +
				"		return \"\";\n" +
				"	}\n" +
				"}\n" +
				"void test() {\n" +
				"	Comparator<InnerClass> comparator=Comparator.comparing(fixturepackage.TestCU.InnerClass::getString);\n"
				+
				"}";

		assertChange(original, expected);
	}

}
