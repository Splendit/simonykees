package eu.jsparrow.core.visitor.security;

import static org.junit.Assert.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.core.visitor.impl.UsesJDTUnitFixture;

@SuppressWarnings("nls")
public class EscapingDynamicQueriesASTVisitorTest extends UsesJDTUnitFixture {

	@BeforeEach
	public void setUp() throws Exception {
		setDefaultVisitor(new EscapingDynamicQueriesASTVisitor());
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
				"		Codec<Character> ORACLE_CODEC = new OracleCodec();\n" +
				"		String query = \"SELECT user_id FROM user_data WHERE user_name = '\" + \n" +
				"				ESAPI.encoder().encodeForSQL(ORACLE_CODEC, req.getParameter(\"userID\")) + \n" +
				"				\"' and user_password = '\" + \n" +
				"				ESAPI.encoder().encodeForSQL(ORACLE_CODEC, req.getParameter(\"pwd\")) + \n" +
				"				\"'\";\n" +
				tryExecute("query") +
				"	}";

		assertChange(original, expected);
		assertTrue(defaultFixture.getImports()
			.stream()
			.anyMatch(t -> t.getName()
				.getFullyQualifiedName()
				.equals("org.owasp.esapi.ESAPI")));
		assertTrue(defaultFixture.getImports()
			.stream()
			.anyMatch(t -> t.getName()
				.getFullyQualifiedName()
				.equals("org.owasp.esapi.codecs.Codec")));
		assertTrue(defaultFixture.getImports()
			.stream()
			.anyMatch(t -> t.getName()
				.getFullyQualifiedName()
				.equals("org.owasp.esapi.codecs.OracleCodec")));
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
				"		Codec<Character> ORACLE_CODEC = new OracleCodec();\n" +
				"		String query = \"SELECT user_id FROM user_data WHERE user_name = '\" + \n" +
				"				ESAPI.encoder().encodeForSQL(ORACLE_CODEC, userName) + \n" +
				"				\"' and user_password = '\" + \n" +
				"				ESAPI.encoder().encodeForSQL(ORACLE_CODEC, userPWD) + \n" +
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
				"		Codec<Character> ORACLE_CODEC = new OracleCodec();\n" +
				"		String query = \"SELECT user_id FROM user_data WHERE user_name = '\" + \n" +
				"				ESAPI.encoder().encodeForSQL(ORACLE_CODEC, this.userName) + \n" +
				"				\"' and user_password = '\" + \n" +
				"				ESAPI.encoder().encodeForSQL(ORACLE_CODEC, this.userPWD) + \n" +
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
				"		Codec<Character> ORACLE_CODEC = new OracleCodec();\n" +
				"		String query = " +
				"			\"SELECT user_id FROM user_data WHERE user_name = '\" + " +
				"			ESAPI.encoder().encodeForSQL(ORACLE_CODEC, userName) + " +
				"			\"'\";\n" +
				"		String query1 = " +
				"			\"SELECT user_id FROM user_data WHERE user_name = '\" + " +
				"			ESAPI.encoder().encodeForSQL(ORACLE_CODEC, userName) + " +
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
				"			Codec<Character> ORACLE_CODEC = new OracleCodec();\n" +
				"			String query = " +
				"				\"SELECT user_id FROM user_data WHERE user_name = '\" + " +
				"				ESAPI.encoder().encodeForSQL(ORACLE_CODEC, userName) + " +
				"				\"'\";\n" +
				tryExecute("query") +
				"		}\n" +
				"		{\n" +
				"			Codec<Character> ORACLE_CODEC1 = new OracleCodec();\n" +
				"			String query1 = " +
				"				\"SELECT user_id FROM user_data WHERE user_name = '\" + " +
				"				ESAPI.encoder().encodeForSQL(ORACLE_CODEC1, userName) + " +
				"				\"'\";\n" +
				tryExecute("query1") +
				"		}\n" +
				"	}";

		assertChange(original, expected);
	}

	@Test
	public void visit_ORACLE_CODECAsIntLocalVariable_shouldTransform() throws Exception {
		String original = "" +
				"	public void test() {\n" +
				"		int ORACLE_CODEC = 0;\n" +
				"		String userName = \"userName\";\n" +
				"		String query = " +
				"			\"SELECT user_id FROM user_data WHERE user_name = '\" + userName + \"'\";\n" +
				tryExecute("query") +
				"		}";
		String expected = "" +
				"	public void test() {\n" +
				"		int ORACLE_CODEC = 0;\n" +
				"		String userName = \"userName\";\n" +
				"		Codec<Character> ORACLE_CODEC1 = new OracleCodec();\n" +
				"		String query = " +
				"			\"SELECT user_id FROM user_data WHERE user_name = '\" + " +
				"			ESAPI.encoder().encodeForSQL(ORACLE_CODEC1, userName) + " +
				"			\"'\";\n" +
				tryExecute("query") +
				"		}";
		assertChange(original, expected);
	}

