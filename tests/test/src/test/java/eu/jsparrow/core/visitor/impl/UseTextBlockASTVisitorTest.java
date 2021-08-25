package eu.jsparrow.core.visitor.impl;

import org.eclipse.jdt.core.JavaCore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.rules.java16.UseTextBlockASTVisitor;

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
				"			\"              <html>\\n\" + \n" +
				"			\"                  <body>\\n\"+ \n" +
				"			\"                      <p>\" +\"null == \" + null +  \"</p>\\n\" + \n" +
				"			\"                  </body>\\n\"+\n" +
				"			\"              </html>\\n\";";

		String expected = "" +
				"  String exampleWithNulLiteral=\"\"\"\n" +
				"                              <html>\n" +
				"                                  <body>\n" +
				"                                      <p>null == null</p>\n" +
				"                                  </body>\n" +
				"                              </html>\n" +
				"                \"\"\";";

		assertChange(original, expected);
	}

	@Test
	public void visit_ConcatenationWithBooleanTrue_shouldTransform() throws Exception {
		String original = "" +
				"		String exampleWithBooleanTrue = \"\" +\n" +
				"			\"              <html>\\n\" + \n" +
				"			\"                  <body>\\n\"+ \n" +
				"			\"                      <p>\" +\"true == \" + true +  \"</p>\\n\" + \n" +
				"			\"                  </body>\\n\"+\n" +
				"			\"              </html>\\n\";";

		String expected = "" +
				"  String exampleWithBooleanTrue=\"\"\"\n" +
				"                              <html>\n" +
				"                                  <body>\n" +
				"                                      <p>true == true</p>\n" +
				"                                  </body>\n" +
				"                              </html>\n" +
				"                \"\"\";";

		assertChange(original, expected);
	}

	@Test
	public void visit_ConcatenationWithBooleanFalse_shouldTransform() throws Exception {
		String original = "" +
				"		String exampleWithBooleanFalse = \"\" +\n" +
				"			\"              <html>\\n\" + \n" +
				"			\"                  <body>\\n\"+ \n" +
				"			\"                      <p>\" +\"false == \" + false +  \"</p>\\n\" + \n" +
				"			\"                  </body>\\n\"+\n" +
				"			\"              </html>\\n\";";

		String expected = "" +
				"  String exampleWithBooleanFalse=\"\"\"\n" +
				"                              <html>\n" +
				"                                  <body>\n" +
				"                                      <p>false == false</p>\n" +
				"                                  </body>\n" +
				"                              </html>\n" +
				"                \"\"\";";

		assertChange(original, expected);
	}

	@Test
	public void visit_ConcatenationWithNumericLiterals_shouldTransform() throws Exception {
		String original = "" +
				"		String exampleWithNumericLiterals = \"\" + \n" +
				"			\"              <html>\\n\" +\n" +
				"			\"                  <body>\\n\" +\n" +
				"			\"                      <p> \" + 1 + \"</p>\\n\" +\n" +
				"			\"                      <p> \" + 1L + \"</p>\\n\" +\n" +
				"			\"                      <p> \" + 0.0f + \"</p>\\n\" +\n" +
				"			\"                      <p> \" + 0.0 + \"</p>\\n\" +\n" +
				"			\"                      <p> \" + 0.0d + \"</p>\\n\" +\n" +
				"			\"                  </body>\\n\" +\n" +
				"			\"              </html>\\n\";";

		String expected = "" +
				"  String exampleWithNumericLiterals=\"\"\"\n" +
				"                              <html>\n" +
				"                                  <body>\n" +
				"                                      <p> 1</p>\n" +
				"                                      <p> 1L</p>\n" +
				"                                      <p> 0.0f</p>\n" +
				"                                      <p> 0.0</p>\n" +
				"                                      <p> 0.0d</p>\n" +
				"                                  </body>\n" +
				"                              </html>\n" +
				"                \"\"\";";

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

	// @Test
	// public void visit__shouldTransform() throws Exception {
	// String original = "" +
	// "";
	//
	// String expected = "" +
	// "";
	//
	// assertChange(original, expected);
	// }

	// @Test
	// public void visit__shouldNotTransform() throws Exception {
	// String original = "" +
	// "";
	//
	// assertNoChange(original);
	// }

}
