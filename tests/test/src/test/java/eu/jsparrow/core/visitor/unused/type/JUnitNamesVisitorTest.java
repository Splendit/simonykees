package eu.jsparrow.core.visitor.unused.type;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import org.eclipse.jdt.core.dom.CompilationUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import eu.jsparrow.common.UsesJDTUnitFixture;
import eu.jsparrow.jdtunit.util.ASTNodeBuilder;

@SuppressWarnings("nls")
class JUnitNamesVisitorTest extends UsesJDTUnitFixture {

	private static final String UNUSED_NESTED_TYPES = "" +
			"	void methodWithLocalClass() {\n" +
			"		class LocalClass {\n" +
			"		}\n" +
			"	}\n" +
			"\n" +
			"	private class PrivateClass {\n" +
			"	}\n" +
			"\n" +
			"	class PackagePrivateClass {\n" +
			"	}\n" +
			"\n" +
			"	protected class ProtectedClass {\n" +
			"	}\n" +
			"\n" +
			"	public class PublicClass {\n" +
			"	}";

	private static Map<String, Boolean> getAllOptions() {
		Map<String, Boolean> options = new HashMap<>();
		options.put("local-classes", true);
		options.put("private-classes", true);
		options.put("protected-classes", true);
		options.put("package-private-classes", true);
		options.put("public-classes", true);
		return options;
	}

