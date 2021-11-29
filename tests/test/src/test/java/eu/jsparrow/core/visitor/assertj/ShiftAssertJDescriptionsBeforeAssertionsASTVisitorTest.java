package eu.jsparrow.core.visitor.assertj;

import static java.lang.String.format;
import static org.junit.jupiter.params.provider.Arguments.of;

import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import eu.jsparrow.common.UsesSimpleJDTUnitFixture;

class ShiftAssertJDescriptionsBeforeAssertionsASTVisitorTest extends UsesSimpleJDTUnitFixture {

	@BeforeEach
	void setUp() throws Exception {
		super.setVisitor(new ShiftAssertJDescriptionsBeforeAssertionsASTVisitor());
		addDependency("org.assertj", "assertj-core", "3.21.0");
		fixture.addImport("org.assertj.core.api.Assertions.assertThat", true, false);
	}

	private static Stream<Arguments> createBaseCases() {
		final String original = "assertThat(\"\").isEqualTo(\"\").%s(\"Description\");";
		final String expected = "assertThat(\"\").%s(\"Description\").isEqualTo(\"\");";
		return Stream.of(
				of(format(original, "as"), 						format(expected, "as")),
				of(format(original, "describedAs"), 			format(expected, "describedAs")),
				of(format(original, "withFailMessage"), 		format(expected, "withFailMessage")),
				of(format(original, "overridingErrorMessage"), 	format(expected, "overridingErrorMessage")));
	}

	private static Stream<String> missingAssertions() {
		final String original = "assertThat(\"\").%s(\"Description\");";
		return Stream.of(
				format(original, "as"),
				format(original, "describedAs"),
				format(original, "withFailMessage"),
				format(original, "overridingErrorMessage"));
	}

	@ParameterizedTest
	@MethodSource("createBaseCases")
	void visit_settingDescriptorAfterAssertion_shouldTransform(String original, String expected) throws Exception {
		assertChange(original, expected);
	}
	
	@ParameterizedTest
	@MethodSource("missingAssertions")
	void visit_missingAssertion_shouldNotTransform(String original) throws Exception {
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
	void visit_settingMultipleDescriptors_shouldNotTransform() throws Exception {
		final String original = "assertThat(\"\").as(\"Description\").as(\"Description\");";
		assertNoChange(original);
	}
	
	@Test
	void visit_missingAssertJAssertionInvocation_shouldNotTransform() throws Exception {
		fixture.addImport("org.assertj.core.api.AbstractStringAssert");
		String original = ""
				+ "AbstractStringAssert<?> stringAssert = null;\n"
				+ "stringAssert.as(\"Description\");";
		assertNoChange(original);
	}
}
