package org.eu.jsparrow.rules.java16.textblock;

import org.eclipse.jdt.core.JavaCore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import eu.jsparrow.common.UsesSimpleJDTUnitFixture;
import eu.jsparrow.rules.java16.textblock.UseTextBlockASTVisitor;

@SuppressWarnings("nls")
public class UseTextBlockASTVisitorTest extends UsesSimpleJDTUnitFixture {

	@BeforeEach
	public void setUp() {
		setJavaVersion(JavaCore.VERSION_16);
		setVisitor(new UseTextBlockASTVisitor());
	}

	@Test
	public void visit_ConcatenationOfHTML_shouldTransform() throws Exception {
		String original = "" +
				"		String html = \"\" +\n" +
				"				\"              <html>\\n\" + \n" +
				"				\"                  <body>\\n\"+ \n" +
				"				\"                      <p>Hello, world</p>\\n\" + \n" +
				"				\"                  </body>\\n\"+\n" +
				"				\"              </html>\\n\";";

		String expected = "" +
				"  String html=\"\"\"\n" +
				"                              <html>\n" +
				"                                  <body>\n" +
				"                                      <p>Hello, world</p>\n" +
				"                                  </body>\n" +
				"                              </html>\n" +
				"                \"\"\";";

		assertChange(original, expected);
	}

	@Test
	public void visit_ConcatenationWithoutLineBreakAtEnd_shouldTransform() throws Exception {
		String original = "" +
				"		String html = \"\" +\n" +
				"				\"              <html>\\n\" + \n" +
				"				\"                  <body>\\n\"+ \n" +
				"				\"                      <p>Hello, world</p>\\n\" + \n" +
				"				\"                  </body>\\n\"+\n" +
				"				\"              </html>\";";

		String expected = "" +
				"  String html=\"\"\"\n" +
				"                              <html>\n" +
				"                                  <body>\n" +
				"                                      <p>Hello, world</p>\n" +
				"                                  </body>\n" +
				"                              </html>\\\n" +
				"                \"\"\";";

		assertChange(original, expected);
	}

	@Test
	public void visit_MacOSConcatenationWithoutLineBreakAtEnd_shouldTransform() throws Exception {
		String original = "" +
				"		String html = \"\" +\n" +
				"				\"              <html>\\r\" + \n" +
				"				\"                  <body>\\r\"+ \n" +
				"				\"                      <p>Hello, world</p>\\r\" + \n" +
				"				\"                  </body>\\r\"+\n" +
				"				\"              </html>\";";

		String expected = "" +
				"  String html=\"\"\"\n" +
				"                              <html>\n" +
				"                                  <body>\n" +
				"                                      <p>Hello, world</p>\n" +
				"                                  </body>\n" +
				"                              </html>\\\n" +
				"                \"\"\";";

		assertChange(original, expected);
	}

	@Test
	public void visit_TextContainingEmptyLines_shouldTransform() throws Exception {
		String original = "" +
				"		String text = \"\" + //\n" +
				"				\"\\n\" + //\n" +
				"				\"     AAA\\n\" + //\n" +
				"				\"     BBB\\n\" + //\n" +
				"				\"\\n\" + //\n" +
				"				\"     CCC\\n\" + //\n" +
				"				\"     DDD\\n\\n\";";

		String expected = "" +
				"  String text=\"\"\"\n"
				+ "\n"
				+ "                     AAA\n"
				+ "                     BBB\n"
				+ "\n"
				+ "                     CCC\n"
				+ "                     DDD\n"
				+ "\n"
				+ "                \"\"\";";

		assertChange(original, expected);
	}

