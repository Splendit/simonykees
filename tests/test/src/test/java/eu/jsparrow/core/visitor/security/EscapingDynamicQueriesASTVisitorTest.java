package eu.jsparrow.core.visitor.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.core.visitor.impl.UsesSimpleJDTUnitFixture;

@SuppressWarnings("nls")
public class EscapingDynamicQueriesASTVisitorTest extends UsesSimpleJDTUnitFixture {

	private static final String TRY_EXECUTING_QUERY = "" +
			"		Connection connection = null;\n" +
			"		Statement statement;\n" +
			"		try {\n" +
			"			statement=connection.createStatement();\n" +
			"			ResultSet resultSet=statement.executeQuery(query);\n" +
			"		}\n" +
			"		catch (Exception e) {\n" +
			"			return;\n" +
			"		}";

	@BeforeEach
	public void setUpVisitor() throws Exception {
		setVisitor(new EscapingDynamicQueriesASTVisitor());
		fixture.addImport(java.sql.Connection.class.getName());
		fixture.addImport(java.sql.Statement.class.getName());
		fixture.addImport(java.sql.PreparedStatement.class.getName());
		fixture.addImport(java.sql.ResultSet.class.getName());
	}

	@Test
	public void visit_DepartmentIDToEscape_shouldTransform() throws Exception {

		String original = "" +
				"		String departmentId = \"40\";\n" +
				"		String query = " +
				"			\"SELECT employee_id, first_name FROM employee WHERE department_id ='\" + " +
				"			departmentId + " +
				"			\"' ORDER BY last_name\";\n" +
				TRY_EXECUTING_QUERY;

		String expected = "" + //
				"		String departmentId=\"40\";\n" +
				"		Codec<Character> ORACLE_CODEC=new OracleCodec();\n" +
				"		String query = " +
				"			\"SELECT employee_id, first_name FROM employee WHERE department_id ='\" + " +
				"			ESAPI.encoder().encodeForSQL(ORACLE_CODEC,departmentId) + " +
				"			\"' ORDER BY last_name\";\n" +
				TRY_EXECUTING_QUERY;

		assertChange(original, expected);
	}

	@Test
	public void visit_RequestParametersToEscape_shouldTransform() throws Exception {

		fixture.addImport(javax.servlet.http.HttpServletRequest.class.getName());

		String original = "" +
				"		HttpServletRequest req = null;\n" +
				"		String query = \"SELECT user_id FROM user_data WHERE user_name = '\" +\n" +
				"				req.getParameter(\"userID\") +\n" +
				"				\"' and user_password = '\" +\n" +
				"				req.getParameter(\"pwd\") +\n" +
				"				\"'\";" +
				TRY_EXECUTING_QUERY;

		String expected = "" +
				"		HttpServletRequest req=null;\n" +
				"		Codec<Character> ORACLE_CODEC=new OracleCodec();\n" +
				"		String query=\"SELECT user_id FROM user_data WHERE user_name = '\" +" +
				"			ESAPI.encoder().encodeForSQL(ORACLE_CODEC,req.getParameter(\"userID\")) + " +
				"			\"' and user_password = '\"+ " +
				"			ESAPI.encoder().encodeForSQL(ORACLE_CODEC,req.getParameter(\"pwd\"))+ " +
				"			\"'\";\n" +
				TRY_EXECUTING_QUERY;

		assertChange(original, expected);

	}

	@Test
	public void visit_ToQueriesWithParametersToEscape_shouldTransform() throws Exception {
		String original = "" + //
				"		Connection connection = null;\n" +
				"		HttpServletRequest req = null;\n" +
				"		String query = \"SELECT user_id FROM user_data WHERE user_name = '\" +\n" +
				"				req.getParameter(\"userID\") +\n" +
				"				\"' and user_password = '\" +\n" +
				"				req.getParameter(\"pwd\") +\n" +
				"				\"'\";\n" +
				"		String query1 = \"SELECT user_id FROM user_data WHERE user_name = '\"\n" +
				"				+ req.getParameter(\"userID\")\n" +
				"				+ \"' and user_password = '\" +\n" +
				"				req.getParameter(\"pwd\") +\n" +
				"				\"'\";\n" +
				"		Statement statement;\n" +
				"		ResultSet results;\n" +
				"		try {\n" +
				"			statement = connection.createStatement();\n" +
				"			results = statement.executeQuery(query);\n" +
				"			statement = connection.createStatement();\n" +
				"			results = statement.executeQuery(query1);\n" +
				"		} catch (SQLException e) {\n" +
				"			e.printStackTrace();\n" +
				"		}";

		String expected = "" + //
				"		Connection connection = null;\n" +
				"		HttpServletRequest req = null;\n" +
				"		Codec<Character> ORACLE_CODEC = new OracleCodec();\n" +
				"		String query = \"SELECT user_id FROM user_data WHERE user_name = '\" +\n" +
				"				ESAPI.encoder()\n" +
				"					.encodeForSQL(ORACLE_CODEC, req.getParameter(\"userID\"))\n" +
				"				+\n" +
				"				\"' and user_password = '\" +\n" +
				"				ESAPI.encoder()\n" +
				"					.encodeForSQL(ORACLE_CODEC, req.getParameter(\"pwd\"))\n" +
				"				+\n" +
				"				\"'\";\n" +
				"		Codec<Character> ORACLE_CODEC1 = new OracleCodec();\n" +
				"		String query1 = \"SELECT user_id FROM user_data WHERE user_name = '\" +\n" +
				"				ESAPI.encoder()\n" +
				"					.encodeForSQL(ORACLE_CODEC1, req.getParameter(\"userID\"))\n" +
				"				+\n" +
				"				\"' and user_password = '\" +\n" +
				"				ESAPI.encoder()\n" +
				"					.encodeForSQL(ORACLE_CODEC1, req.getParameter(\"pwd\"))\n" +
				"				+\n" +
				"				\"'\";\n" +
				"		Statement statement;\n" +
				"		ResultSet results;\n" +
				"		try {\n" +
				"			statement = connection.createStatement();\n" +
				"			results = statement.executeQuery(query);\n" +
				"			statement = connection.createStatement();\n" +
				"			results = statement.executeQuery(query1);\n" +
				"		} catch (SQLException e) {\n" +
				"			e.printStackTrace();\n" +
				"		}\n" +
				"";

		assertChange(original, expected);

	}

}