	@Test
	public void visit_ORACLE_CODECAsInnerClass_shouldTransform() throws Exception {
		String original = "" +
				"               class ORACLE_CODEC {\n" +
				"               }\n" +
				"               public void test() {\n" +
				"                       String userName = \"userName\";\n" +
				"                       String query = \n" +
				"                                       \"SELECT user_id FROM user_data WHERE user_name = '\" + userName  + \"'\";                      \n"
				+
				"                       try {\n" +
				"                               Connection connection = null;\n" +
				"                               Statement statement = connection.createStatement();\n" +
				"                               ResultSet results = statement.executeQuery(query);\n" +
				"                       } catch (SQLException e) {\n" +
				"                               e.printStackTrace();\n" +
				"                       }\n" +
				"               }";
		String expected = "" +
				"               class ORACLE_CODEC {\n" +
				"               }\n" +
				"               public void test() {\n" +
				"                       String userName = \"userName\";\n" +
				"                       Codec<Character> ORACLE_CODEC1 = new OracleCodec();\n" +
				"                       String query = \n" +
				"                                       \"SELECT user_id FROM user_data WHERE user_name = '\" + ESAPI.encoder().encodeForSQL(ORACLE_CODEC1, userName)  + \"'\";                 \n"
				+
				"                       try {\n" +
				"                               Connection connection = null;\n" +
				"                               Statement statement = connection.createStatement();\n" +
				"                               ResultSet results = statement.executeQuery(query);\n" +
				"                       } catch (SQLException e) {\n" +
				"                               e.printStackTrace();\n" +
				"                       }\n" +
				"               }" +
				"";
		assertChange(original, expected);
	}

	@Test
	public void visit_ORACLE_CODECAsImportedClass_shouldTransform() throws Exception {
		defaultFixture.addImport("examplePackage.ORACLE_CODEC");
		String original = "" +
				"               public void test() {\n" +
				"                       String userName = \"userName\";\n" +
				"                       String query = \n" +
				"                                       \"SELECT user_id FROM user_data WHERE user_name = '\" + userName  + \"'\";                      \n"
				+
				"                       try {\n" +
				"                               Connection connection = null;\n" +
				"                               Statement statement = connection.createStatement();\n" +
				"                               ResultSet results = statement.executeQuery(query);\n" +
				"                       } catch (SQLException e) {\n" +
				"                               e.printStackTrace();\n" +
				"                       }\n" +
				"               }";
		String expected = "" +
				"               public void test() {\n" +
				"                       String userName = \"userName\";\n" +
				"                       Codec<Character> ORACLE_CODEC1 = new OracleCodec();\n" +
				"                       String query = \n" +
				"                                       \"SELECT user_id FROM user_data WHERE user_name = '\" + ESAPI.encoder().encodeForSQL(ORACLE_CODEC1, userName)  + \"'\";                 \n"
				+
				"                       try {\n" +
				"                               Connection connection = null;\n" +
				"                               Statement statement = connection.createStatement();\n" +
				"                               ResultSet results = statement.executeQuery(query);\n" +
				"                       } catch (SQLException e) {\n" +
				"                               e.printStackTrace();\n" +
				"                       }\n" +
				"               }" +
				"";
		assertChange(original, expected);
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
				"		Codec<Character> ORACLE_CODEC=new OracleCodec();\n" +
				"		String query = \n" +
				"				\"SELECT user_id FROM user_data WHERE user_name = '\" + \n" +
				"				org.owasp.esapi.ESAPI.encoder().encodeForSQL(ORACLE_CODEC,userName) + \n" +
				"				\"'\";		\n" +
				tryExecute("query") +
				"	}";
		assertChange(original, expected);
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
				"		Codec<Character> ORACLE_CODEC=new OracleCodec();\n" +
				"		String query = \n" +
				"				\"SELECT user_id FROM user_data WHERE user_name = '\" + \n" +
				"				org.owasp.esapi.ESAPI.encoder().encodeForSQL(ORACLE_CODEC,userName) + \n" +
				"				\"'\";\n" +
				tryExecute("query") +
				"	}";
		assertChange(original, expected);
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
				"		Codec<Character> ORACLE_CODEC=new OracleCodec();\n" +
				"		String query = \n" +
				"				\"SELECT user_id FROM user_data WHERE user_name = '\" + \n" +
				"				org.owasp.esapi.ESAPI.encoder().encodeForSQL(ORACLE_CODEC,userName) + \n" +
				"				\"'\";		\n" +
				tryExecute("query") +
				"	}";

		assertChange(original, expected);
	}

}