	@Test
	public void visit_ConcatenationWithParentheses_shouldTransform() throws Exception {
		String original = "" +
				"	String exampleWithParentheses = \"\" + //\n" +
				"			(\"A-1\" + \"      A-2\") + \"      A-3\" + \"      A-4\" + \"      A-5\\n\" + //\n" +
				"			((\"B-1\" + \"      B-2\") + \"      B-3\") + \"      B-4\" + (((\"      B-5\\n\" + //\n" +
				"			\"C-1\" + (\"      C-2\" + \"      C-3\" + \"      C-4\") + \"      C-5\\n\" + //\n" +
				"			(\"D-1\" + \"      D-2\" + \"      D-3\")) + \"      D-4\" + \"      D-5\\n\" + //\n" +
				"			\"E-1\")) + \"      E-2\" + ((\"      E-3\") + \"      E-4\") + \"      E-5\\n\";";

		String expected = "" +
				"  String exampleWithParentheses=\"\"\"\n" +
				"                A-1      A-2      A-3      A-4      A-5\n" +
				"                B-1      B-2      B-3      B-4      B-5\n" +
				"                C-1      C-2      C-3      C-4      C-5\n" +
				"                D-1      D-2      D-3      D-4      D-5\n" +
				"                E-1      E-2      E-3      E-4      E-5\n" +
				"                \"\"\";";

		assertChange(original, expected);
	}

	@Test
	public void visit_ConcatenationWithNullLiteral_shouldTransform() throws Exception {
		String original = "" +
				"		String exampleWithNulLiteral = \"\" +\n" +
				"				\"              <html>\\n\" + \n" +
				"				\"                  <body>\\n\"+ \n" +
				"				\"                      <p>\" + null + \"</p>\\n\" + \n" +
				"				\"                  </body>\\n\"+\n" +
				"				\"              </html>\\n\";";

		String expected = "" +
				"  String exampleWithNulLiteral=\"\"\"\n" +
				"                              <html>\n" +
				"                                  <body>\n" +
				"                                      <p>null</p>\n" +
				"                                  </body>\n" +
				"                              </html>\n" +
				"                \"\"\";";

		assertChange(original, expected);
	}

	@Test
	public void visit_ConcatenationWithBooleanTrue_shouldTransform() throws Exception {
		String original = "" +
				"		String exampleWithBooleanTrue = \"\" +\n"
				+ "				\"              <html>\\n\" + \n"
				+ "				\"                  <body>\\n\"+ \n"
				+ "				\"                      <p>\" + true + \"</p>\\n\" + \n"
				+ "				\"                  </body>\\n\"+\n"
				+ "				\"              </html>\\n\";";

		String expected = "" +
				"  String exampleWithBooleanTrue=\"\"\"\n"
				+ "                              <html>\n"
				+ "                                  <body>\n"
				+ "                                      <p>true</p>\n"
				+ "                                  </body>\n"
				+ "                              </html>\n"
				+ "                \"\"\";";

		assertChange(original, expected);
	}

	@Test
	public void visit_ConcatenationWithBooleanFalse_shouldTransform() throws Exception {
		String original = "" +
				"		String exampleWithBooleanFalse = \"\" +\n"
				+ "				\"              <html>\\n\" + \n"
				+ "				\"                  <body>\\n\"+ \n"
				+ "				\"                      <p>\" + false + \"</p>\\n\" + \n"
				+ "				\"                  </body>\\n\"+\n"
				+ "				\"              </html>\\n\";";

		String expected = "" +
				"  String exampleWithBooleanFalse=\"\"\"\n"
				+ "                              <html>\n"
				+ "                                  <body>\n"
				+ "                                      <p>false</p>\n"
				+ "                                  </body>\n"
				+ "                              </html>\n"
				+ "                \"\"\";";

		assertChange(original, expected);
	}

	@ParameterizedTest
	@ValueSource(strings = { "0", "101259", "211", "7654", "99995" })
	public void visit_ConcatenationWithNumericLiterals_shouldTransform(String numericToken) throws Exception {
		String original = "" +
				"		String exampleWithNumericLiterals = \"\" + \n" +
				"			\"              <html>\\n\" +\n" +
				"			\"                  <body>\\n\" +\n" +
				"			\"                      <p> \" + " + numericToken + " + \"</p>\\n\" +\n" +
				"			\"                  </body>\\n\" +\n" +
				"			\"              </html>\\n\";";

		String expected = "" +
				"  String exampleWithNumericLiterals=\"\"\"\n" +
				"                              <html>\n" +
				"                                  <body>\n" +
				"                                      <p> " + numericToken + "</p>\n" +
				"                                  </body>\n" +
				"                              </html>\n" +
				"                \"\"\";";

		assertChange(original, expected);
	}

