package eu.jsparrow.core.visitor.security;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.Name;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.core.visitor.impl.UsesJDTUnitFixture;

@SuppressWarnings("nls")
public class EscapeUserInputsInSQLQueriesASTVisitorTest extends UsesJDTUnitFixture {

	@BeforeEach
	public void setUp() throws Exception {
		setDefaultVisitor(new EscapeUserInputsInSQLQueriesASTVisitor());
		defaultFixture.addImport(java.sql.Connection.class.getName());
		defaultFixture.addImport(java.sql.Statement.class.getName());
		defaultFixture.addImport(java.sql.ResultSet.class.getName());
		defaultFixture.addImport(java.sql.SQLException.class.getName());
	}

	@AfterEach
	public void tearDown() throws Exception {
		fixtureProject.clear();
	}

	private String tryExecute(String queryName) {
		return "" +
				"		try {\n" +
				"			Connection connection = null;\n" +
				"			Statement statement = connection.createStatement();\n" +
				"			ResultSet resultset = statement.executeQuery(" + queryName + ");\n" +
				"		} catch (SQLException e) {\n" +
				"			e.printStackTrace();\n" +
				"		}";
	}

	private void assertContainsImport(String qualifiedName) {
		assertTrue(defaultFixture.getImports()
			.stream()
			.map(ImportDeclaration::getName)
			.map(Name::getFullyQualifiedName)
			.anyMatch(qualifiedName::equals));
	}

	private void assertNotContainsImport(String qualifiedName) {
		assertFalse(defaultFixture.getImports()
			.stream()
			.map(ImportDeclaration::getName)
			.map(Name::getFullyQualifiedName)
			.anyMatch(qualifiedName::equals));
	}

	@Test
	public void visit_RequestGetParameter_shouldTransform() throws Exception {

		String declareRequestField = "" +
				"	class HttpServletRequest {\n" +
				"		String getParameter(String parameterName) {\n" +
				"			return \"\";\n" +
				"		}\n" +
				"	}\n" +
				"\n" +
				"	HttpServletRequest req = null;\n" +
				"\n";

		String original = declareRequestField +
				"	void test() {\n" +
				"		String query = \"SELECT user_id FROM user_data WHERE user_name = '\" + \n" +
				"				req.getParameter(\"userID\") + \n" +
				"				\"' and user_password = '\" + \n" +
				"				req.getParameter(\"pwd\") + \n" +
				"				\"'\";\n" +
				tryExecute("query") +
				"	}";

		String expected = declareRequestField +
				"	void test() {\n" +
				"		Codec<Character> oracleCodec = new OracleCodec();\n" +
				"		String query = \"SELECT user_id FROM user_data WHERE user_name = '\" + \n" +
				"				ESAPI.encoder().encodeForSQL(oracleCodec, req.getParameter(\"userID\")) + \n" +
				"				\"' and user_password = '\" + \n" +
				"				ESAPI.encoder().encodeForSQL(oracleCodec, req.getParameter(\"pwd\")) + \n" +
				"				\"'\";\n" +
				tryExecute("query") +
				"	}";

		assertChange(original, expected);
		assertContainsImport("org.owasp.esapi.ESAPI");
		assertContainsImport("org.owasp.esapi.codecs.Codec");
		assertContainsImport("org.owasp.esapi.codecs.OracleCodec");
	}

	@Test
	public void visit_LocalStringVariables_shouldTransform() throws Exception {

		String original = "" +
				"	void test() {			\n" +
				"		String userName = \"userName\";\n" +
				"		String userPWD = \"userPWD\";\n" +
				"		String query = \"SELECT user_id FROM user_data WHERE user_name = '\" + \n" +
				"				userName + \n" +
				"				\"' and user_password = '\" + \n" +
				"				userPWD + \n" +
				"				\"'\";\n" +
				tryExecute("query") +
				"	}";

		String expected = "" +
				"	void test() {			\n" +
				"		String userName = \"userName\";\n" +
				"		String userPWD = \"userPWD\";\n" +
				"		Codec<Character> oracleCodec = new OracleCodec();\n" +
				"		String query = \"SELECT user_id FROM user_data WHERE user_name = '\" + \n" +
				"				ESAPI.encoder().encodeForSQL(oracleCodec, userName) + \n" +
				"				\"' and user_password = '\" + \n" +
				"				ESAPI.encoder().encodeForSQL(oracleCodec, userPWD) + \n" +
				"				\"'\";\n" +
				tryExecute("query") +
				"	}";

		assertChange(original, expected);
	}