	@BeforeEach
	public void setUp() throws Exception {
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
			"junit",
			"junit.x",
			"org.junit",
			"org.junit.x"
	})
	void visit_JUnitPackageDeclaration_shouldFindJUnitName(String packageName)
			throws Exception {

		String sourceWithJUnitPackageDeclaration = "" +
				"package " + packageName + ";\n" +
				"\n" +
				"public class ExampleClass {\n" +
				"}";

		CompilationUnit compilationUnitWithJUnitPackageDeclaration = ASTNodeBuilder
			.createCompilationUnitFromString(sourceWithJUnitPackageDeclaration);

		JUnitNamesVisitor jUnitNamesVisitor = new JUnitNamesVisitor();
		compilationUnitWithJUnitPackageDeclaration.accept(jUnitNamesVisitor);
		assertTrue(jUnitNamesVisitor.isJUnitNameFound());
	}

	@ParameterizedTest
	@ValueSource(strings = {
			"x.junit",
			"org",
			"x.org.junit"
	})
	void visit_NotJUnitPackageDeclaration_shouldNotFindJUnitName(String packageName)
			throws Exception {

		String sourceWithJUnitPackageDeclaration = "" +
				"package " + packageName + ";\n" +
				"\n" +
				"public class ExampleClass {\n" +
				"}";

		CompilationUnit compilationUnitWithJUnitPackageDeclaration = ASTNodeBuilder
			.createCompilationUnitFromString(sourceWithJUnitPackageDeclaration);

		JUnitNamesVisitor jUnitNamesVisitor = new JUnitNamesVisitor();
		compilationUnitWithJUnitPackageDeclaration.accept(jUnitNamesVisitor);
		assertFalse(jUnitNamesVisitor.isJUnitNameFound());
	}

	@Test
	void visit_UnusedTypesWithoutJUnitNames_shouldBeRemoved()
			throws Exception {

		String unusedTypeDeclaration = UNUSED_NESTED_TYPES;

		UnusedTypesCandidatesVisitor visitor = new UnusedTypesCandidatesVisitor(getAllOptions());
		defaultFixture.addTypeDeclarationFromString(DEFAULT_TYPE_DECLARATION_NAME, unusedTypeDeclaration);
		defaultFixture.accept(visitor);

		assertEquals(1, visitor.getUnusedLocalTypes()
			.size());
		assertEquals(1, visitor.getUnusedPrivateTypes()
			.size());
		assertEquals(3, visitor.getNonPrivateCandidates()
			.size());
	}

	private static Stream<Arguments> jUnit3Imports() throws Exception {
		return Stream.of(
				Arguments.of("junit", false, true),
				Arguments.of("junit.runner", false, true),
				Arguments.of("junit.framework.TestCase", false, false));

	}

	@ParameterizedTest
	@MethodSource(value = "jUnit3Imports")
	void testEmptyNestedTypeDeclarations_shouldBeRemoved(String name, boolean isStatic, boolean isOnDemand)
			throws Exception {

		defaultFixture.addImport(name, isStatic, isOnDemand);

		String unusedTypeDeclaration = UNUSED_NESTED_TYPES;

		UnusedTypesCandidatesVisitor visitor = new UnusedTypesCandidatesVisitor(getAllOptions());
		defaultFixture.addTypeDeclarationFromString(DEFAULT_TYPE_DECLARATION_NAME, unusedTypeDeclaration);
		defaultFixture.accept(visitor);

		assertTrue(visitor.getUnusedLocalTypes()
			.isEmpty());
		assertTrue(visitor.getUnusedPrivateTypes()
			.isEmpty());
		assertTrue(visitor.getNonPrivateCandidates()
			.isEmpty());
	}

	private static Stream<Arguments> jUnitTestAnnotationQualifiedNames() throws Exception {
		return Stream.of(
				Arguments.of("org.junit.Test"),
				Arguments.of("org.junit.jupiter.api.Test"),
				Arguments.of("org.junit.jupiter.params.ParameterizedTest"),
				Arguments.of("org.junit.jupiter.api.RepeatedTest"));

	}

	@ParameterizedTest
	@MethodSource(value = "jUnitTestAnnotationQualifiedNames")
	void visit_UnusedTypesAndJUnitTestAnnotationImports_shouldNotBeRemoved(String name)
			throws Exception {

		defaultFixture.addImport(name);

		String unusedTypeDeclaration = UNUSED_NESTED_TYPES;

		UnusedTypesCandidatesVisitor visitor = new UnusedTypesCandidatesVisitor(getAllOptions());
		defaultFixture.addTypeDeclarationFromString(DEFAULT_TYPE_DECLARATION_NAME, unusedTypeDeclaration);
		defaultFixture.accept(visitor);

		assertTrue(visitor.getUnusedLocalTypes()
			.isEmpty());
		assertTrue(visitor.getUnusedPrivateTypes()
			.isEmpty());
		assertTrue(visitor.getNonPrivateCandidates()
			.isEmpty());
	}

	private static Stream<Arguments> otheOrgJUnitImports() throws Exception {
		return Stream.of(
				Arguments.of("org.junit", false, true),
				Arguments.of("org.junit.jupiter.api.Assertions", false, false),
				Arguments.of("org.junit.jupiter.api.Assertions", true, true),
				Arguments.of("org.junit.jupiter.api.Assertions.assertEquals", true, false));
	}

	@ParameterizedTest
	@MethodSource(value = "otheOrgJUnitImports")
	void visit_UnusedTypesAnOtherOrgJUnitImports_shouldNotBeRemoved(String name, boolean isStatic, boolean isOnDemand)
			throws Exception {

		defaultFixture.addImport(name, isStatic, isOnDemand);

		String unusedTypeDeclaration = UNUSED_NESTED_TYPES;

		UnusedTypesCandidatesVisitor visitor = new UnusedTypesCandidatesVisitor(getAllOptions());
		defaultFixture.addTypeDeclarationFromString(DEFAULT_TYPE_DECLARATION_NAME, unusedTypeDeclaration);
		defaultFixture.accept(visitor);

		assertTrue(visitor.getUnusedLocalTypes()
			.isEmpty());
		assertTrue(visitor.getUnusedPrivateTypes()
			.isEmpty());
		assertTrue(visitor.getNonPrivateCandidates()
			.isEmpty());
	}

	@ParameterizedTest
	@MethodSource(value = "jUnitTestAnnotationQualifiedNames")
	void visit_UnusedTypesAndJUnitTestAnnotation_shouldNotBeRemoved(String qualifiedAnnotationName)
			throws Exception {

		String codeWithTestMethod = UNUSED_NESTED_TYPES;

		codeWithTestMethod += "" +
				"\n" +
				"	@" + qualifiedAnnotationName + "\n" +
				"	void test() {\n" +
				"	}";

		UnusedTypesCandidatesVisitor visitor = new UnusedTypesCandidatesVisitor(getAllOptions());

		defaultFixture.addTypeDeclarationFromString(DEFAULT_TYPE_DECLARATION_NAME, codeWithTestMethod);
		defaultFixture.accept(visitor);

		assertTrue(visitor.getUnusedLocalTypes()
			.isEmpty());
		assertTrue(visitor.getUnusedPrivateTypes()
			.isEmpty());
		assertTrue(visitor.getNonPrivateCandidates()
			.isEmpty());

	}

}