	@Test
	public void visit_IntLiteralAsFirstOperand_shouldTransform() throws Exception {
		String original = "" +
				"		String text = \n" +
				"				1 + \"st line\\n\" +\n" +
				"				2 + \"nd line\\n\" + \n" +
				"				3 + \"rd line\\n\" + \n" +
				"				4 + \"th line\\n\" + \n" +
				"				5 + \"th line\\n\";";

		String expected = "" +
				"  String text=\"\"\"\n" +
				"                1st line\n" +
				"                2nd line\n" +
				"                3rd line\n" +
				"                4th line\n" +
				"                5th line\n" +
				"                \"\"\";";

		assertChange(original, expected);
	}

	@Test
	public void visit_ConcatenationWithCharacterLiteral_shouldTransform() throws Exception {
		String original = "" +
				"		String exampleWithCharacterLiteral = \"\" +\n"
				+ "				\"              <html>\\n\" + \n"
				+ "				\"                  <body>\\n\"+ \n"
				+ "				\"                      <p>\" + 'A' + \"</p>\\n\" + \n"
				+ "				\"                  </body>\\n\"+\n"
				+ "				\"              </html>\\n\";";

		String expected = "" +
				"  String exampleWithCharacterLiteral=\"\"\"\n"
				+ "                              <html>\n"
				+ "                                  <body>\n"
				+ "                                      <p>A</p>\n"
				+ "                                  </body>\n"
				+ "                              </html>\n"
				+ "                \"\"\";";

		assertChange(original, expected);
	}

	@Test
	public void visit_RepeatedQuotationMarks_shouldTransform() throws Exception {
		String original = "" +
				"		String string2 = \"\" +\n" +
				"				\"\\\"\\n\" +\n" +
				"				\"\\\"\\\"\\n\" +\n" +
				"				\"\\\"\\\"\\\"\\n\" +\n" +
				"				\"\\\"\\\"\\\"\\\"\\n\" +\n" +
				"				\"\\\"\\\"\\\"\\\"\\\"\\n\";";

		String expected = "" +
				"  String string2=\"\"\"\n" +
				"                \"\n" +
				"                \"\"\n" +
				"                \"\"\\\"\n" +
				"                \"\"\\\"\"\n" +
				"                \"\"\\\"\"\"\n" +
				"                \"\"\";";

		assertChange(original, expected);
	}

	@Test
	public void visit_SupportedEscapeSequences_shouldTransform() throws Exception {
		String original = "" +
				"	String string2 = \"\" +\n" +
				"			\"backslash b --\\b--\\n\" +\n" +
				"			\"tabulator --\\t--\\n\" +\n" +
				"			\"form feed --\\f--\\n\" +\n" +
				"			\"simple quotation mark --\\'--\\n\" +\n" +
				"			\"double quotation mark --\\\"--\\n\";";

		String expected = "" +
				"  String string2=\"\"\"\n"
				+ "                backslash b --\b--\n"
				+ "                tabulator --	--\n"
				+ "                form feed --\f--\n"
				+ "                simple quotation mark --'--\n"
				+ "                double quotation mark --\"--\n"
				+ "                \"\"\";";

		assertChange(original, expected);
	}

	@Test
	public void visit_ConcatenationWithParenthesizedIntAddition_shouldNotTransform() throws Exception {
		String original = "" +
				"		String exampleWithParenthesizedIntAddition = \"\" + \n" +
				"				\"              <html>\\n\" +\n" +
				"				\"                  <body>\\n\" +\n" +
				"				\"                      <p> \" + (1 + 1) + \"</p>\\n\" +\n" +
				"				\"                  </body>\\n\" +\n" +
				"				\"              </html>\\n\";";

		assertNoChange(original);
	}