	@Test
	public void visit_StringFields_shouldTransform() throws Exception {

		String original = "" +
				"	private String userName = \"userName\";\n" +
				"	private String userPWD = \"userPWD\";\n" +
				"	void test() {\n" +
				"		String query = \"SELECT user_id FROM user_data WHERE user_name = '\" + \n" +
				"				this.userName + \n" +
				"				\"' and user_password = '\" + \n" +
				"				this.userPWD + \n" +
				"				\"'\";\n" +
				tryExecute("query") +
				"	}";

		String expected = "" +
				"	private String userName = \"userName\";\n" +
				"	private String userPWD = \"userPWD\";\n" +
				"	void test() {\n" +
				"		Codec<Character> oracleCodec = new OracleCodec();\n" +
				"		String query = \"SELECT user_id FROM user_data WHERE user_name = '\" + \n" +
				"				ESAPI.encoder().encodeForSQL(oracleCodec, this.userName) + \n" +
				"				\"' and user_password = '\" + \n" +
				"				ESAPI.encoder().encodeForSQL(oracleCodec, this.userPWD) + \n" +
				"				\"'\";\n" +
				tryExecute("query") +
				"	}";

		assertChange(original, expected);
	}

	@Test
	public void visit_TwoQueriesInSameBlock_shouldTransform() throws Exception {

		String original = "" +
				"	public void test() {\n" +
				"		String userName = \"userName\";\n" +
				"		String query = " +
				"			\"SELECT user_id FROM user_data WHERE user_name = '\" + userName + \"'\";\n" +
				"		String query1 = " +
				"			\"SELECT user_id FROM user_data WHERE user_name = '\" + userName + \"'\";\n" +
				tryExecute("query") +
				tryExecute("query1") +
				"	}";

		String expected = "" +
				"	public void test() {\n" +
				"		String userName = \"userName\";\n" +
				"		Codec<Character> oracleCodec = new OracleCodec();\n" +
				"		String query = " +
				"			\"SELECT user_id FROM user_data WHERE user_name = '\" + " +
				"			ESAPI.encoder().encodeForSQL(oracleCodec, userName) + " +
				"			\"'\";\n" +
				"		String query1 = " +
				"			\"SELECT user_id FROM user_data WHERE user_name = '\" + " +
				"			ESAPI.encoder().encodeForSQL(oracleCodec, userName) + " +
				"			\"'\";\n" +
				tryExecute("query") +
				tryExecute("query1") +
				"		}";

		assertChange(original, expected);
	}

	@Test
	public void visit_TwoQueriesInDifferentBlocks_shouldTransform() throws Exception {

		String original = "" +
				"	public void test() {\n" +
				"		String userName = \"userName\";\n" +
				"		{\n" +
				"			String query = " +
				"				\"SELECT user_id FROM user_data WHERE user_name = '\" + userName + \"'\";\n" +
				tryExecute("query") +
				"		}\n" +
				"		{\n" +
				"			String query1 = " +
				"				\"SELECT user_id FROM user_data WHERE user_name = '\" + userName + \"'\";\n" +
				tryExecute("query1") +
				"		}\n" +
				"	}\n" +
				"";

		String expected = "" +
				"	public void test() {\n" +
				"		String userName = \"userName\";\n" +
				"		{\n" +
				"			Codec<Character> oracleCodec = new OracleCodec();\n" +
				"			String query = " +
				"				\"SELECT user_id FROM user_data WHERE user_name = '\" + " +
				"				ESAPI.encoder().encodeForSQL(oracleCodec, userName) + " +
				"				\"'\";\n" +
				tryExecute("query") +
				"		}\n" +
				"		{\n" +
				"			Codec<Character> oracleCodec1 = new OracleCodec();\n" +
				"			String query1 = " +
				"				\"SELECT user_id FROM user_data WHERE user_name = '\" + " +
				"				ESAPI.encoder().encodeForSQL(oracleCodec1, userName) + " +
				"				\"'\";\n" +
				tryExecute("query1") +
				"		}\n" +
				"	}";

		assertChange(original, expected);
	}

	@Test
	public void visit_oracleCodecAsIntLocalVariable_shouldTransform() throws Exception {
		String original = "" +
				"	public void test() {\n" +
				"		int oracleCodec = 0;\n" +
				"		String userName = \"userName\";\n" +
				"		String query = " +
				"			\"SELECT user_id FROM user_data WHERE user_name = '\" + userName + \"'\";\n" +
				tryExecute("query") +
				"		}";
		String expected = "" +
				"	public void test() {\n" +
				"		int oracleCodec = 0;\n" +
				"		String userName = \"userName\";\n" +
				"		Codec<Character> oracleCodec1 = new OracleCodec();\n" +
				"		String query = " +
				"			\"SELECT user_id FROM user_data WHERE user_name = '\" + " +
				"			ESAPI.encoder().encodeForSQL(oracleCodec1, userName) + " +
				"			\"'\";\n" +
				tryExecute("query") +
				"		}";
		assertChange(original, expected);
	}

