package eu.jsparrow.core.visitor.security.random;

import java.util.Random;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.core.visitor.impl.UsesJDTUnitFixture;

public class UseSecureRandomASTVisitorTest extends UsesJDTUnitFixture {

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
				"		byte bytes[] = new byte[20];\n" +
				"		random.nextBytes(bytes);\n" +
				"	}";

		String expected = "" +
				"	void test() {\n" +
				"		SecureRandom random = new SecureRandom();\n" +
				"		byte bytes[] = new byte[20];\n" +
				"		random.nextBytes(bytes);" +
				"	}";

		assertChange(original, expected);
	}
	
	
	@Test
	public void visit_ImportNotPossible_shouldTransform() throws Exception {

		String original = "" +
				"	class SecureRandom {}\n" + 
				"	void test() {\n" + 
				"		Random random = new Random();\n" + 
				"		byte bytes[] = new byte[20];\n" + 
				"		random.nextBytes(bytes);\n" + 
				"	}";

		String expected = "" +
				"	class SecureRandom {}\n" + 
				"	void test() {\n" + 
				"		java.security.SecureRandom random = new java.security.SecureRandom();\n" + 
				"		byte bytes[] = new byte[20];\n" + 
				"		random.nextBytes(bytes);\n" + 
				"	}";

		assertChange(original, expected);
	}
	
	


}
