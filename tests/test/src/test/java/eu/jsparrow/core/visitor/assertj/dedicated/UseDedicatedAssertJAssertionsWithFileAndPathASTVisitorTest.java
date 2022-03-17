package eu.jsparrow.core.visitor.assertj.dedicated;

import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import eu.jsparrow.common.UsesSimpleJDTUnitFixture;

@SuppressWarnings("nls")
public class UseDedicatedAssertJAssertionsWithFileAndPathASTVisitorTest extends UsesSimpleJDTUnitFixture {

	private static final String IS_TRUE = "isTrue()";
	private static final String IS_FALSE = "isFalse()";

	@BeforeEach
	void setUp() throws Exception {
		setVisitor(new UseDedicatedAssertJAssertionsASTVisitor());
		addDependency("org.assertj", "assertj-core", "3.21.0");
		fixture.addImport("org.assertj.core.api.Assertions.assertThat", true, false);
	}

	public static Stream<Arguments> booleanAssertionsWithFileMethods() throws Exception {
		return Stream.of(
				Arguments.of("equals(file)", IS_TRUE, "isEqualTo(file)"),
				Arguments.of("exists()", IS_TRUE, "exists()"),
				Arguments.of("isFile()", IS_TRUE, "isFile()"),
				Arguments.of("isDirectory()", IS_TRUE, "isDirectory()"),
				Arguments.of("isAbsolute()", IS_TRUE, "isAbsolute()"),
				Arguments.of("canRead()", IS_TRUE, "canRead()"),
				Arguments.of("canWrite()", IS_TRUE, "canWrite()"),
				Arguments.of("equals(file)", IS_FALSE, "isNotEqualTo(file)"),
				Arguments.of("exists()", IS_FALSE, "doesNotExist()"),
				Arguments.of("isAbsolute()", IS_FALSE, "isRelative()"));
	}

	@ParameterizedTest
	@MethodSource("booleanAssertionsWithFileMethods")
	void visit_BooleanAssertionsWithFileMethods_shouldTransform(String originalInvocation, String booleanAssertion,
			String expectedInvocation)
			throws Exception {

		fixture.addImport(java.io.File.class.getName());
		String variableDeclaration = "File file = new File(\"pom.xml\");";

		String original = String.format("" +
				"		%s\n" +
				"		assertThat(file.%s).%s;",
				variableDeclaration, originalInvocation, booleanAssertion);

		String expected = String.format("" +
				"		%s\n" +
				"		assertThat(file).%s;",
				variableDeclaration, expectedInvocation);

		assertChange(original, expected);
	}

	public static Stream<Arguments> notSupportedBooleanAssertionsWithFileMethods() throws Exception {
		return Stream.of(
				Arguments.of("canExecute()", IS_TRUE),
				Arguments.of("isHidden()", IS_TRUE),
				Arguments.of("isFile()", IS_FALSE),
				Arguments.of("isDirectory()", IS_FALSE),
				Arguments.of("canRead()", IS_FALSE),
				Arguments.of("canWrite()", IS_FALSE),
				Arguments.of("canExecute()", IS_FALSE),
				Arguments.of("isHidden()", IS_FALSE));
	}

	@ParameterizedTest
	@MethodSource("notSupportedBooleanAssertionsWithFileMethods")
	void visit_NotSupportedBooleanAssertionsWithFileMethods_shouldNotTransform(String originalInvocation,
			String booleanAssertion)
			throws Exception {

		fixture.addImport(java.io.File.class.getName());
		String variableDeclaration = "File file = new File(\"pom.xml\");";

		String original = String.format("" +
				"		%s\n" +
				"		assertThat(file.%s).%s;",
				variableDeclaration, originalInvocation, booleanAssertion);

		assertNoChange(original);
	}

	public static Stream<Arguments> booleanAssertionsWithPathMethods() throws Exception {
		return Stream.of(
				Arguments.of("equals(path)", IS_TRUE, "isEqualTo(path)"),
				Arguments.of("isAbsolute()", IS_TRUE, "isAbsolute()"),
				Arguments.of("startsWith(Path.of(\"/home/gregor/\"))", IS_TRUE,
						"startsWith(Path.of(\"/home/gregor/\"))"),
				Arguments.of("endsWith(Path.of(\"pom.xml\"))", IS_TRUE, "endsWith(Path.of(\"pom.xml\"))"),
				Arguments.of("equals(path)", IS_FALSE, "isNotEqualTo(path)"),
				Arguments.of("isAbsolute()", IS_FALSE, "isRelative()"));
	}

	@ParameterizedTest
	@MethodSource("booleanAssertionsWithPathMethods")
	void visit_BooleanAssertionsWithPathMethods_shouldTransform(String originalInvocation, String booleanAssertion,
			String expectedInvocation)
			throws Exception {

		fixture.addImport(java.nio.file.Path.class.getName());
		String variableDeclaration = "Path path = Path.of(\"/home/gregor/opensource/eclipse-plugin-tests/workspace/simple-test/pom.xml\");";

		String original = String.format("" +
				"		%s\n" +
				"		assertThat(path.%s).%s;",
				variableDeclaration, originalInvocation, booleanAssertion);

		String expected = String.format("" +
				"		%s\n" +
				"		assertThat(path).%s;",
				variableDeclaration, expectedInvocation);

		assertChange(original, expected);
	}

	public static Stream<Arguments> notSupportedBooleanAssertionsWithPathMethods() throws Exception {
		return Stream.of(
				Arguments.of("startsWith(\"/home/gregor/\")", IS_TRUE),
				Arguments.of("endsWith(\"pom.xml\")", IS_TRUE),
				Arguments.of("startsWith(Path.of(\"/home/gregor-1/\"))", IS_FALSE),
				Arguments.of("endsWith(Path.of(\"pom-1.xml\"))", IS_FALSE));
	}

	@ParameterizedTest
	@MethodSource("notSupportedBooleanAssertionsWithPathMethods")
	void visit_NotSupportedBooleanAssertionsWithPathMethods_shouldNotTransform(String originalInvocation,
			String booleanAssertion)
			throws Exception {

		fixture.addImport(java.nio.file.Path.class.getName());
		String variableDeclaration = "Path path = Path.of(\"/home/gregor/opensource/eclipse-plugin-tests/workspace/simple-test/pom.xml\");";

		String original = String.format("" +
				"		%s\n" +
				"		assertThat(path.%s).%s;",
				variableDeclaration, originalInvocation, booleanAssertion);

		assertNoChange(original);
	}
}
