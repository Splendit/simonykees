package eu.jsparrow.core.visitor.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class UseOffsetBasedStringMethodsASTVisitorTest extends UsesSimpleJDTUnitFixture {
	@BeforeEach
	public void setUp() throws Exception {
		setVisitor(new UseOffsetBasedStringMethodsASTVisitor());
	}

	@Test
	public void visit_SubstringIndexOfCharacter_shouldTransform() throws Exception {
		String original = "" +
				"		String str = \"Hello World!\";\n" +
				"		int index = str.substring(6).indexOf('d');\n";
		String expected = "" +
				"		String str = \"Hello World!\";\n" +
				"		int index=max(str.indexOf('d',6) - 6,-1);\n";

		assertChange(original, expected);
	}

	@Test
	public void visit_SubstringIndexOfString_shouldTransform() throws Exception {
		String original = "" +
				"		String str = \"Hello World!\";\n" +
				"		int index = str.substring(6).indexOf(\"d\");";
		String expected = "" +
				"		String str = \"Hello World!\";\n" +
				"		int index=max(str.indexOf(\"d\",6) - 6,-1);";

		assertChange(original, expected);
	}

	@Test
	public void visit_SubstringLastIndexOfCharacter_shouldTransform() throws Exception {
		String original = "" +
				"		String str = \"Hello World!\";\n" +
				"		int index = str.substring(6).lastIndexOf('d');";
		String expected = "" +
				"		String str = \"Hello World!\";\n" +
				"		int index=max(str.lastIndexOf('d',6) - 6,-1);";

		assertChange(original, expected);
	}

	@Test
	public void visit_SubstringLastIndexOfString_shouldTransform() throws Exception {
		String original = "" +
				"		String str = \"Hello World!\";\n" +
				"		int index = str.substring(6).lastIndexOf(\"d\");";
		String expected = "" +
				"		String str = \"Hello World!\";\n" +
				"		int index=max(str.lastIndexOf(\"d\",6) - 6,-1);";

		assertChange(original, expected);
	}

	@Test
	public void visit_SubstringStartsWith_shouldTransform() throws Exception {
		String original = "" +
				"		String str = \"Hello World!\";\n" +
				"		boolean startsWith = str.substring(6).startsWith(\"World\");";
		String expected = "" +
				"		String str = \"Hello World!\";\n" +
				"		boolean startsWith = str.startsWith(\"World\", 6);";

		assertChange(original, expected);
	}

	@Test
	public void visit_SubstringWithTwoArguments_shouldNotTransform() throws Exception {
		String original = "" +
				"		String str = \"Hello World!\";\n" +
				"		int index = str.substring(6, 11).indexOf('d');";
		assertNoChange(original);
	}

	@Test
	public void visit_IndexOfWithTwoArguments_shouldNotTransform() throws Exception {
		String original = "" +
				"		String str = \"Hello World!\";\n" +
				"		int index = str.substring(6).indexOf('d', 1);";
		assertNoChange(original);
	}

	@Test
	public void visit_NonStringIndexOf_shouldNotTransform() throws Exception {
		String original = "" +
				"		class HelloWorld {\n" +
				"			int indexOf(int character) {\n" +
				"				return 1;\n" +
				"			}\n" +
				"		}\n" +
				"		int index = new HelloWorld().indexOf('d');\n";
		assertNoChange(original);
	}

	@Test
	public void visit_IndexOfExpressionNotMethodInvocation_shouldNotTransform() throws Exception {
		String original = "" +
				"		String str = \"Hello World!\";\n" +
				"		int index = str.indexOf('d');";
		assertNoChange(original);
	}

}
