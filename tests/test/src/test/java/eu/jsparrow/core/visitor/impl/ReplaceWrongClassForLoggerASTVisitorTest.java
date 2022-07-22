package eu.jsparrow.core.visitor.impl;

import java.util.stream.Stream;

import org.eclipse.jdt.core.dom.TypeLiteral;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;

import eu.jsparrow.common.UsesJDTUnitFixture;

class ReplaceWrongClassForLoggerASTVisitorTest extends UsesJDTUnitFixture {

	@BeforeEach
	public void setupTest() {
		setDefaultVisitor(new ReplaceWrongClassForLoggerASTVisitor());
	}

	@AfterEach
	public void tearDown() throws Exception {
		fixtureProject.clear();
	}

	public static Stream<Arguments> visit_GetNameOfNotCorrectClass_arguments() throws Exception {
		return Stream.of(
				// Arguments.of("java.util.logging.Logger", "getLog", false),
				// Arguments.of("java.util.logging.Logger", "getLogger", false),
				Arguments.of("java.util.logging.Logger", "java.util.logging.Logger.getLogger", false),
				Arguments.of("org.slf4j.Logger", "org.slf4j.LoggerFactory.getLogger", true),
				Arguments.of("org.apache.log4j.Logger", "org.apache.log4j.LogManager.getLogger", false),
				Arguments.of("org.apache.logging.log4j.Logger", "org.apache.logging.log4j.LogManager.getLogger", true));
	}

	@Disabled
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

