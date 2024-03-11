package eu.jsparrow.core.visitor.unused.type;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Set;

import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import eu.jsparrow.common.UsesJDTUnitFixture;

class ReferencesInTestAnalyzerVisitorTest extends UsesJDTUnitFixture {

	@BeforeEach
	void setUp() throws Exception {
		addDependency("junit", "junit", "4.13");
		addDependency("org.junit.jupiter", "junit-jupiter-api", "5.4.0");
		addDependency("org.junit.jupiter", "junit-jupiter-params", "5.7.0");
	}
	
	@AfterEach
	void tearDown() throws Exception {
		fixtureProject.clear();
	}
	
	@ParameterizedTest
	@ValueSource(strings = {
			"@Test void testMethod() {\n" +
			"    String value = null;\n" +
			"}" +
			"void emptyMethod(){}" +
			"",
			
			"@Test void testMethod() {\n" +
			"    String value = null;\n" +
			"    String value2 = null;\n" +
			"}"+
			"",
			
			"@Test void testMethod() {\n" +
			"    Runnable r = () -> {String value = null;};\n" +
			"}" +
			"",
			
			"@Test void testMethod() {\n" +
			"    String value = null;" +
			"}" +
			"@Test void intTest() {int i = 0;}" +
			"",
	})
	void referenceInTestCase_shouldFindOneTest(String original) throws Exception {
		ReferencesInTestAnalyzerVisitor visitor = new ReferencesInTestAnalyzerVisitor("java.lang.String");
		defaultFixture.addImport("org.junit.jupiter.api.Test");
		defaultFixture.addImport("java.lang.String");
		defaultFixture.addTypeDeclarationFromString(DEFAULT_TYPE_DECLARATION_NAME, original);
		defaultFixture.accept(visitor);
		Set<MethodDeclaration> tests = visitor.getTestMethodsHavingUnusedTypeReferences();
		Set<ImportDeclaration> imports = visitor.getUnusedTypeImports();
		Set<AbstractTypeDeclaration> types = visitor.getTypesWithReferencesToUnusedType();
		assertAll(
				() -> assertEquals(1, tests.size()),
				() -> assertEquals(1, imports.size()),
				() -> assertTrue(types.isEmpty()),
				() -> assertFalse(visitor.isMainTopLevelTypeDesignated()));
	}
	
	@ParameterizedTest
	@ValueSource(strings = {
			// Reference in fields of top level class
			"private String value = null;"+
			"@Test void emptyMethod() {\n" +
			"}" +
			"",
			
			// Multiple references in fields of top level class
			"private String value = null;"+
			"private String value2 = null;"+
			"@Test void emptyMethod() {\n" +
			"}" +
			"",
			
			// Reference in normal method of top level class
			"@Test void emptyMethod() {\n" +
			"}" +
			"void setUp() {String value = null;}" +
			"",
			
			// Reference in Setup method of top level class
			"@Test void emptyMethod() {\n" +
			"}" +
			"@org.junit.jupiter.api.BeforeEach void setUp() {String value = null;}" +
			"",
			
			// Reference in top level class AND in its test case
			"private String value = null;"+
			"@Test void testMethod() {\n" +
			"    String value = null;\n" +
			"}" +
			"",
			
			// Reference in top level AND in its nested type
			"private String value = null;"+
			"@org.junit.jupiter.api.Nested class NestedClass {" +
			"	void setUp() {String value = null;}" +
			"	@Test void emptyMethod() {}\n" +
			"}" + 
			""
			
	})
	void referencesInTopLevelType_shouldReturnOneType(String original) throws Exception {
		ReferencesInTestAnalyzerVisitor visitor = new ReferencesInTestAnalyzerVisitor("java.lang.String");
		defaultFixture.addImport("org.junit.jupiter.api.Test");

		defaultFixture.addTypeDeclarationFromString(DEFAULT_TYPE_DECLARATION_NAME, original);
		defaultFixture.accept(visitor);

		Set<MethodDeclaration> tests = visitor.getTestMethodsHavingUnusedTypeReferences();
		Set<ImportDeclaration> imports = visitor.getUnusedTypeImports();
		Set<AbstractTypeDeclaration> types = visitor.getTypesWithReferencesToUnusedType();
		assertAll(
				() -> assertEquals(0, tests.size()),
				() -> assertEquals(1, types.size()),
				() -> assertTrue(visitor.isMainTopLevelTypeDesignated()), 
				() -> assertTrue(imports.isEmpty()));
	}
	
	@ParameterizedTest
	@ValueSource(strings = {
			// Reference in test method of nested class
			"@org.junit.jupiter.api.Nested class NestedClass {" +
			"	private String value = null;"+
			"	@Test void emptyMethod() {}\n" +
			"}" + 
			"",
			
			// Reference in setUp method of nested class
			"@org.junit.jupiter.api.Nested class NestedClass {" +
			"	void setUp() {String value = null;}" +
			"	@Test void emptyMethod() {}\n" +
			"}" + 
			"",
			
			// Multiple nested classes 
			"@org.junit.jupiter.api.Nested class NestedClass {" +
			"	private String value = null;"+
			"	@Test void emptyMethod() {}\n" +
			"}" + 
			"@org.junit.jupiter.api.Nested class SecondNestedClass {" +
			"	private Integer value = null;"+
			"	@Test void emptyMethod() {}\n" +
			"}" + 
			"",
			
	})
	void referencesInNestedTypes_shouldReturnOneType(String original) throws Exception {
		ReferencesInTestAnalyzerVisitor visitor = new ReferencesInTestAnalyzerVisitor("java.lang.String");
		defaultFixture.addImport("org.junit.jupiter.api.Test");

		defaultFixture.addTypeDeclarationFromString(DEFAULT_TYPE_DECLARATION_NAME, original);
		defaultFixture.accept(visitor);

		Set<MethodDeclaration> tests = visitor.getTestMethodsHavingUnusedTypeReferences();
		Set<AbstractTypeDeclaration> types = visitor.getTypesWithReferencesToUnusedType();
		assertEquals(0, tests.size());
		assertEquals(1, types.size());
		assertFalse(visitor.isMainTopLevelTypeDesignated());
	}
	
	@Test
	void staticImport_shouldReturnTwoImports() throws Exception {
		String original = 			
				"@Test void testMethod() {\n" +
				"    Comparator c = CASE_INSENSITIVE_ORDER;" +
				"}";
		
		ReferencesInTestAnalyzerVisitor visitor = new ReferencesInTestAnalyzerVisitor("java.lang.String");
		defaultFixture.addImport("org.junit.jupiter.api.Test");
		defaultFixture.addImport("java.util.Comparator");
		defaultFixture.addImport("java.lang.String", true, true);
		defaultFixture.addImport("java.lang.String.CASE_INSENSITIVE_ORDER", true, false);
		defaultFixture.addTypeDeclarationFromString(DEFAULT_TYPE_DECLARATION_NAME, original);
		defaultFixture.accept(visitor);
		Set<MethodDeclaration> tests = visitor.getTestMethodsHavingUnusedTypeReferences();
		Set<ImportDeclaration> imports = visitor.getUnusedTypeImports();
		Set<AbstractTypeDeclaration> types = visitor.getTypesWithReferencesToUnusedType();
		assertAll(
				() -> assertTrue(tests.isEmpty()),
				() -> assertEquals(2, imports.size()),
				() -> assertTrue(types.isEmpty()),
				() -> assertFalse(visitor.isMainTopLevelTypeDesignated()));
	}
	
}
