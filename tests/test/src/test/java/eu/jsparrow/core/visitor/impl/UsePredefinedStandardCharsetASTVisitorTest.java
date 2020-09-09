package eu.jsparrow.core.visitor.impl;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class UsePredefinedStandardCharsetASTVisitorTest extends UsesJDTUnitFixture {

	@BeforeEach
	public void setUp() throws Exception {
		defaultFixture.addImport(java.nio.charset.Charset.class.getName());
		setDefaultVisitor(new UsePredefinedStandardCharsetASTVisitor());

	}

	@AfterEach
	public void tearDown() throws Exception {
		fixtureProject.clear();
	}

	@Test
	public void visit_StringLiteralMatchingConstantName_shouldTransform() throws Exception {
		String original = "" +
				"	void test() {\n" +
				"		Charset c0 = Charset.forName(\"UTF-8\");\n" +
				"		Charset c1 = Charset.forName(\"ISO-8859-1\");\n" +
				"		Charset c2 = Charset.forName(\"US-ASCII\");\n" +
				"		Charset c3 = Charset.forName(\"UTF-16\");\n" +
				"		Charset c4 = Charset.forName(\"UTF-16BE\");\n" +
				"		Charset c5 = Charset.forName(\"UTF-16LE\");\n" +
				"	}";

		String expected = "" +
				"	void test() {\n" +
				"		Charset c0 = StandardCharsets.UTF_8;\n" +
				"		Charset c1 = StandardCharsets.ISO_8859_1;\n" +
				"		Charset c2 = StandardCharsets.US_ASCII;\n" +
				"		Charset c3 = StandardCharsets.UTF_16;\n" +
				"		Charset c4 = StandardCharsets.UTF_16BE;\n" +
				"		Charset c5 = StandardCharsets.UTF_16LE;\n" +
				"	}";

		assertChange(original, expected);
	}

	@Test
	public void visit_CannotImportStandardCharsets_shouldTransform() throws Exception {
		String original = "" +
				"	class StandardCharsets {}\n" +
				"	void test() {\n" +
				"		Charset c0 = Charset.forName(\"UTF-8\");\n" +
				"	}";

		String expected = "" +
				"	class StandardCharsets {}\n" +
				"	void test() {\n" +
				"		Charset c0 = java.nio.charset.StandardCharsets.UTF_8;\n" +
				"	}";

		assertChange(original, expected);
	}

	@Test
	public void visit_StringLiteralNotMatchingConstantName_shouldNotTransform() throws Exception {

		String original = "" +
				"	void test() {\n" +
				"		Charset c = Charset.forName(\"XXXX-XXXX\");\n" +
				"	}";
		assertNoChange(original);
	}

	@Test
	public void visit_StringVariableAsArgument_shouldNotTransform() throws Exception {

		String original = "" +
				"	void test() {\n" +
				"		String charsetUTF_8 = \"UTF-8\";\n" +
				"		Charset c0 = Charset.forName(charsetUTF_8);\n" +
				"	}";
		assertNoChange(original);
	}

	@Test
	public void visit_ForNameNotMethodOfCharset_shouldNotTransform() throws Exception {

		String original = "" +
				"	Charset forName(String charsetName) {\n" +
				"		return null;\n" +
				"	}\n" +
				"	void test() {\n" +
				"		Charset c0 = this.forName(\"UTF-8\");\n" +
				"	}";
		assertNoChange(original);
	}
}
