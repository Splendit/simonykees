package eu.jsparrow.core.visitor.logger;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.common.UsesJDTUnitFixture;

class ReplaceWrongClassForLoggerASTVisitorTest extends UsesJDTUnitFixture {

	@BeforeEach
	public void setupTest() throws Exception {
		setDefaultVisitor(new ReplaceWrongClassForLoggerASTVisitor());
		addDependency("org.slf4j", "slf4j-api", "1.7.25");
		defaultFixture.addImport(org.slf4j.Logger.class.getName());
		defaultFixture.addImport(org.slf4j.LoggerFactory.class.getName());

	}

	@AfterEach
	public void tearDown() throws Exception {
		fixtureProject.clear();
	}

	private static String declareSLFJLoggerField(String initializationArgument) {
		return String.format("" +
				"private static final Logger logger = LoggerFactory.getLogger(%s);", initializationArgument);
	}

	@Test
	void visit_InitializeSLFJLoggerWithCorrectClass_shouldNotTransform() throws Exception {
		String original = "" +
				"	static class Employee {\n" +
				"		" + declareSLFJLoggerField("Employee.class") +
				"	}";
		assertNoChange(original);
	}

	@Test
	void visit_InitializeSLFJLoggerWithForeignClass_shouldTransform() throws Exception {
		String original = "" +
				"	static class Employee {\n" +
				"		" + declareSLFJLoggerField("Object.class") +
				"	}";
		String expected = "" +
				"	static class Employee {\n" +
				"		" + declareSLFJLoggerField("Employee.class") +
				"	}";
		assertChange(original, expected);
	}

	@Test
	void visit_InitializeSLFJLoggerWithInvalidArgument_shouldNotTransform() throws Exception {
		String original = "" +
				"	static class Employee {\n" +
				"		private static final Logger logger = LoggerFactory.getLogger(Object.class, 1);" +
				"	}";
		assertNoChange(original);
	}
}