	@Test
	public void visit_FirstLineSimpleName_shouldNotTransform() throws Exception {
		String original = "" +
				"		String line1 = \"first line\\n\";\n" +
				"		String text = line1 + \n" +
				"				\"second line\\n\" + \n" +
				"				\"third line\\n\" + \n" +
				"				\"fourth line\\n\" + \n" +
				"				\"last line\\n\";";

		assertNoChange(original);
	}

	@Test
	public void visit_LessThanThreeLines_shouldNotTransform() throws Exception {
		String original = "" +
				"		String text = \"\" + \n" +
				"				\"first line\\n\" + \n" +
				"				\"second line\\n\";";

		assertNoChange(original);
	}

	@Test
	public void visit_LessThanThreeOperands_shouldNotTransform() throws Exception {
		String original = "String text = \"A\\nB\\nC\\nD\\nE\" + \"A\\nB\\nC\\nD\\nE\";";
		assertNoChange(original);
	}

	@Test
	public void visit_TripleQuotationMarkAtEnd_shouldNotTransform() throws Exception {
		String original = "" +
				"		String string2 = \"\" + //\n"
				+ "				\"\\\"\\n\" + //\n"
				+ "				\"\\\"\\\"\\n\" + //\n"
				+ "				\"\\\"\\\"\\\"\\n\" + //\n"
				+ "				\"\\\"\\\"\\\"\\\"\\n\" + //\n"
				+ "				\"\\\"\\\" \\\"\\\"\\\"\";";

		assertNoChange(original);
	}

	@Test
	public void visit_OnlyEmptyLines_shouldNotTransform() throws Exception {
		String original = "" +
				"		String text = \n" +
				"				\"\\n\" +\n" +
				"				\"\\n\" +\n" +
				"				\"\\n\" +\n" +
				"				\"\\n\" +\n" +
				"				\"\\n\";";

		assertNoChange(original);
	}

	@ParameterizedTest
	@ValueSource(strings = { "1L", "1l", "1.0", "1.0F", "1.0f", "1.0D", "1.0d", "00", "010", "0XFFF", "0xFFF",
			"1000_0000" })
	public void visit_ConcatenationWithNotSupportedNumericLiterals_shouldNotTransform(String numericToken)
			throws Exception {
		String original = "" +
				"		String exampleWithNumericLiterals = \"\" + \n" +
				"			\"              <html>\\n\" +\n" +
				"			\"                  <body>\\n\" +\n" +
				"			\"                      <p> \" + " + numericToken + " + \"</p>\\n\" +\n" +
				"			\"                  </body>\\n\" +\n" +
				"			\"              </html>\\n\";";

		assertNoChange(original);
	}

	@Test
	public void visit_ConcatenationWithMultiplication_shouldNotTransform() throws Exception {
		String original = "" +
				"		String exampleWithMultiplication = \"\" + \n" +
				"				\"              <html>\\n\" +\n" +
				"				\"                  <body>\\n\" +\n" +
				"				\"                      <p> \" + 2 * 3 + \"</p>\\n\" +\n" +
				"				\"                  </body>\\n\" +\n" +
				"				\"              </html>\\n\";";

		assertNoChange(original);
	}

	@Test
	public void visit_LinesWithTrailingBlanks_shouldNotTransform() throws Exception {
		String original = "" +
				"		String html = \"\" +\n" +
				"				\"              <html>                  \\n\" + \n" +
				"				\"                  <body>\\n\"+ \n" +
				"				\"                      <p>Hello, world</p>\\n\" + \n" +
				"				\"                  </body>\\n\"+\n" +
				"				\"              </html>\\n\";";

		assertNoChange(original);
	}

	@Test
	public void visit_UnsupportedEscapeSequence_shouldNotTransform() throws Exception {
		String original = "" +
				"		String string2 = \"\" + //\n" +
				"				\"first line\\n\" + //\n" +
				"				\"second line\\n\" + //\n" +
				"				\"third line\\n\" + //\n" +
				"				\"unsupported escape sequence --\\uFFFF--\\n\";";

		assertNoChange(original);
	}
}
