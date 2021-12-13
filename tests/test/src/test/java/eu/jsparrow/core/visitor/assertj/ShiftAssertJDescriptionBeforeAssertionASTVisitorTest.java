package eu.jsparrow.core.visitor.assertj;

import static java.lang.String.format;

import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import eu.jsparrow.common.UsesSimpleJDTUnitFixture;

class ShiftAssertJDescriptionBeforeAssertionASTVisitorTest extends UsesSimpleJDTUnitFixture {

	@BeforeEach
	void setUp() throws Exception {
		super.setVisitor(new ShiftAssertJDescriptionBeforeAssertionASTVisitor());
		addDependency("org.assertj", "assertj-core", "3.21.0");
		fixture.addImport("org.assertj.core.api.Assertions.assertThat", true, false);
	}

	private static Stream<String> settingDescription() {
		return Stream.of("as", "describedAs", "withFailMessage", "overridingErrorMessage");
	}

	@ParameterizedTest
	@MethodSource("settingDescription")
	void visit_settingDescriptorAfterAssertion_shouldTransform(String methodName) throws Exception {
		final String original = format("assertThat(\"\").isEqualTo(\"\").%s(\"Description\");", methodName);
		final String expected = format("assertThat(\"\").%s(\"Description\").isEqualTo(\"\");", methodName);
		assertChange(original, expected);
	}

	@ParameterizedTest
	@MethodSource("settingDescription")
	void visit_missingAssertion_shouldNotTransform(String methodName) throws Exception {
		final String original = format("assertThat(\"\").%s(\"Description\");",
				methodName);
		assertNoChange(original);
	}

	@Test
	void visit_missingExpression_shouldNotTransform() throws Exception {
		String original = ""
				+ "class Foo {\n"
				+ "	void foo() {\n"
				+ "		as(\"description\");\n"
				+ "	}\n"
				+ "	void as(String val) {}\n"
				+ "}";

		assertNoChange(original);
	}

	@Test
	void visit_missingAssertJInvocationExpression_shouldNotTransform() throws Exception {
		String original = ""
				+ "class Foo {\n"
				+ "	void as(String val) {}\n"
				+ "}\n"
				+ "Foo foo = new Foo();\n"
				+ "foo.as(\"\");";

		assertNoChange(original);
	}

	@Test
	void visit_settingMultipleDescriptors_shouldNotTransform() throws Exception {
		final String original = "assertThat(\"\").as(\"Description\").as(\"Description\");";
		assertNoChange(original);
	}

	@ParameterizedTest
	@MethodSource("settingDescription")
	void visit_settingDescriptionBetweenAssertions_shouldNotTransform(String methodName) throws Exception {
		String original = String.format(""
				+ "assertThat(\"\").isEqualTo(\"\").%s(\"Description\").isEqualTo(\"\");",
				methodName);
		assertNoChange(original);
	}

	@ParameterizedTest
	@MethodSource("settingDescription")
	void visit_settingDescriptionAfterEachAssertion_shouldTransform(String methodName) throws Exception {
		String original = String.format(""
				+ "assertThat(\"\").isEqualTo(\"\").%s(\"Description1\").isEqualTo(\"\").%s(\"Description2\");",
				methodName, methodName);
		String expected = String.format(""
				+ "assertThat(\"\").isEqualTo(\"\").%s(\"Description1\").%s(\"Description2\").isEqualTo(\"\");",
				methodName, methodName);
		assertChange(original, expected);

	}

	@ParameterizedTest
	@MethodSource("settingDescription")
	void visit_typeArguments_shouldTransform(String methodName) throws Exception {
		String original = String.format(""
				+ "assertThat(\"\").isEqualTo(\"\").%s(\"Description1\").isEqualTo(\"\").<String>%s(\"Description2\");"
				+ "assertThat(\"\").isEqualTo(\"\").%s(\"Description1\").<String>isEqualTo(\"\").%s(\"Description2\");",
				methodName, methodName, methodName, methodName);
		String expected = String.format(""
				+ "assertThat(\"\").isEqualTo(\"\").%s(\"Description1\").<String>%s(\"Description2\").isEqualTo(\"\");"
				+ "assertThat(\"\").isEqualTo(\"\").%s(\"Description1\").%s(\"Description2\").<String>isEqualTo(\"\");",
				methodName, methodName, methodName, methodName);
		assertChange(original, expected);

	}

	@ParameterizedTest
	@MethodSource("settingDescription")
	void visit_missingAssertJAssertionInvocation_shouldNotTransform(String methodName) throws Exception {
		fixture.addImport("org.assertj.core.api.AbstractStringAssert");
		String original = String.format(""
				+ "AbstractStringAssert<?> stringAssert = null;\n"
				+ "stringAssert.%s(\"Description\");", methodName);
		assertNoChange(original);
	}
}
