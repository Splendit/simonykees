package eu.jsparrow.core.visitor.assertj.dedicated;

import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import eu.jsparrow.common.UsesSimpleJDTUnitFixture;

@SuppressWarnings("nls")
class UseDedicatedAssertJAssertionsASTVisitorTest extends UsesSimpleJDTUnitFixture {

	@BeforeEach
	void setUp() throws Exception {
		setVisitor(new UseDedicatedAssertJAssertionsASTVisitor());
		addDependency("org.assertj", "assertj-core", "3.21.0");
		fixture.addImport("org.assertj.core.api.Assertions.assertThat", true, false);
	}

	public static Stream<Arguments> stringMethodIsTrue() throws Exception {
		return Stream.of(
				Arguments.of("equals(\"str-1\")", "isEqualTo(\"str-1\")"),
				Arguments.of("equalsIgnoreCase(\"STR-1\")", "isEqualToIgnoringCase(\"STR-1\")"),
				Arguments.of("startsWith(\"str\")", "startsWith(\"str\")"),
				Arguments.of("contains(\"-\")", "contains(\"-\")"),
				Arguments.of("endsWith(\"1\")", "endsWith(\"1\")"),
				Arguments.of("isEmpty()", "isEmpty()"),
				Arguments.of("isBlank()", "isBlank()"));
	}
	
	public static Stream<Arguments> stringMethodIsFalse() throws Exception {
		return Stream.of(
				Arguments.of("equals(\"str-1\")", "isNotEqualTo(\"str-1\")"),
				Arguments.of("equalsIgnoreCase(\"STR-1\")", "isNotEqualToIgnoringCase(\"STR-1\")"),
				Arguments.of("startsWith(\"str\")", "doesNotStartWith(\"str\")"),
				Arguments.of("contains(\"-\")", "doesNotContain(\"-\")"),
				Arguments.of("endsWith(\"1\")", "doesNotEndWith(\"1\")"),
				Arguments.of("isEmpty()", "isNotEmpty()"),
				Arguments.of("isBlank()", "isNotBlank()"));
	}


	@ParameterizedTest
	@MethodSource("stringMethodIsTrue")
	void visit_AssertThatStringMethodIsTrue_shouldTransform(String originalInvocation, String expectedInvocation)
			throws Exception {

		String original = String.format(
				"" +
						"		String s1 = \"str-1\";\n" +
						"		assertThat(s1.%s).isTrue();",
				originalInvocation);

		String expected = String.format(
				"" +
						"		String s1 = \"str-1\";\n" +
						"		assertThat(s1).%s;",
				expectedInvocation);

		assertChange(original, expected);
	}
	
	@ParameterizedTest
	@MethodSource("stringMethodIsFalse")
	void visit_AssertThatStringMethodIsFalse_shouldTransform(String originalInvocation, String expectedInvocation)
			throws Exception {		

		String original = String.format(
				"" +
						"		String s1 = \"str-1\";\n" +
						"		assertThat(s1.%s).isFalse();",
				originalInvocation);

		String expected = String.format(
				"" +
						"		String s1 = \"str-1\";\n" +
						"		assertThat(s1).%s;",
				expectedInvocation);

		assertChange(original, expected);
	}
	
	
	@ParameterizedTest
	@MethodSource("stringMethodIsFalse")
	void visit_AssertThatNegatedStringMethodIsTrue_shouldTransform(String originalInvocation, String expectedInvocation)
			throws Exception {		

		String original = String.format(
				"" +
						"		String s1 = \"str-1\";\n" +
						"		assertThat(!s1.%s).isTrue();",
				originalInvocation);

		String expected = String.format(
				"" +
						"		String s1 = \"str-1\";\n" +
						"		assertThat(s1).%s;",
				expectedInvocation);

		assertChange(original, expected);
	}
	
	@ParameterizedTest
	@MethodSource("stringMethodIsTrue")
	void visit_AssertThatNegatedStringMethodIsFalse_shouldTransform(String originalInvocation,
			String expectedInvocation)
			throws Exception {

		String original = String.format(
				"" +
						"		String s1 = \"str-1\";\n" +
						"		assertThat(!s1.%s).isFalse();",
				originalInvocation);

		String expected = String.format(
				"" +
						"		String s1 = \"str-1\";\n" +
						"		assertThat(s1).%s;",
				expectedInvocation);

		assertChange(original, expected);
	}

}