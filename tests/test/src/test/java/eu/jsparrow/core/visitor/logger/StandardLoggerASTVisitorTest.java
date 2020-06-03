package eu.jsparrow.core.visitor.logger;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.core.visitor.impl.UsesJDTUnitFixture;

public class StandardLoggerASTVisitorTest extends UsesJDTUnitFixture {

	@BeforeEach
	public void setUpVisitor() throws Exception {
		Map<String, String> options = new HashMap<>();
		options.put("system-out-print", "info");
		options.put("system-err-print", "error");
		options.put("print-stacktrace", "error");
		options.put("system-out-print-exception", "info");
		options.put("system-err-print-exception", "error");
		options.put("new-logging-statement", "error");
		options.put("attach-exception-object", Boolean.TRUE.toString());

		addDependency("org.slf4j", "slf4j-api", "1.7.25");
		setDefaultVisitor(new StandardLoggerASTVisitor("org.slf4j.Logger", options));

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
				"	System.out.println(\"Sample value\");\n" +
				"}";
		String expected = "" +
				"private static final Logger logger = LoggerFactory.getLogger(" + DEFAULT_TYPE_DECLARATION_NAME
				+ ".class);\n" +
				"\n" +
				"private void sampleMethod() {\n" +
				"	logger.info(\"Sample value\");\n" +
				"}";
		assertChange(original, expected);

	}

}
