package eu.jsparrow.core.visitor.assertj.dedicated;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import eu.jsparrow.common.UsesSimpleJDTUnitFixture;

@SuppressWarnings("nls")
public class UseDedicatedAssertJAssertionsWithLiteralsASTVisitorTest extends UsesSimpleJDTUnitFixture {

	@BeforeEach
	void setUp() throws Exception {
		setVisitor(new UseDedicatedAssertJAssertionsASTVisitor());
		addDependency("org.assertj", "assertj-core", "3.21.0");
		fixture.addImport("org.assertj.core.api.Assertions.assertThat", true, false);
	}

	@ParameterizedTest
	@ValueSource(strings = {
			"assertThat(o1 != null).isTrue()",
			"assertThat(null != o1).isTrue()",
			"assertThat(o1 == null).isFalse()",
			"assertThat(null == o1).isFalse()"
	})
	void visit_ObjectIsNotNull_shouldTransform(String originalInvocation) throws Exception {
		String original = String.format("" +
				"		Object o1 = new Object();\n" +
				"		%s;",
				originalInvocation);

		String expected = "" +
				"		Object o1 = new Object();\n"
				+ "		assertThat(o1).isNotNull();";

		assertChange(original, expected);
	}

	@ParameterizedTest
	@ValueSource(strings = {
			"assertThat(o1 == null).isTrue()",
			"assertThat(null == o1).isTrue()",
			"assertThat(o1 != null).isFalse()",
			"assertThat(null != o1).isFalse()"
	})
	void visit_ObjectIsNull_shouldTransform(String originalInvocation) throws Exception {
		String original = String.format("" +
				"		Object o1 = null;\n" +
				"		%s;",
				originalInvocation);

		String expected = "" +
				"		Object o1 = null;\n"
				+ "		assertThat(o1).isNull();";

		assertChange(original, expected);
	}

	@Test
	void visit_NullIsNull_shouldNotTransform() throws Exception {
		String original = "" +
				"		assertThat(null == null).isTrue();";

		assertNoChange(original);
	}

	/**
	 * This test is expected to fail as soon as transformation is prohibited for
	 * assertions like {@code assertThat(o.equals(null)).isTrue();}
	 * 
	 */
	@Test
	void visit_ObjectEqualsNullIsTrue_shouldNotTransform_butTransforms() throws Exception {
		String original = "" +
				"			Object o = null;\n"
				+ "			assertThat(o.equals(null)).isTrue();";

		String expected = "" +
				"			Object o = null;\n"
				+ "			assertThat(o).isNull();";

		assertChange(original, expected);
	}

	@Test
	void visit_ObjectEqualsNullIsFalse_shouldTransform() throws Exception {
		String original = "" +
				"		Object o = new Object();\n"
				+ "		assertThat(o.equals(null)).isFalse();";

		String expected = "" +
				"		Object o = new Object();\n"
				+ "		assertThat(o).isNotNull();";

		assertChange(original, expected);
	}
}