	@Test
	public void visit_oracleCodecAsIntFieldOfEnclosingClass_shouldTransform() throws Exception {

		String original = "" +
				"	int oracleCodec = 0;\n" +
				"	String userName = \"userName\";\n" +
				"	class InnerClass {\n" +
				"		void test() {\n" +
				"			String query = \"SELECT user_id FROM user_data WHERE user_name = '\" +\n" +
				"				userName + \n" +
				"				\"'\";\n" +
				tryExecute("query") +
				"		}\n" +
				"	}\n" +
				"	void test() {\n" +
				"		String query = \"SELECT user_id FROM user_data WHERE user_name = '\" + \n" +
				"			userName + \n" +
				"			\"'\";\n" +
				tryExecute("query") +
				"	}";

		String expected = "" +
				"	int oracleCodec = 0;\n" +
				"	String userName = \"userName\";\n" +
				"	class InnerClass {\n" +
				"		void test() {\n" +
				"			Codec<Character> oracleCodec1 = new OracleCodec();\n" +
				"			String query = \"SELECT user_id FROM user_data WHERE user_name = '\" + \n" +
				"					ESAPI.encoder().encodeForSQL(oracleCodec1, userName) + \n" +
				"					\"'\";\n" +
				tryExecute("query") +
				"		}\n" +
				"	}\n" +
				"	void test() {\n" +
				"		Codec<Character> oracleCodec1 = new OracleCodec();\n" +
				"		String query = \"SELECT user_id FROM user_data WHERE user_name = '\" + \n" +
				"				ESAPI.encoder().encodeForSQL(oracleCodec1, userName) + \n" +
				"				\"'\";\n" +
				tryExecute("query") +
				"	}";
		assertChange(original, expected);
	}

	@Test
	public void visit_oracleCodecConflictingWithClassAsQualifier_shouldTransform() throws Exception {
		String original = "" +
				"		static class oracleCodec {\n" +
				"			static final int CONST = 1;\n" +
				"		}\n" +
				"		public void test() {\n" +
				"			String userName = \"userName\";\n" +
				"			String query = \"SELECT user_id FROM user_data WHERE user_name = '\" + \n" +
				"					userName + \n" +
				"					\"'\";\n" +
				tryExecute("query") +
				"			System.out.println(oracleCodec.CONST);\n" +
				"		}";
		String expected = "" +
				"		static class oracleCodec {\n" +
				"			static final int CONST = 1;\n" +
				"		}\n" +
				"		public void test() {\n" +
				"			String userName = \"userName\";\n" +
				"			Codec<Character> oracleCodec1 = new OracleCodec();\n" +
				"			String query = \"SELECT user_id FROM user_data WHERE user_name = '\" + \n" +
				"					ESAPI.encoder().encodeForSQL(oracleCodec1, userName) + \n" +
				"					\"'\";\n" +
				tryExecute("query") +
				"			System.out.println(oracleCodec.CONST);\n" +
				"		}";
		assertChange(original, expected);
	}

	@Test
	public void visit_OracleCodecAsInnerClass_shouldTransform() throws Exception {
		String original = "" +
				"	class OracleCodec {\n" +
				"	}\n" +
				"	public void test() {\n" +
				"		String userName = \"userName\";\n" +
				"		String query = \n" +
				"			\"SELECT user_id FROM user_data WHERE user_name = '\" +\n" +
				"			userName +\n" +
				"			\"'\";\n" +
				tryExecute("query") +
				"	}";

		String expected = "" +
				"class OracleCodec {\n" +
				"}\n" +
				"	public void test() {\n" +
				"		String userName = \"userName\";\n" +
				"		Codec<Character> oracleCodec = new org.owasp.esapi.codecs.OracleCodec();\n" +
				"		String query = \n" +
				"			\"SELECT user_id FROM user_data WHERE user_name = '\" +\n" +
				"			ESAPI.encoder().encodeForSQL(oracleCodec, userName) +\n" +
				"			\"'\";\n" +
				tryExecute("query") +
				"	}";
		assertChange(original, expected);
		assertContainsImport("org.owasp.esapi.ESAPI");
		assertContainsImport("org.owasp.esapi.codecs.Codec");
		assertNotContainsImport("org.owasp.esapi.codecs.OracleCodec");
	}

