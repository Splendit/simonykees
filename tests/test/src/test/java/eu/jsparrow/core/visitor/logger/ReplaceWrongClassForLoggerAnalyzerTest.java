package eu.jsparrow.core.visitor.logger;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.TypeLiteral;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import eu.jsparrow.common.UsesJDTUnitFixture;
import eu.jsparrow.rules.common.exception.UnresolvedBindingException;
import eu.jsparrow.rules.common.util.ASTNodeUtil;

class ReplaceWrongClassForLoggerAnalyzerTest extends UsesJDTUnitFixture {

	private static final String NESTED_LOGGER = "" +
			"	static class Logger {\n" +
			"		final Class<?> clazz;\n" +
			"\n" +
			"		Logger(Class<?> clazz) {\n" +
			"			this.clazz = clazz;\n" +
			"		}\n" +
			"\n" +
			"		static Logger getLogger(Class<?> clazz) {\n" +
			"			return new Logger(clazz);\n" +
			"		}\n" +
			"\n" +
			"		void info(String message) {\n" +
			"\n" +
			"		}\n" +
			"	}";

	@BeforeEach
	public void setupTest() {
		setDefaultVisitor(new ReplaceWrongClassForLoggerASTVisitor());
	}

	@AfterEach
	public void tearDown() throws Exception {
		fixtureProject.clear();
	}

	@ParameterizedTest
	@ValueSource(strings = {
			"Class<?> clazz = Object.class;",
			"int i = Object.class.hashCode();",
			"String className = Object.class.getName();",
			"" +
					"	void callUseClass() {\n" +
					"		useClass(Object.class);\n" +
					"	}\n" +
					"\n" +
					"	void useClass(Class<?> clazz) {\n" +
					"	}",
			"" +
					"	static final Logger logger = Logger.getLogger(Object.class);\n" +
					"\n" +
					NESTED_LOGGER,
			"" +
					"	void callIUnsupportedLogInfo(String message) {\n" +
					"		Logger.getLogger(Object.class).info(message);\n" +
					"	}\n" +
					"\n" +
					NESTED_LOGGER
	})
	void analyze_ForeignTypeLiteralNotUsedForGetLogger_shouldReturnFalse(String fieldDeclaration)
			throws Exception {
		defaultFixture.addTypeDeclarationFromString(DEFAULT_TYPE_DECLARATION_NAME, fieldDeclaration);
		TypeDeclaration typeDeclaration = defaultFixture.getTypeDeclaration();
		TypeLiteral typeLiteral = VisitorTestUtil.findUniqueNode(typeDeclaration, TypeLiteral.class);
		assertSame(typeDeclaration, ASTNodeUtil.getSpecificAncestor(typeLiteral, AbstractTypeDeclaration.class));
		assertTrue(ForeignTypeLiteral.isForeignTypeLiteral(typeLiteral, typeDeclaration, defaultFixture.getRootNode()));
		assertFalse(ReplaceWrongClassForLoggerAnalyzer.isClassLiteralToReplace(typeLiteral, typeDeclaration,
				defaultFixture.getRootNode()));
	}

	@Test
	void analyze_UnresolvedGetLoggerWithForeignLiteral_shouldThrowException()
			throws Exception {
		String fieldDeclaration = "static final Logger logger = getLogger(Object.class);";
		defaultFixture.addTypeDeclarationFromString(DEFAULT_TYPE_DECLARATION_NAME, fieldDeclaration);
		TypeDeclaration typeDeclaration = defaultFixture.getTypeDeclaration();
		TypeLiteral typeLiteral = VisitorTestUtil.findUniqueNode(typeDeclaration, TypeLiteral.class);
		assertSame(typeDeclaration, ASTNodeUtil.getSpecificAncestor(typeLiteral, AbstractTypeDeclaration.class));
		assertTrue(ForeignTypeLiteral.isForeignTypeLiteral(typeLiteral, typeDeclaration, defaultFixture.getRootNode()));
		assertThrows(UnresolvedBindingException.class,
				() -> ReplaceWrongClassForLoggerAnalyzer.isClassLiteralToReplace(typeLiteral, typeDeclaration,
						defaultFixture.getRootNode()));
	}

	private TypeDeclaration createDefaultTypeDeclarationUsingSLF4JLogger(String argumentForLoggerFactory)
			throws Exception {
		addDependency("org.slf4j", "slf4j-api", "1.7.25");
		defaultFixture.addImport(org.slf4j.Logger.class.getName());
		defaultFixture.addImport(org.slf4j.LoggerFactory.class.getName());
		String loggerDeclaration = String.format("" +
				"	static final Logger LOGGER = LoggerFactory.getLogger(%s);",
				argumentForLoggerFactory);
		defaultFixture.addTypeDeclarationFromString(DEFAULT_TYPE_DECLARATION_NAME, loggerDeclaration);
		return defaultFixture.getTypeDeclaration();
	}

