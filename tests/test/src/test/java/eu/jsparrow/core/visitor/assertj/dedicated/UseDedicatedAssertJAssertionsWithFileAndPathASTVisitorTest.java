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

	public static Stream<Arguments> booleanAssertionOnPathMethod() throws Exception {
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
	@MethodSource("booleanAssertionOnPathMethod")
	void visit_AssertThatPathMethodIsTrue_shouldTransform(String originalInvocation, String booleanAssertion,
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

	public static Stream<Arguments> notSupportedBooleanAssertionOnPathMethod() throws Exception {
		return Stream.of(
				Arguments.of("startsWith(\"/home/gregor/\")", IS_TRUE),
				Arguments.of("endsWith(\"pom.xml\")", IS_TRUE),
				Arguments.of("startsWith(Path.of(\"/home/gregor-1/\"))", IS_FALSE),
				Arguments.of("endsWith(Path.of(\"pom-1.xml\"))", IS_FALSE));
	}

	@ParameterizedTest
	@MethodSource("notSupportedBooleanAssertionOnPathMethod")
	void visit_NotSupportedBooleanAssertionOnPathMethod_shouldNotTransform(String originalInvocation,
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
