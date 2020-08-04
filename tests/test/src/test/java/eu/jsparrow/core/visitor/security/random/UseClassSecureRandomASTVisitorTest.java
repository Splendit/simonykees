package eu.jsparrow.core.visitor.security.random;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.core.visitor.impl.UsesJDTUnitFixture;

public class UseClassSecureRandomASTVisitorTest extends UsesJDTUnitFixture {

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
	public void visit_NewRandomInvocation_shouldTransform() throws Exception {

		String original = "" +
				"	void test() {\n" +
				"		Random random = new Random();\n" +
				"	}";

		String expected = "" +
				"	void test() {\n" +
				"		Random random = new SecureRandom();\n" +
				"	}";

		assertChange(original, expected);
	}
	
	@Test
	public void visit_NewRandomInvocationWithQualifiedName_shouldTransform() throws Exception {

		String original = "" +
				"		void test() {\n" + 
				"			java.util.Random random = new java.util.Random();\n" + 
				"		}";

		String expected = "" +
				"		void test() {\n" + 
				"			java.util.Random random = new SecureRandom();\n" + 
				"		}";

		assertChange(original, expected);
	}
	
	@Test
	public void visit_ParenthesizedNewRandomInvocation_shouldTransform() throws Exception {

		String original = "" +
				"	void test() {\n" +
				"		Random random = ((new Random()));\n" +
				"	}";

		String expected = "" +
				"	void test() {\n" +
				"		Random random = new SecureRandom();\n" +
				"	}";

		assertChange(original, expected);
	}

	@Test
	public void visit_ImportNotPossible_shouldTransform() throws Exception {

		String original = "" +
				"	class SecureRandom {}\n" +
				"	void test() {\n" +
				"		Random random = new Random();\n" +
				"	}";

		String expected = "" +
				"	class SecureRandom {}\n" +
				"	void test() {\n" +
				"		Random random = new java.security.SecureRandom();\n" +
				"	}";

		assertChange(original, expected);
	}

	@Test
	public void visit_NewRandomWithSeed_shouldTransform() throws Exception {
		String original = "" +
				"	void test() {\n" +
				"		Random random = new Random(0L);\n" +
				"	}";
		String expected = "" +
				"	void test() {\n" +
				"		Random random = new SecureRandom();\n" +
				"		random.setSeed(0L);\n" +
				"	}";
		assertChange(original, expected);
	}
	
	@Test
	public void visit_ParenthesizedNewRandomWithSeed_shouldTransform() throws Exception {
		String original = "" +
				"	void test() {\n" +
				"		Random random = ((new Random(0L)));\n" +
				"	}";
		String expected = "" +
				"	void test() {\n" +
				"		Random random = new SecureRandom();\n" +
				"		random.setSeed(0L);\n" +
				"	}";
		assertChange(original, expected);
	}
	
	@Test
	public void visit_NewRandomWithSeedAssignedToField_shouldTransform() throws Exception {
		String original = "" +
				"		class RandomWrapper {\n" + 
				"			Random random;\n" + 
				"		}\n" + 
				"		RandomWrapper wrapper = new RandomWrapper();\n" + 
				"		void test() {\n" + 
				"			this.wrapper.random = new Random(0L);\n" + 
				"		}";
		String expected = "" +
				"		class RandomWrapper {\n" + 
				"			Random random;\n" + 
				"		}\n" + 
				"		RandomWrapper wrapper = new RandomWrapper();\n" + 
				"		void test() {\n" + 
				"			this.wrapper.random = new SecureRandom();\n" + 
				"			this.wrapper.random.setSeed(0L);\n" + 
				"		}";
		assertChange(original, expected);
	}
}
