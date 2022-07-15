package eu.jsparrow.core.visitor.impl;

import java.util.stream.Stream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import eu.jsparrow.common.UsesJDTUnitFixture;

class ReplaceWrongClassForLoggerASTVisitorTest extends UsesJDTUnitFixture {

	@BeforeEach
	public void setupTest() {
		setDefaultVisitor(new ReplaceWrongClassForLoggerASTVisitor());
	}

	@AfterEach
	void tearDown() throws Exception {
		fixtureProject.clear();
	}

	public static Stream<Arguments> visit_GetNameOfNotCorrectClass_arguments() throws Exception {
		return Stream.of(
				Arguments.of("java.util.logging.Logger", "getLog", false),
				Arguments.of("java.util.logging.Logger", "getLogger", false),
				Arguments.of("java.util.logging.Logger", "java.util.logging.Logger.getLogger", true),
				Arguments.of("org.slf4j.Logger", "org.slf4j.LoggerFactory.getLogger", true),
				Arguments.of("org.apache.log4j.Logger", "org.apache.log4j.LogManager.getLogger", true),
				Arguments.of("org.apache.logging.log4j.Logger", "org.apache.logging.log4j.LogManager.getLogger", true));
	}

	@ParameterizedTest
	@MethodSource(value = "visit_GetNameOfNotCorrectClass_arguments")
	void visit_GetNameOfNotCorrectClassLiteral(String logger, String logggerFactory, boolean shouldTransform)
			throws Exception {
		addDependency("org.slf4j", "slf4j-api", "1.7.25");
		addDependency("log4j", "log4j", "1.2.17");
		addDependency("org.apache.logging.log4j", "log4j-api", "2.7");

		String original = String.format(""
				+ "	static class Employee {\n"
				+ "		static final %s logger = %s(Object.class.getName());\n"
				+ "	}", logger, logggerFactory);

		String expected = String.format(""
				+ "	static class Employee {\n"
				+ "		static final %s logger = %s(Employee.class.getName());\n"
				+ "	}", logger, logggerFactory);

		if (shouldTransform) {
			assertChange(original, expected);
		} else {
			assertNoChange(original);
		}

	}

	public static Stream<Arguments> visit_NotCorrectClassLiteral_arguments() throws Exception {
		return Stream.of(
				Arguments.of("java.util.logging.Logger", "getLog", false),
				Arguments.of("java.util.logging.Logger", "getLogger", false),
				Arguments.of("java.util.logging.Logger", "java.util.logging.Logger.getLogger", false),
				Arguments.of("org.slf4j.Logger", "org.slf4j.LoggerFactory.getLogger", true),
				Arguments.of("org.apache.log4j.Logger", "org.apache.log4j.LogManager.getLogger", true),
				Arguments.of("org.apache.logging.log4j.Logger", "org.apache.logging.log4j.LogManager.getLogger", true));
	}

	@ParameterizedTest
	@MethodSource(value = "visit_NotCorrectClassLiteral_arguments")
	void visit_NotCorrectClassLiteral(String logger, String logggerFactoryMethod, boolean shouldTransform)
			throws Exception {
		addDependency("org.slf4j", "slf4j-api", "1.7.25");
		addDependency("log4j", "log4j", "1.2.17");
		addDependency("org.apache.logging.log4j", "log4j-api", "2.7");

		String original = String.format(""
				+ "	static class Employee {\n"
				+ "		static final %s logger = %s(Object.class);\n"
				+ "	}", logger, logggerFactoryMethod);

		String expected = String.format(""
				+ "	static class Employee {\n"
				+ "		static final %s logger = %s(Employee.class);\n"
				+ "	}", logger, logggerFactoryMethod);

		if (shouldTransform) {
			assertChange(original, expected);
		} else {
			assertNoChange(original);
		}
	}
	
	
	@ParameterizedTest
	@ValueSource(strings = {
			"Employee.class", 
			"Employee.class.getName()"
	})
	void visit_CorrectLoggerInitialization_shouldNotTransform(String initializationArgument) throws Exception {
		defaultFixture.addImport(org.slf4j.Logger.class.getName());
		defaultFixture.addImport(org.slf4j.LoggerFactory.class.getName());
		String original = String.format(""
				+ "	static class Employee {\n"
				+ "		static final Logger logger = LoggerFactory.getLogger(%s);\n"
				+ "	}", initializationArgument);
		
		assertNoChange(original);
	}
}