	@ParameterizedTest
	@ValueSource(strings = {
			"Object.class",
			"Object.class.getName()"
	})
	void analyze_UsingOrgSLFJLogger_shouldReturnTrue(String argumentForLoggerFactory) throws Exception {
		TypeDeclaration typeDeclaration = createDefaultTypeDeclarationUsingSLF4JLogger(argumentForLoggerFactory);
		TypeLiteral typeLiteral = VisitorTestUtil.findUniqueNode(typeDeclaration, TypeLiteral.class);
		assertTrue(ReplaceWrongClassForLoggerAnalyzer.isClassLiteralToReplace(typeLiteral, typeDeclaration,
				defaultFixture.getRootNode()));
	}

	@ParameterizedTest
	@ValueSource(strings = {
			DEFAULT_TYPE_DECLARATION_NAME + ".class",
			DEFAULT_TYPE_DECLARATION_NAME + ".class.getName()"
	})
	void analyze_UsingOrgSLFJLogger_shouldReturnFalse(String argumentForLoggerFactory) throws Exception {
		TypeDeclaration typeDeclaration = createDefaultTypeDeclarationUsingSLF4JLogger(argumentForLoggerFactory);
		TypeLiteral typeLiteral = VisitorTestUtil.findUniqueNode(typeDeclaration, TypeLiteral.class);
		assertFalse(ReplaceWrongClassForLoggerAnalyzer.isClassLiteralToReplace(typeLiteral, typeDeclaration,
				defaultFixture.getRootNode()));
	}

	private TypeDeclaration createDefaultTypeUsingApacheLoggingLog4J2Logger(String argumentForLoggerFactory)
			throws Exception {
		addDependency("org.apache.logging.log4j", "log4j-api", "2.7");
		defaultFixture.addImport("org.apache.logging.log4j.LogManager");
		defaultFixture.addImport("org.apache.logging.log4j.Logger");
		String loggerDeclaration = String.format("" +
				"	static final Logger LOGGER = LogManager.getLogger(%s);",
				argumentForLoggerFactory);
		defaultFixture.addTypeDeclarationFromString(DEFAULT_TYPE_DECLARATION_NAME, loggerDeclaration);
		return defaultFixture.getTypeDeclaration();
	}

	@ParameterizedTest
	@ValueSource(strings = {
			"Object.class",
			"Object.class.getName()"
	})
	void analyze_ApacheLoggingLog4jGetLoggerWithForeignClass_shouldReturnTrue(String argumentForLoggerFactory)
			throws Exception {
		TypeDeclaration typeDeclaration = createDefaultTypeUsingApacheLoggingLog4J2Logger("Object.class");
		TypeLiteral typeLiteral = VisitorTestUtil.findUniqueNode(typeDeclaration, TypeLiteral.class);
		assertTrue(ReplaceWrongClassForLoggerAnalyzer.isClassLiteralToReplace(typeLiteral, typeDeclaration,
				defaultFixture.getRootNode()));
	}

	@ParameterizedTest
	@ValueSource(strings = {
			DEFAULT_TYPE_DECLARATION_NAME + ".class",
			DEFAULT_TYPE_DECLARATION_NAME + ".class.getName()"
	})
	void analyze_ApacheLoggingLog4jGetLoggerWithForeignClass_shouldReturnFalse(String argumentForLoggerFactory)
			throws Exception {
		TypeDeclaration typeDeclaration = createDefaultTypeUsingApacheLoggingLog4J2Logger(argumentForLoggerFactory);
		TypeLiteral typeLiteral = VisitorTestUtil.findUniqueNode(typeDeclaration, TypeLiteral.class);
		assertFalse(ReplaceWrongClassForLoggerAnalyzer.isClassLiteralToReplace(typeLiteral, typeDeclaration,
				defaultFixture.getRootNode()));
	}

	@Test
	void analyze_JavaUtilLoggingLoggerGetLoggerWithTypeLiteral_shouldThrowException() throws Exception {
		defaultFixture.addImport(java.util.logging.Logger.class.getName());
		defaultFixture.addTypeDeclarationFromString(DEFAULT_TYPE_DECLARATION_NAME,
				"" +
						"	void callInfoOnGetLogger(String message) {\n" +
						"		Logger.getLogger(Object.class).info(message);\n" +
						"	}");
		TypeDeclaration typeDeclaration = defaultFixture.getTypeDeclaration();
		TypeLiteral typeLiteral = VisitorTestUtil.findUniqueNode(typeDeclaration, TypeLiteral.class);
		assertThrows(UnresolvedBindingException.class,
				() -> ReplaceWrongClassForLoggerAnalyzer.isClassLiteralToReplace(typeLiteral, typeDeclaration,
						defaultFixture.getRootNode()));
	}