		if (shouldTransform) {
			String expected = String.format(""
					+ "	static class Employee {\n"
					+ "		static final %s logger = %s(Employee.class.getName());\n"
					+ "	}", logger, logggerFactory);
			assertChange(original, expected);
		} else {
			assertNoChange(original);
		}
	}

	public static Stream<Arguments> visit_NotCorrectClassLiteral_arguments() throws Exception {
		return Stream.of(
				// Arguments.of("java.util.logging.Logger", "getLog", false),
				// Arguments.of("java.util.logging.Logger", "getLogger", false),
				// Arguments.of("java.util.logging.Logger",
				// "java.util.logging.Logger.getLogger", false),
				Arguments.of("org.slf4j.Logger", "org.slf4j.LoggerFactory.getLogger", true),
				Arguments.of("org.apache.log4j.Logger", "org.apache.log4j.LogManager.getLogger", false),
				Arguments.of("org.apache.logging.log4j.Logger", "org.apache.logging.log4j.LogManager.getLogger", true));
	}

	@Disabled
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

		if (shouldTransform) {
			String expected = String.format(""
					+ "	static class Employee {\n"
					+ "		static final %s logger = %s(Employee.class);\n"
					+ "	}", logger, logggerFactoryMethod);
			assertChange(original, expected);
		} else {
			assertNoChange(original);
		}
	}

	private void addSLFJLoggerDependencyAndImports() throws Exception {
		addDependency("org.slf4j", "slf4j-api", "1.7.25");
		defaultFixture.addImport(org.slf4j.Logger.class.getName());
		defaultFixture.addImport(org.slf4j.LoggerFactory.class.getName());
	}

	private static String declareSLFJLoggerField(String initializationArgument) {
		return String.format("" +
				"private static final Logger logger = LoggerFactory.getLogger(%s);", initializationArgument);
	}

	@Test
	void visit_InitializeSLFJLoggerWithCorrectClass_shouldNotTransform() throws Exception {
		addSLFJLoggerDependencyAndImports();
		String original = "" +
				"	static class Employee {\n" +
				"		" + declareSLFJLoggerField("Employee.class") +
				"	}";
		assertNoChange(original);
	}

	@Test
	void visit_InitializeSLFJLoggerWithErasureClass_shouldNotTransform() throws Exception {
		ReplaceWrongClassForLoggerASTVisitor visitor = Mockito.spy(new ReplaceWrongClassForLoggerASTVisitor());
		setDefaultVisitor(visitor);
		addSLFJLoggerDependencyAndImports();
		String original = "" +
				"	static class Employee<T>{\n" +
				"		" + declareSLFJLoggerField("Employee.class") +
				"	}";
		assertNoChange(original);
		Mockito.verify(visitor, Mockito.times(1))
			.visit(Mockito.any(TypeLiteral.class));
		Mockito.verify(visitor, Mockito.never())
			.addMarkerEvent(Mockito.any(TypeLiteral.class));
	}

	public static Stream<Arguments> initializeSLFJLoggerWithForeignClass_Arguments() throws Exception {
		return Stream.of(
				Arguments.of("Object.class", "Employee.class"),
				Arguments.of("Object.class.getName()", "Employee.class.getName()"),
				Arguments.of("Employee[].class", "Employee.class"));
	}

	@ParameterizedTest
	@MethodSource(value = "initializeSLFJLoggerWithForeignClass_Arguments")
	void visit_InitializeSLFJLoggerWithForeignClass_shouldTransform(String originalArgument, String expectedArgument)
			throws Exception {
		addSLFJLoggerDependencyAndImports();
		String original = "" +
				"	static class Employee {\n" +
				"		" + declareSLFJLoggerField(originalArgument) +
				"	}";
		String expected = "" +
				"	static class Employee {\n" +
				"		" + declareSLFJLoggerField(expectedArgument) +
				"	}";
		assertChange(original, expected);
	}

	@Disabled
	@Test
	void visit_InitializeSLFJLoggerWithForeignClassGetName_shouldTransform() throws Exception {
		addSLFJLoggerDependencyAndImports();
		String original = "" +
				"	static class Employee {\n" +
				"		" + declareSLFJLoggerField("Object.class.getName()") +
				"	}";
		String expected = "" +
				"	static class Employee {\n" +
				"		" + declareSLFJLoggerField("Employee.class.getName()") +
				"	}";
		assertChange(original, expected);
	}

	@Disabled
	@Test
	void visit_InitializeSLFJLoggerWithArrayOfClass_shouldTransform() throws Exception {
		addSLFJLoggerDependencyAndImports();
		String original = "" +
				"	static class Employee {\n" +
				"		" + declareSLFJLoggerField("Employee[].class") +
				"	}";
		String expected = "" +
				"	static class Employee {\n" +
				"		" + declareSLFJLoggerField("Employee.class") +
				"	}";
		assertChange(original, expected);
	}

	@Test
	void visit_NotSupportedGetLoggerMethod_shouldNotTransform() throws Exception {
		addSLFJLoggerDependencyAndImports();
		String original = "" +
				"	static class Employee {\n" +
				"		static final Logger LOGGER = getLogger(Object.class);\n" +
				"	}\n" +
				"\n" +
				"	static Logger getLogger(Class<?> clazz) {\n" +
				"		return LoggerFactory.getLogger(clazz);\n" +
				"	}";

		assertNoChange(original);
	}

	@Test
	void visit_ApacheLoggingLog4jGetLoggerWithForeignClass_shouldTransform() throws Exception {
		addDependency("org.apache.logging.log4j", "log4j-api", "2.7");
		defaultFixture.addImport("org.apache.logging.log4j.LogManager");
		defaultFixture.addImport("org.apache.logging.log4j.Logger");

		String original = "" +
				"	static class Employee {\n"
				+ "		static final Logger LOGGER = LogManager.getLogger(Object.class);\n"
				+ "	}";
		String expected = "" +
				"	static class Employee {\n"
				+ "		static final Logger LOGGER = LogManager.getLogger(Employee.class);\n"
				+ "	}";
		assertChange(original, expected);
	}

	@Test
	void visit_TypeLiteralAsArgumentForUseClass_shouldNotTransform() throws Exception {
		String original = "" +
				"	static class Employee {\n"
				+ "		void callUseClass() {\n"
				+ "			useClass(Object.class);\n"
				+ "		}\n"
				+ "		void useClass(Class<?> clazz) {\n"
				+ "\n"
				+ "		}\n"
				+ "	}";
		assertNoChange(original);
	}

	@Test
	void visit_CallInfoOnNotSupportedGetLogger_shouldNotTransform() throws Exception {
		addSLFJLoggerDependencyAndImports();
		String original = "" +
				"	void callInfoOnGetLogger() {\n"
				+ "		getLogger(Object.class).info(\"info\");\n"
				+ "	}\n"
				+ "\n"
				+ "	static Logger getLogger(Class<?> clazz) {\n"
				+ "		return LoggerFactory.getLogger(clazz);\n"
				+ "	}";
		assertNoChange(original);
	}

	@Test
	void visit_CallInfoOnApacheLog4jLogManagerGetLogger_shouldTransform() throws Exception {
		addDependency("log4j", "log4j", "1.2.17");
		defaultFixture.addImport("org.apache.log4j.LogManager");

		String original = "" +
				"	static class Employee {\n"
				+ "		void callInfoOnGetLogger(String message) {\n"
				+ "			LogManager.getLogger(Object.class).info(message);\n"
				+ "		}\n"
				+ "	}";

		String expected = "" +
				"	static class Employee {\n"
				+ "		void callInfoOnGetLogger(String message) {\n"
				+ "			LogManager.getLogger(Employee.class).info(message);\n"
				+ "		}\n"
				+ "	}";

		assertChange(original, expected);
	}

	@Test
	void visit_CallHashCodeOnApacheLog4jLogManagerGetLogger_shouldNotTransform() throws Exception {
		addDependency("log4j", "log4j", "1.2.17");
		defaultFixture.addImport("org.apache.log4j.LogManager");

		String original = "" +
				"	static class Employee {\n"
				+ "		void callHashCodeOnLogger() {\n"
				+ "			LogManager.getLogger(Object.class).hashCode();\n"
				+ "		}\n"
				+ "	}\n"
				+ "";

		assertNoChange(original);
	}

	@Test
	void visit_CallInfoOnJavaUtilLoggerGetLogger_shouldTransform() throws Exception {
		defaultFixture.addImport(java.util.logging.Logger.class.getName());
		String original = "" +
				"	static class Employee {\n"
				+ "		void callInfoOnGetLogger(String message) {\n"
				+ "			Logger.getLogger(Object.class.getName()).info(message);\n"
				+ "		}\n"
				+ "	}";
		String expected = "" +
				"	static class Employee {\n"
				+ "		void callInfoOnGetLogger(String message) {\n"
				+ "			Logger.getLogger(Employee.class.getName()).info(message);\n"
				+ "		}\n"
				+ "	}";
		assertChange(original, expected);

	}

	@Test
	void visit_CallHashCodeOnJavaUtilLoggerGetLogger_shouldNotTransform() throws Exception {
		defaultFixture.addImport(java.util.logging.Logger.class.getName());
		String original = "" +
				"	static class Employee {\n"
				+ "		void callHGashCodeOfJavaUtilLogger() {\n"
				+ "			Logger.getLogger(Object.class.getName()).hashCode();\n"
				+ "		}\n"
				+ "	}";
		assertNoChange(original);

	}

	@Test
	void visit_CallInfoOnJavaUtilLoggerGetLoggerWithTypeLiteral_shouldNotTransform() throws Exception {
		defaultFixture.addImport(java.util.logging.Logger.class.getName());
		String original = "" +
				"	static class Employee {\n"
				+ "		void callInfoOnGetLogger(String message) {\n"
				+ "			Logger.getLogger(Object.class).info(message);\n"
				+ "		}\n"
				+ "	}";
		assertNoChange(original);
	}

	@Test
	void visit_missingImportOfJavaUtilLogger_shouldTransform() throws Exception {
		String original = "" +
				"	static class Employee {\n"
				+ "		void callInfoOnGetLogger(String message) {\n"
				+ "			Logger.getLogger(Object.class.getName()).info(message);\n"
				+ "		}\n"
				+ "	}";
		assertNoChange(original);
	}

	@ParameterizedTest
	@ValueSource(strings = {
			"Class<?> clazz = Object.class;",
			"Class<?> clazz = Object.class.getName();",
			"Class<?> clazz = Object.class.hashCode();"
	})
	void visit_TypeLiteralNotWithinArgumentsOfMethodInvocation_shouldNotTransform(String codeContainingTypeLiteral)
			throws Exception {
		String original = "" +
				"	static class Employee {\n" +
				"		" + codeContainingTypeLiteral + "\n" +
				"	}";
		assertNoChange(original);
	}

}
