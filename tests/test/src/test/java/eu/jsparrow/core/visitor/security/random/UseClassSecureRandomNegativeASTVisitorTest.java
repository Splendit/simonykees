package eu.jsparrow.core.visitor.security.random;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.core.visitor.impl.UsesJDTUnitFixture;

public class UseClassSecureRandomNegativeASTVisitorTest extends UsesJDTUnitFixture {

	@BeforeEach
	public void setUp() throws Exception {
		setDefaultVisitor(new UseClassSecureRandomASTVisitor());
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

}
