package eu.jsparrow.core.visitor.impl.optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.common.UsesSimpleJDTUnitFixture;
import eu.jsparrow.core.visitor.optional.OptionalMapASTVisitor;

class OptionalMapASTVisitorTest extends UsesSimpleJDTUnitFixture {

	@BeforeEach
	public void beforeEach() throws Exception {
		setVisitor(new OptionalMapASTVisitor());
		fixture.addImport("java.util.Optional");
	}

	@Test
	void test_baseCase_shouldTransform() throws Exception {
		String original = "" +
				"		Optional<String> optional = Optional.of(\"value\");\n" +
				"		optional.ifPresent(value -> {\n" +
				"			String test = value.replace(\"t\", \"o\");\n" +
				"			System.out.print(test);\n" +
				"		});";
		String expected = "" +
				"		Optional<String> optional = Optional.of(\"value\");\n" +
				"		optional\n" +
				"			.map(value -> value.replace(\"t\", \"o\"))\n" +
				"			.ifPresent(test -> System.out.print(test));";

		assertChange(original, expected);
	}

	@Test
	void test_multipleRemainingStatements_shouldTransform() throws Exception {
		String original = "" +
				"		Optional<String> optional = Optional.of(\"value\");\n" +
				"		optional.ifPresent(value -> {\n" +
				"			String test = value.replace(\"t\", \"o\");\n" +
				"			System.out.print(test);\n" +
				"			System.out.print(test);\n" +
				"		});";
		String expected = "" +
				"		Optional<String> optional = Optional.of(\"value\");\n" +
				"		optional.map(value -> value.replace(\"t\", \"o\")).ifPresent(test -> {\n" +
				"			System.out.print(test);\n" +
				"			System.out.print(test);\n" +
				"		});";

		assertChange(original, expected);
	}

	@Test
	void test_multipleExtractedStatements_shouldTransform() throws Exception {
		String original = "" +
				"		Optional<String> optional = Optional.of(\"value\");\n" +
				"		optional.ifPresent(value -> {\n" +
				"			System.out.print(value);\n" +
				"			String test = value.replace(\"t\", \"o\");\n" +
				"			System.out.print(test);\n" +
				"		});";
		String expected = "" +
				"		Optional<String> optional = Optional.of(\"value\");\n" +
				"		optional.map(value -> {\n" +
				"			System.out.print(value);\n" +
				"			return value.replace(\"t\", \"o\");\n" +
				"		}).ifPresent(test -> System.out.print(test));";

		assertChange(original, expected);
	}

	@Test
	void test_primitiveTypes_shouldTransform() throws Exception {
		String original = "" +
				"		Optional<String> optional = Optional.of(\"value\");\n" +
				"		optional.ifPresent(value -> {\n" +
				"			int length = value.length();\n" +
				"			if(length > 0) {\n" +
				"				System.out.println(\"Length is \" + length);\n" +
				"			}\n" +
				"		});";
		String expected = "" +
				"		Optional<String> optional = Optional.of(\"value\");\n" +
				"		optional.map(value -> value.length()).ifPresent(length -> {\n" +
				"			if(length > 0) {\n" +
				"				System.out.println(\"Length is \" + length);\n" +
				"			}\n" +
				"		});";

		assertChange(original, expected);
	}

	@Test
	void test_usingParameterType_shouldTransform() throws Exception {
		String original = "" +
				"		Optional<String> optional = Optional.of(\"value\");\n" +
				"		optional.ifPresent((String value) -> {\n" +
				"			String test = value.replace(\"t\", \"o\"); \n" +
				"			System.out.print(test);\n" +
				"		});";
		String expected = "" +
				"		Optional<String> optional = Optional.of(\"value\");\n" +
				"		optional.map((String value) -> value.replace(\"t\", \"o\")).ifPresent((String test) -> System.out.print(test));";

		assertChange(original, expected);
	}

	// Negative test cases

	@Test
	void test_nonExtractableBody_shouldNotTransform() throws Exception {
		assertNoChange("" +
				"		Optional<String> optional = Optional.of(\"value\");\n" +
				"		optional.ifPresent(value -> {\n" +
				"			String test = value.replace(\"t\", \"o\");\n" +
				"			System.out.print(test);\n" +
				"			System.out.print(value);\n" +
				"		});");
	}

	@Test
	void test_singleStatementLambdaBlock_shouldNotTransform() throws Exception {
		assertNoChange("" +
				"		Optional<String> optional = Optional.of(\"value\");\n" +
				"		optional.ifPresent(value -> {\n" +
				"			if(value.length() > 1) {				\n" +
				"				System.out.print(value);\n" +
				"			}\n" +
				"		});");
	}
	
	@Test
	void test_duplicatedFinalModifier_shouldTransform() throws Exception {
		String original = "" +
				"		Optional<String> optional = Optional.of(\"value\");\n" +
				"		optional.ifPresent((final String value) -> {\n" +
				"			final String test = value.replace(\"t\", \"o\");\n" +
				"			System.out.print(test);\n" +
				"		});";
		String expected = "" +
				"		Optional<String> optional = Optional.of(\"value\");\n" +
				"		optional\n" +
				"			.map((final String value) -> value.replace(\"t\", \"o\"))\n" +
				"			.ifPresent((final String test) -> System.out.print(test));";

		assertChange(original, expected);
	}
}