	@Test
	public void visit_OracleCodecAsImportedClass_shouldTransform() throws Exception {
		defaultFixture.addImport("examplePackage.OracleCodec");
		String original = "" +
				"	public void test() {\n" +
				"		String userName = \"userName\";\n" +
				"		String query = \n" +
				"			\"SELECT user_id FROM user_data WHERE user_name = '\" +\n" +
				"			userName  +\n" +
				"			\"'\";\n" +
				tryExecute("query") +
				"}";
		String expected = "" +
				"	public void test() {\n" +
				"		String userName = \"userName\";\n" +
				"		Codec<Character> oracleCodec = new org.owasp.esapi.codecs.OracleCodec();\n" +
				"		String query = \n" +
				"			\"SELECT user_id FROM user_data WHERE user_name = '\" +\n" +
				"			ESAPI.encoder().encodeForSQL(oracleCodec, userName) +\n" +
				"			\"'\";\n" +
				tryExecute("query") +
				"	}";
		assertChange(original, expected);
		assertContainsImport("org.owasp.esapi.ESAPI");
		assertContainsImport("org.owasp.esapi.codecs.Codec");
		assertNotContainsImport("org.owasp.esapi.codecs.OracleCodec");
	}

	@Test
	public void visit_ImportStaticESAPI_shouldTransform() throws Exception {
		boolean isOnDemand = false;
		boolean isStatic = true;
		defaultFixture.addImport("examplePackage.Constants.ESAPI", isStatic, isOnDemand);
		String original = "" +
				"	public void test() {\n" +
				"		String userName = \"userID\";\n" +
				"		String query = \n" +
				"				\"SELECT user_id FROM user_data WHERE user_name = '\" + \n" +
				"				userName + \n" +
				"				\"'\";		\n" +
				tryExecute("query") +
				"	}";

		String expected = "" +
				"	public void test() {\n" +
				"		String userName = \"userID\";\n" +
				"		Codec<Character> oracleCodec=new OracleCodec();\n" +
				"		String query = \n" +
				"				\"SELECT user_id FROM user_data WHERE user_name = '\" + \n" +
				"				org.owasp.esapi.ESAPI.encoder().encodeForSQL(oracleCodec,userName) + \n" +
				"				\"'\";		\n" +
				tryExecute("query") +
				"	}";
		assertChange(original, expected);
		assertNotContainsImport("org.owasp.esapi.ESAPI");
		assertContainsImport("org.owasp.esapi.codecs.Codec");
		assertContainsImport("org.owasp.esapi.codecs.OracleCodec");
	}

	@Test
	public void visit_LocalClassESAPI_shouldTransform() throws Exception {
		String original = "" +
				"	void testWithLocalClassESAPI() {\n" +
				"		class ESAPI {\n" +
				"		}\n" +
				"	}\n" +
				"	void test() {\n" +
				"		String userName = \"userID\";\n" +
				"		String query = \n" +
				"				\"SELECT user_id FROM user_data WHERE user_name = '\" + \n" +
				"				userName + \n" +
				"				\"'\";\n" +
				tryExecute("query") +
				"	}";

		String expected = "" +
				"	void testWithLocalClassESAPI() {\n" +
				"		class ESAPI {\n" +
				"		}\n" +
				"	}\n" +
				"	void test() {\n" +
				"		String userName = \"userID\";\n" +
				"		Codec<Character> oracleCodec=new OracleCodec();\n" +
				"		String query = \n" +
				"				\"SELECT user_id FROM user_data WHERE user_name = '\" + \n" +
				"				org.owasp.esapi.ESAPI.encoder().encodeForSQL(oracleCodec,userName) + \n" +
				"				\"'\";\n" +
				tryExecute("query") +
				"	}";
		assertChange(original, expected);
		assertNotContainsImport("org.owasp.esapi.ESAPI");
		assertContainsImport("org.owasp.esapi.codecs.Codec");
		assertContainsImport("org.owasp.esapi.codecs.OracleCodec");
	}

	@Test
	public void visit_StaticFinalFieldESAPI_shouldNotTransform() throws Exception {

		String original = "" +
				"	static final int ESAPI = 0;" +
				"	public void test() {\n" +
				"		String userName = \"userID\";\n" +
				"		String query = \n" +
				"				\"SELECT user_id FROM user_data WHERE user_name = '\" + \n" +
				"				userName + \n" +
				"				\"'\";		\n" +
				tryExecute("query") +
				"	}";

		String expected = "" +
				"	static final int ESAPI = 0;" +
				"	public void test() {\n" +
				"		String userName = \"userID\";\n" +
				"		Codec<Character> oracleCodec=new OracleCodec();\n" +
				"		String query = \n" +
				"				\"SELECT user_id FROM user_data WHERE user_name = '\" + \n" +
				"				org.owasp.esapi.ESAPI.encoder().encodeForSQL(oracleCodec,userName) + \n" +
				"				\"'\";		\n" +
				tryExecute("query") +
				"	}";

		assertChange(original, expected);
		assertNotContainsImport("org.owasp.esapi.ESAPI");
		assertContainsImport("org.owasp.esapi.codecs.Codec");
		assertContainsImport("org.owasp.esapi.codecs.OracleCodec");
	}

}
