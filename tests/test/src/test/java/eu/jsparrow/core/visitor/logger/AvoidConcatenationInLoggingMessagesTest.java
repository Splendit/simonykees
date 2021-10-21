package eu.jsparrow.core.visitor.logger;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.common.UsesJDTUnitFixture;
import eu.jsparrow.core.visitor.impl.AvoidConcatenationInLoggingStatementsASTVisitor;

public class AvoidConcatenationInLoggingMessagesTest extends UsesJDTUnitFixture {

	@BeforeEach
	public void setUpVisitor() throws Exception {
		addDependency("org.slf4j", "slf4j-api", "1.7.25");
		setDefaultVisitor(new AvoidConcatenationInLoggingStatementsASTVisitor());

	}

	@Test
	public void visit_existingLogger_shouldTransform() throws Exception {

		defaultFixture.addImport("org.slf4j.Logger");
		defaultFixture.addImport("org.slf4j.LoggerFactory");
		defaultFixture.addImport("java.math.BigDecimal");
		defaultFixture.addImport("java.time.Instant");

		String original = "" +
				"private static final Logger logger = LoggerFactory.getLogger(" + DEFAULT_TYPE_DECLARATION_NAME
				+ ".class);\n" +
				"\n" +
				"private void sampleMethod(String s, int i, BigDecimal bd, char c) {\n" +
				"	logger.info(\"s: \" + s + \" i: \" + i + \" bd: \" + bd + \" c: \" + c);\n" +
				"}";
		String expected = "" +
				"private static final Logger logger = LoggerFactory.getLogger(" + DEFAULT_TYPE_DECLARATION_NAME
				+ ".class);\n" +
				"\n" +
				"private void sampleMethod(String s, int i, BigDecimal bd, char c) {\n" +
				"	logger.info(\"s: {} i: {} bd: {} c: {}\", s, i, bd, c);\n" +
				"}";
		assertChange(original, expected);

	}

}
