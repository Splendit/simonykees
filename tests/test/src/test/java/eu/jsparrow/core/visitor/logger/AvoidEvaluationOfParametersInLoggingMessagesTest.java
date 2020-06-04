package eu.jsparrow.core.visitor.logger;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.core.visitor.impl.AvoidEvaluationOfParametersInLoggingMessagesASTVisitor;
import eu.jsparrow.core.visitor.impl.UsesJDTUnitFixture;

public class AvoidEvaluationOfParametersInLoggingMessagesTest extends UsesJDTUnitFixture {

	@BeforeEach
	public void setUpVisitor() throws Exception {
		addDependency("org.slf4j", "slf4j-api", "1.7.25");
		setDefaultVisitor(new AvoidEvaluationOfParametersInLoggingMessagesASTVisitor());

	}

	@Test
	public void visit_existingLogger_shouldTransform() throws Exception {

		defaultFixture.addImport("org.slf4j.Logger");
		defaultFixture.addImport("org.slf4j.LoggerFactory");

		String original = "" +
				"private static final Logger logger = LoggerFactory.getLogger(" + DEFAULT_TYPE_DECLARATION_NAME
				+ ".class);\n" +
				"\n" +
				"private void sampleMethod() {\n" +
				"	logger.info(\"A \" + 1 + \" B \" + 2);\n" +
				"}";
		String expected = "" +
				"private static final Logger logger = LoggerFactory.getLogger(" + DEFAULT_TYPE_DECLARATION_NAME
				+ ".class);\n" +
				"\n" +
				"private void sampleMethod() {\n" +
				"	logger.info(\"A {} B {} C {} D {}\", 1, 2, 3, 4);\n" +
				"}";
		assertChange(original, expected);

	}

}
