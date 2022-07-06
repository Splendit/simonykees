package eu.jsparrow.core.visitor.impl;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.common.UsesJDTUnitFixture;

class ReplaceLoggingWithForeignClassASTVisitorTest extends UsesJDTUnitFixture {

	@BeforeEach
	public void setupTest() {
		setDefaultVisitor(new ReplaceLoggingWithForeignClassASTVisitor());
	}

	@AfterEach
	void tearDown() throws Exception {
		fixtureProject.clear();
	}

	/**
	 * This test is expected to fail as soon as
	 * ReplaceLoggingWithForeignClassASTVisitor has been implemented.
	 */
	@Test
	void visit_replaceForeignClassLiteral_shouldTransform() throws Exception {
		defaultFixture.addImport(org.slf4j.Logger.class.getName());
		defaultFixture.addImport(org.slf4j.LoggerFactory.class.getName());
		String original = ""
				+ "	static class Employee {\n"
				+ "		static final Logger logger = LoggerFactory.getLogger(Object.class);\n"
				+ "	}";

		assertNoChange(original);
	}
}
