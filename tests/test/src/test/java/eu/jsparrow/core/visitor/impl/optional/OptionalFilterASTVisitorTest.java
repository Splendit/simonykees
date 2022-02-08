package eu.jsparrow.core.visitor.impl.optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.common.UsesSimpleJDTUnitFixture;
import eu.jsparrow.core.visitor.optional.OptionalFilterASTVisitor;

class OptionalFilterASTVisitorTest extends UsesSimpleJDTUnitFixture {

	@BeforeEach
	void beforeEach() throws Exception {
		setVisitor(new OptionalFilterASTVisitor());
		fixture.addImport("java.util.Optional");
	}

	@Test
	void visit_baseCase_shouldTransform() throws Exception {
		String original = "" +
				"		Optional<String> optional = Optional.empty();\n" +
				"		optional.ifPresent(value -> {\n" +
				"			if(!value.isEmpty()) {\n" +
				"				System.out.println(value);\n" +
				"			}\n" +
				"		});";
		String expected = "" +
				"		Optional<String> optional = Optional.empty();\n" +
				"		optional.filter(value -> !value.isEmpty()).ifPresent(value -> {\n" +
				"			System.out.println(value);\n" +
				"		});";

		assertChange(original, expected);
	}

	@Test
	void visit_usingExplicitTypes_shouldTransform() throws Exception {
		String original = "" +
				"		Optional<String> optional = Optional.empty();\n" +
				"		optional.ifPresent((String value) -> {\n" +
				"			if (!value.isEmpty()) {\n" +
				"				System.out.println(value);\n" +
				"			}\n" +
				"		});";
		String expected = "" +
				"		Optional<String> optional = Optional.empty();\n" +
				"		optional.filter((String value) -> !value.isEmpty()).ifPresent((String value) -> {\n" +
				"			System.out.println(value);\n" +
				"		});";

		assertChange(original, expected);
	}

	/*
	 * Negative Test Cases
	 */

	@Test
	void visit_multipleBodyStatements_shouldNotTransform() throws Exception {
		assertNoChange("" +
				"		Optional<String> optional = Optional.of(\"value\");\n" +
				"		optional.ifPresent(value -> {\n" +
				"			if(!value.isEmpty()) {\n" +
				"				System.out.println(value);\n" +
				"				String test = value.replace(\"t\", \"o\");\n" +
				"			}\n" +
				"			System.out.print(value);\n" +
				"		});");
	}

	@Test
	void visit_missingIfStatement_shouldNotTransform() throws Exception {
		assertNoChange("" +
				"		Optional<String> optional = Optional.of(\"value\");\n" +
				"		optional.ifPresent(value -> {\n" +
				"			String test = value.replace(\"t\", \"o\");\n" +
				"		});");
	}

	@Test
	void visit_emptyLambdaBody_shouldNotTransform() throws Exception {
		assertNoChange("" +
				"		Optional<String> optional = Optional.of(\"value\");\n" +
				"		optional.ifPresent(value -> {\n" +
				"		});");
	}

	@Test
	void visit_unrelatedIfCondition_shouldNotTransform() throws Exception {
		assertNoChange("" +
				"		Optional<String> optional = Optional.empty();\n" +
				"		optional.ifPresent(value -> {\n" +
				"			if (!optional.isEmpty()) {\n" +
				"				System.out.println(value);\n" +
				"			}\n" +
				"		});");
	}

	@Test
	void visit_emptyIfStatement_shouldNotTransform() throws Exception {
		assertNoChange("" +
				"		Optional<String> optional = Optional.empty();\n" +
				"		optional.ifPresent(value -> {\n" +
				"			if (!optional.isEmpty()) {\n" +
				"\n" +
				"			}\n" +
				"		});");
	}

	@Test
	void visit_ifThenElse_shouldNotTransform() throws Exception {
		assertNoChange("" +
				"		Optional<String> optional = Optional.empty();\n" +
				"		optional.ifPresent(value -> {\n" +
				"			if (!value.isEmpty()) {\n" +
				"				System.out.println(value);\n" +
				"			} else {\n" +
				"				\n" +
				"			}\n" +
				"		});");
	}

	@Test
	void visit_singleExpressionLambdaBody_shouldNotTransform() throws Exception {
		assertNoChange("" +
				"		Optional<String> optional = Optional.empty();\n" +
				"		optional.ifPresent(value -> System.out.println(value));");
	}

	@Test
	void visit_lambdaNotAsMethodArgument_shouldNotTransform() throws Exception {
		assertNoChange("Runnable r = () -> {};");
	}

	@Test
	void visit_lambdaNotAsOptionalIfPresent_shouldNotTransform() throws Exception {
		assertNoChange("" +
				"		Optional<String> optional = Optional.empty();\n" +
				"		optional.filter(value -> !value.isEmpty());");
	}
}
