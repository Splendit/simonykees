package eu.jsparrow.core.visitor.impl.ternary;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.common.UsesJDTUnitFixture;

@SuppressWarnings({ "nls" })
public class UseTernaryOperatorASTVisitorNegativesTest extends UsesJDTUnitFixture {

	@BeforeEach
	public void setUp() {
		setDefaultVisitor(new UseTernaryOperatorASTVisitor());
	}

	@AfterEach
	public void tearDown() throws Exception {
		fixtureProject.clear();
	}

	@Test
	void visit_AssigningDifferentVariables_shouldNotTransform() throws Exception {
		String original = ""
				+ "	void test() {\n"
				+ "		boolean condition = true;\n"
				+ "		int x1;\n"
				+ "		int x2;\n"
				+ "		if (condition) {\n"
				+ "			x1 = 1;\n"
				+ "		} else {\n"
				+ "			x2 = 0;\n"
				+ "		}\n"
				+ "	}";
		assertNoChange(original);
	}
}
