package eu.jsparrow.core.visitor.assertj.dedicated;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.common.UsesSimpleJDTUnitFixture;

@SuppressWarnings("nls")
public class UseDedicatedAssertJAssertionsWithInstanceofASTVisitorTest extends UsesSimpleJDTUnitFixture {

	@BeforeEach
	void setUp() throws Exception {
		setVisitor(new UseDedicatedAssertJAssertionsASTVisitor());
		addDependency("org.assertj", "assertj-core", "3.21.0");
		fixture.addImport("org.assertj.core.api.Assertions.assertThat", true, false);
	}

	@Test
	void visit_instanceofIsTrue_shouldTransform() throws Exception {
		String original = String.format("" +
				"		Object o = new Object();\n"
				+ "		assertThat(o instanceof Object).isTrue();");

		String expected = String.format("" +
				"		Object o = new Object();\n"
				+ "		assertThat(o).isInstanceOf(Object.class);");

		assertChange(original, expected);
	}

	@Test
	void visit_negatedInstanceofIsFalse_shouldTransform() throws Exception {
		String original = String.format("" +
				"		Object o = new Object();\n"
				+ "		assertThat(!(o instanceof Object)).isFalse();");

		String expected = String.format("" +
				"		Object o = new Object();\n"
				+ "		assertThat(o).isInstanceOf(Object.class);");

		assertChange(original, expected);
	}

	@Test
	void visit_instanceofIsFalse_shouldNotTransform() throws Exception {
		String original = String.format("" +
				"		Object o = null;\n"
				+ "		assertThat(o instanceof Object).isFalse();");

		assertNoChange(original);
	}

	@Test
	void visit_negatedInstanceofIsTrue_shouldNotTransform() throws Exception {
		String original = String.format("" +
				"		Object o = null;\n"
				+ "		assertThat(!(o instanceof Object)).isTrue();");

		assertNoChange(original);
	}

	@Test
	void visit_InstanceofParameterizedType_shouldNotTransform() throws Exception {
		String original = ""
				+ "		List<String> stringList = new ArrayList<>();\n"
				+ "		assertThat(stringList instanceof List<String>).isTrue();";
		assertNoChange(original);
	}

	@Test
	void visit_InstanceofWithNotSupportedLeftOperand_shouldNotTransform() throws Exception {
		String original = ""
				+ "		class LocalClass {\n"
				+ "		}\n"
				+ "		LocalClass localCass = new LocalClass();\n"
				+ "		assertThat(localCass instanceof LocalClass).isTrue();";
		assertNoChange(original);
	}
}
