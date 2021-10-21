package eu.jsparrow.core.visitor.security.random;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.common.UsesJDTUnitFixture;

public class UseSecureRandomNegativeASTVisitorTest extends UsesJDTUnitFixture {

	@BeforeEach
	public void setUp() throws Exception {
		setDefaultVisitor(new UseSecureRandomASTVisitor());
		defaultFixture.addImport(java.util.Random.class.getName());
	}

	@AfterEach
	public void tearDown() throws Exception {
		fixtureProject.clear();
	}

	@Test
	public void visit_NewRandomAsMethodArgument_shouldNotTransform() throws Exception {

		String original = "" +
				"		void useRandom(Random random) {}\n" +
				"		void test() {\n" +
				"			useRandom(new Random());\n" +
				"		}";

		assertNoChange(original);
	}

	@Test
	public void visit_NewRandomAsParenthesizedMethodArgument_shouldNotTransform() throws Exception {

		String original = "" +
				"		void useRandom(Random random) {}\n" +
				"		void test() {\n" +
				"			useRandom((new Random()));\n" +
				"		}";

		assertNoChange(original);
	}

	@Test
	public void visit_NewRandomAsConstructorArgument_shouldNotTransform() throws Exception {

		String original = "" +
				"		class RandomWrapper{\n" +
				"			RandomWrapper(Random random) {}\n" +
				"		}\n" +
				"		void test() {\n" +
				"			new RandomWrapper(new Random());\n" +
				"		}";

		assertNoChange(original);
	}

	@Test
	public void visit_NewRandomWithSeedAssignedToField_shouldNotTransform() throws Exception {

		String original = "" +
				"		Random random = new Random(0L);";

		assertNoChange(original);
	}
	

	@Test
	public void visit_NewRandomWithSeedAssignmentAsArgument_shouldNotTransform() throws Exception {

		String original = "" +
				"		void useRandom(Random random) {\n" + 
				"		}\n" + 
				"		void test() {\n" + 
				"			Random random;\n" + 
				"			useRandom(random = new Random(0L));\n" + 
				"		}";

		assertNoChange(original);
	}
	
	@Test
	public void visit_NewRandomWithSeedAssignmentInThenClause_shouldNotTransform() throws Exception {

		String original = "" +
				"		void test() {\n" + 
				"			Random random;\n" + 
				"			if (true)\n" + 
				"				random = new Random(0L);\n" + 
				"		}";

		assertNoChange(original);
	}
	
	@Test
	public void visit_NewRandomWithSeedAsExpressionStatement_shouldNotTransform() throws Exception {

		String original = "" +
				"		void test() {\n" + 
				"			new Random(0L);\n" + 
				"		}";

		assertNoChange(original);
	}

}