	@Test
	void analyze_CallHashCodeOnJavaUtilLoggerGetLogger_shouldReturnFalse() throws Exception {
		defaultFixture.addImport(java.util.logging.Logger.class.getName());
		defaultFixture.addTypeDeclarationFromString(DEFAULT_TYPE_DECLARATION_NAME,
				"" +
						"	void callHGashCodeOnGetLogger() {\n" +
						"		Logger.getLogger(Object.class.getName()).hashCode();\n" +
						"	}");
		TypeDeclaration typeDeclaration = defaultFixture.getTypeDeclaration();
		TypeLiteral typeLiteral = VisitorTestUtil.findUniqueNode(typeDeclaration, TypeLiteral.class);
		AbstractTypeDeclaration enclosingTypeDeclaration = ASTNodeUtil.getSpecificAncestor(typeLiteral,
				AbstractTypeDeclaration.class);
		assertFalse(ReplaceWrongClassForLoggerAnalyzer.isClassLiteralToReplace(typeLiteral, enclosingTypeDeclaration,
				defaultFixture.getRootNode()));
	}

	@Test
	void analyze_ObjectClassGetNameNotFirstArgument_shouldReturnFalse() throws Exception {
		defaultFixture.addImport(java.util.logging.Logger.class.getName());
		defaultFixture.addTypeDeclarationFromString(DEFAULT_TYPE_DECLARATION_NAME,
				"" +
						"		void callInfoOnGetLogger(String className, String msg) {\n" +
						"			Logger.getLogger(className, Object.class.getName()).info(msg);\n" +
						"		}");
		TypeDeclaration typeDeclaration = defaultFixture.getTypeDeclaration();
		TypeLiteral typeLiteral = VisitorTestUtil.findUniqueNode(typeDeclaration, TypeLiteral.class);
		AbstractTypeDeclaration enclosingTypeDeclaration = ASTNodeUtil.getSpecificAncestor(typeLiteral,
				AbstractTypeDeclaration.class);
		assertFalse(ReplaceWrongClassForLoggerAnalyzer.isClassLiteralToReplace(typeLiteral, enclosingTypeDeclaration,
				defaultFixture.getRootNode()));
	}

	@Test
	void analyze_CallInfoOnJavaUtilLoggerGetLogger_shouldReturnTrue() throws Exception {
		defaultFixture.addImport(java.util.logging.Logger.class.getName());
		defaultFixture.addTypeDeclarationFromString(DEFAULT_TYPE_DECLARATION_NAME,
				"" +
						"	void callInfoOnGetLogger(String message) {\n" +
						"		Logger.getLogger(Object.class.getName()).info(message);\n" +
						"	}");
		TypeDeclaration typeDeclaration = defaultFixture.getTypeDeclaration();
		TypeLiteral typeLiteral = VisitorTestUtil.findUniqueNode(typeDeclaration, TypeLiteral.class);
		AbstractTypeDeclaration enclosingTypeDeclaration = ASTNodeUtil.getSpecificAncestor(typeLiteral,
				AbstractTypeDeclaration.class);
		assertTrue(ReplaceWrongClassForLoggerAnalyzer.isClassLiteralToReplace(typeLiteral, enclosingTypeDeclaration,
				defaultFixture.getRootNode()));
	}

	@Test
	void analyze_CallHashCodeOnApacheLog4jLogManagerGetLogger_shouldReturnFalse() throws Exception {
		addDependency("log4j", "log4j", "1.2.17");
		defaultFixture.addImport("org.apache.log4j.LogManager");
		defaultFixture.addTypeDeclarationFromString(DEFAULT_TYPE_DECLARATION_NAME,
				"" +
						"	void callHashCodeOnGetLogger() {\n" +
						"		LogManager.getLogger(Object.class).hashCode();\n" +
						"	}");
		TypeDeclaration typeDeclaration = defaultFixture.getTypeDeclaration();
		TypeLiteral typeLiteral = VisitorTestUtil.findUniqueNode(typeDeclaration, TypeLiteral.class);
		AbstractTypeDeclaration enclosingTypeDeclaration = ASTNodeUtil.getSpecificAncestor(typeLiteral,
				AbstractTypeDeclaration.class);
		assertFalse(ReplaceWrongClassForLoggerAnalyzer.isClassLiteralToReplace(typeLiteral, enclosingTypeDeclaration,
				defaultFixture.getRootNode()));
	}

	@Test
	void visit_CallInfoOnApacheLog4jLogManagerGetLogger_shouldReturnTrue() throws Exception {
		addDependency("log4j", "log4j", "1.2.17");
		defaultFixture.addImport("org.apache.log4j.LogManager");
		defaultFixture.addTypeDeclarationFromString(DEFAULT_TYPE_DECLARATION_NAME,
				"" +
						"	void callInfoOnGetLogger(String message) {\n" +
						"		LogManager.getLogger(Object.class).info(message);\n" +
						"	}");
		TypeDeclaration typeDeclaration = defaultFixture.getTypeDeclaration();
		TypeLiteral typeLiteral = VisitorTestUtil.findUniqueNode(typeDeclaration, TypeLiteral.class);
		AbstractTypeDeclaration enclosingTypeDeclaration = ASTNodeUtil.getSpecificAncestor(typeLiteral,
				AbstractTypeDeclaration.class);
		assertTrue(ReplaceWrongClassForLoggerAnalyzer.isClassLiteralToReplace(typeLiteral, enclosingTypeDeclaration,
				defaultFixture.getRootNode()));
	}

}
