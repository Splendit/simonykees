package eu.jsparrow.core.visitor.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.common.UsesSimpleJDTUnitFixture;

class UseParameterizedQueryNegativeASTVisitorTest extends UsesSimpleJDTUnitFixture {

	@BeforeEach
	public void setUpVisitor() throws Exception {
		setVisitor(new UseParameterizedQueryASTVisitor());
		fixture.addImport(java.sql.Connection.class.getName());
		fixture.addImport(java.sql.Statement.class.getName());
		fixture.addImport(java.sql.ResultSet.class.getName());
	}

	/*
	 * SQL query variable tests
	 */
	@Test
	void test_missingParameters_shouldNotTransform() throws Exception {
		String original = "" +
				"Connection connection = null;\n" +
				"String query = \"SELECT first_name FROM employee WHERE department_id ='40' ORDER BY last_name\";\n" +
				"try {\n" +
				"    Statement statement = connection.createStatement();\n" +
				"    statement.executeQuery(query);\n" +
				"} catch (Exception e) {}";
		assertNoChange(original);
	}

	@Test
	void test_missingQueryDeclaration_shouldNotTransform() throws Exception {
		String original = "" +
				"class Foo {\n" +
				"    public void runQuery(String query, Connection connection, String departmentId) throws Exception {\n"
				+
				"        Statement statement = connection.createStatement();\n" +
				"        statement.executeQuery(query);\n" +
				"    }\n" +
				"}";
		assertNoChange(original);
	}

	@Test
	void test_multipleQueryUsages_shouldNotTransform() throws Exception {
		String original = "" +
				"String departmentId = \"40\";\n" +
				"Connection connection = null;\n" +
				"String query = \"SELECT first_name FROM employee WHERE department_id ='\" + departmentId + \"' ORDER BY last_name\";\n"
				+
				"System.out.println(query);\n" +
				"try {\n" +
				"    Statement statement = connection.createStatement();\n" +
				"    statement.executeQuery(query);\n" +
				"} catch (Exception e) {}";
		assertNoChange(original);
	}

	@Test
	void visit_usingFieldAsQuery_shouldNotTransform() throws Exception {
		String original = "" +
				"class Foo {\n" +
				"    String departmentId = \"40\";\n" +
				"    String query = \"SELECT first_name FROM employee WHERE department_id ='\" + departmentId + \"' ORDER BY last_name\";\n"
				+
				"    public void runQuery(Connection connection, String departmentId) throws Exception {\n" +
				"        Statement statement = connection.createStatement();\n" +
				"        statement.executeQuery(query);\n" +
				"    }\n" +
				"}";
		assertNoChange(original);
	}

	@Test
	void visit_consecutiveVariablesAsQueryComponents_shouldNotTransform() throws Exception {
		String original = "" +
				"String departmentId = \"40\";\n" +
				"Connection connection = null;\n" +
				"String zero = \"0\";\n" +
				"String query = \"SELECT first_name FROM employee WHERE department_id ='\" + departmentId + zero + \"' ORDER BY last_name\";\n"
				+
				"try {\n" +
				"    Statement statement = connection.createStatement();\n" +
				"    statement.executeQuery(query);\n" +
				"} catch (Exception e) {}";
		assertNoChange(original);
	}

	@Test
	void visit_queryComponentsStartingWithVariable_shouldNotTransform() throws Exception {
		String original = "" +
				"String departmentId = \"40\";\n" +
				"Connection connection = null;\n" +
				"String select = \"SELECT first_name FROM employee WHERE department_id ='\";\n" +
				"String query = select + departmentId + \"' ORDER BY last_name\";\n" +
				"try {\n" +
				"    Statement statement = connection.createStatement();\n" +
				"    statement.executeQuery(query);\n" +
				"} catch (Exception e) {}";
		assertNoChange(original);
	}

	@Test
	void visit_variableAsLastQueryComponent_shouldNotTransform() throws Exception {
		String original = "" +
				"String departmentId = \"40\";\n" +
				"Connection connection = null;\n" +
				"String orderBy = \"' ORDER BY last_name\";\n" +
				"String query = \"SELECT first_name FROM employee WHERE department_id ='\" + departmentId + orderBy;\n"
				+
				"try {\n" +
				"    Statement statement = connection.createStatement();\n" +
				"    statement.executeQuery(query);\n" +
				"} catch (Exception e) {}";
		assertNoChange(original);
	}

	/*
	 * SQL Statement tests
	 */
	@Test
	void test_storingExecuteResult_shouldNotTransform() throws Exception {
		String original = "" +
				"String departmentId = \"40\";\n" +
				"Connection connection = null;\n" +
				"String query = \"SELECT first_name FROM employee WHERE department_id ='\" + departmentId + \"' ORDER BY last_name\";\n"
				+
				"try {\n" +
				"    Statement statement = connection.createStatement();\n" +
				"    boolean b = statement.execute(query);\n" +
				"} catch (Exception e) {}";
		assertNoChange(original);
	}

	@Test
	void test_multipleExecutionOfSameQuery_shouldNotTransform() throws Exception {
		String original = "" +
				"String departmentId = \"40\";\n" +
				"Connection connection = null;\n" +
				"String query = \"SELECT first_name FROM employee WHERE department_id ='\" + departmentId + \"' ORDER BY last_name\";\n"
				+
				"try {\n" +
				"    Statement statement = connection.createStatement();\n" +
				"    statement.executeQuery(query);\n" +
				"    statement.execute(query);\n" +
				"} catch (Exception e) {}";
		assertNoChange(original);
	}

	@Test
	void test_wrongConnectionType_shouldNotTransform() throws Exception {
		String original = "" +
				"class Foo {\n" +
				"    public void runQuery(String departmentId) throws Exception {\n" +
				"        String query = \"SELECT first_name FROM employee WHERE department_id ='\" + departmentId + \"' ORDER BY last_name\";\n"
				+
				"        Statement statement = getConnection().createStatement();\n" +
				"        statement.executeQuery(query);\n" +
				"    }\n" +
				"    public FakeConnection getConnection() {\n" +
				"        return new FakeConnection();\n" +
				"    }\n" +
				"    class FakeConnection {\n" +
				"        Statement createStatement(String query) {\n" +
				"            return null;\n" +
				"        }\n" +
				"    }\n" +
				"}";
		assertNoChange(original);
	}

	@Test
	void visit_missingConnectionCreateStatementInitializer_shouldNotTransform() throws Exception {
		String original = "" +
				"class Foo {\n" +
				"    public void runQuery(Statement statement, String departmentId) throws Exception {\n" +
				"        String query = \"SELECT first_name FROM employee WHERE department_id ='\" + departmentId + \"' ORDER BY last_name\";\n"
				+
				"        statement.executeQuery(query);\n" +
				"    }\n" +
				"} ";
		assertNoChange(original);
	}

	@Test
	void visit_sqlStatementAssignmentRHS_shouldNotTransform() throws Exception {
		String original = "" +
				"class Foo {\n" +
				"    public void runQuery(Connection connection, String departmentId) throws Exception {\n" +
				"        String query = \"SELECT first_name FROM employee WHERE department_id ='\" + departmentId + \"' ORDER BY last_name\";\n"
				+
				"        Statement statement = connection.createStatement();\n" +
				"        Statement statement1;\n" +
				"        statement1 = statement;\n" +
				"        statement.executeQuery(query);\n" +
				"    }\n" +
				"}";
		assertNoChange(original);
	}

	@Test
	void test_multipleStatementUsages_shouldNotTransform() throws Exception {
		String original = "" +
				"String departmentId = \"40\";\n" +
				"Connection connection = null;\n" +
				"String query = \"SELECT first_name FROM employee WHERE department_id ='\" + departmentId + \"' ORDER BY last_name\";\n"
				+
				"try {\n" +
				"    Statement statement = connection.createStatement(), statement1 = statement;\n" +
				"    statement.executeQuery(query);\n" +
				"} catch (Exception e) {}";
		assertNoChange(original);
	}

	@Test
	void visit_nonSimpleNameExpression_shouldNotTransform() throws Exception {
		String original = "" +
				"class Foo {\n" +
				"    public void runQuery(Connection connection, String departmentId) throws Exception {\n" +
				"        String query = \"SELECT first_name FROM employee WHERE department_id ='\" + departmentId + \"' ORDER BY last_name\";\n"
				+
				"        Statement statement = connection.createStatement();\n" +
				"        statement().executeQuery(query);\n" +
				"    }\n" +
				"    public Statement statement() {\n" +
				"        return null;\n" +
				"    }\n" +
				"}";
		assertNoChange(original);
	}

	@Test
	void visit_simpleNameSqlStatementInitializer_shouldNotTransform() throws Exception {
		String original = "" +
				"class Foo {\n" +
				"    public void runQuery(Statement statement2, Connection connection, String departmentId) throws Exception {\n"
				+
				"        String query = \"SELECT first_name FROM employee WHERE department_id ='\" + departmentId + \"' ORDER BY last_name\";\n"
				+
				"        Statement statement = statement2;\n" +
				"        statement.executeQuery(query);\n" +
				"    }\n" +
				"}";
		assertNoChange(original);
	}

	@Test
	void visit_missingExpressionInExecuteQuery_shouldNotTransform() throws Exception {
		String original = "" +
				"class FakeSqlStatement {\n" +
				"    public void runQuery(Connection connection, String departmentId) {\n" +
				"        String query = \"SELECT first_name FROM employee WHERE department_id ='\" + departmentId + \"' ORDER BY last_name\";\n"
				+
				"        executeQuery(query);\n" +
				"    }\n" +
				"    public ResultSet executeQuery(String query) {\n" +
				"        return null;\n" +
				"    }\n" +
				"}";
		assertNoChange(original);
	}

	@Test
	void visit_reassignmentOfSqlStatement_shouldNotTransform() throws Exception {
		String original = "" +
				"Connection connection = null;\n" +
				"long id = 40;\n" +
				"String query = \"SELECT first_name FROM employee WHERE department_id ='\" + id + \"' ORDER BY last_name\";\n"
				+
				"try {\n" +
				"    Statement statement = connection.createStatement();\n" +
				"    statement = null;\n" +
				"    statement.executeQuery(query);\n" +
				"} catch (Exception e) {}";
		assertNoChange(original);
	}

	/*
	 * getResultSet tests
	 */

	@Test
	void test_multipleGetResultSet_shouldNotTransform() throws Exception {
		String original = "" +
				"String departmentId = \"40\";\n" +
				"Connection connection = null;\n" +
				"String query = \"SELECT first_name FROM employee WHERE department_id ='\" + departmentId + \"' ORDER BY last_name\";\n"
				+
				"try {\n" +
				"    Statement statement = connection.createStatement();\n" +
				"    statement.execute(query);\n" +
				"    ResultSet rs = statement.getResultSet();\n" +
				"    statement.getResultSet();\n" +
				"} catch (Exception e) {}";
		assertNoChange(original);
	}

	@Test
	void visit_usingGetResultSetAsParameter_shouldNotTransform() throws Exception {
		String original = "" +
				"class Foo {\n" +
				"    public void runQuery(Connection connection, String departmentId) throws Exception {\n" +
				"        String query = \"SELECT first_name FROM employee WHERE department_id ='\" + departmentId + \"' ORDER BY last_name\";\n"
				+
				"        Statement statement = connection.createStatement();\n" +
				"        statement.execute(query);\n" +
				"        useResultSet(statement.getResultSet());\n" +
				"        \n" +
				"    }\n" +
				"    public void useResultSet(ResultSet resultSet) {}\n" +
				"}";
		assertNoChange(original);
	}

	@Test
	void test_conflictingRSVariableNames_shouldNotTransform() throws Exception {
		String original = "" +
				"String departmentId = \"40\";\n" +
				"Connection connection = null;\n" +
				"String query = \"SELECT first_name FROM employee WHERE department_id ='\" + departmentId + \"' ORDER BY last_name\";\n"
				+
				"try {\n" +
				"    Statement statement = connection.createStatement();\n" +
				"    statement.execute(query);\n" +
				"    if(false) {\n" +
				"        ResultSet rs = null;\n" +
				"    } else {\n" +
				"        ResultSet rs = statement.getResultSet();\n" +
				"    }\n" +
				"} catch (Exception e) {}";
		assertNoChange(original);
	}

	@Test
	void visit_singleVariableDeclarationGetResultSet_shouldNotTransform() throws Exception {
		String original = "" +
				"class Foo {\n" +
				"    public void runQuery(Connection connection, String departmentId) throws Exception {\n" +
				"        String query = \"SELECT first_name FROM employee WHERE department_id ='\" + departmentId + \"' ORDER BY last_name\";\n"
				+
				"        Statement statement = connection.createStatement();\n" +
				"        statement.executeQuery(query);\n" +
				"        for (ResultSet rs = statement.getResultSet(); ; ) {\n" +
				"            break;\n" +
				"        }\n" +
				"    }\n" +
				"}";
		assertNoChange(original);
	}

	@Test
	void visit_discardedGetResultSet_shouldNotTransform() throws Exception {
		String original = "" +
				"class Foo {\n" +
				"    public void runQuery(Connection connection, String departmentId) throws Exception {\n" +
				"        String query = \"SELECT first_name FROM employee WHERE department_id ='\" + departmentId + \"' ORDER BY last_name\";\n"
				+
				"        Statement statement = connection.createStatement();\n" +
				"        statement.execute(query);\n" +
				"        statement.getResultSet();\n" +
				"        statement.getResultSet();\n" +
				"    }\n" +
				"}";
		assertNoChange(original);
	}

	@Test
	void visit_executeQueryAndGetResultSet_shouldNotTransform() throws Exception {
		String original = "" +
				"class Foo {\n" +
				"    public void runQuery(Connection connection, String departmentId) throws Exception {\n" +
				"        String query = \"SELECT first_name FROM employee WHERE department_id ='\" + departmentId + \"' ORDER BY last_name\";\n"
				+
				"        Statement statement = connection.createStatement();\n" +
				"        statement.executeQuery(query);\n" +
				"        statement.getResultSet();\n" +
				"    }\n" +
				"}";
		assertNoChange(original);
	}

	@Test
	void visit_ExecuteQueryArgumentAsInfixExpression_shouldNotTransform() throws Exception {

		String original = "" +
				"String userID = \"100000\";\n" +
				"String query = \n" +
				"	\"SELECT user_name FROM user_data WHERE user_id = '\" + \n" +
				"	userID + \n" +
				"	\"'\";\n" +
				"try {\n" +
				"	Connection connection = null;\n" +
				"	Statement statement = connection.createStatement();\n" +
				"	ResultSet resultset = statement.executeQuery(query + \" ORDER BY last_name\");\n" +
				"} catch (SQLException e) {\n" +
				"	e.printStackTrace();\n" +
				"}";

		assertNoChange(original);
	}

	@Test
	void visit_WithStatementAsLocalClass_shouldNotTransform() throws Exception {

		String original = "" +
				"String userID = \"100000\";\n" +
				"String query = \n" +
				"	\"SELECT user_name FROM user_data WHERE user_id = '\" + \n" +
				"	userID + \n" +
				"	\"'\";\n" +
				"class Statement {\n" +
				"	boolean execute(String query) {\n" +
				"		return false;\n" +
				"	}\n" +
				"}\n" +
				"try {\n" +
				"	Statement statement = new Statement();\n" +
				"	statement.execute(query);\n" +
				"} catch (Exception e) {\n" +
				"}";
		assertNoChange(original);
	}

	@Test
	void visit_TwoExecuteArguments_shouldNotTransform() throws Exception {

		String original = "" +
				"String userID = \"100000\";\n" +
				"String query = \n" +
				"	\"SELECT user_name FROM user_data WHERE user_id = '\" + \n" +
				"	userID + \n" +
				"	\"'\";\n" +
				"try {\n" +
				"	Connection connection = null;\n" +
				"	Statement statement = connection.createStatement();\n" +
				"	statement.execute(query, 1);\n" +
				"} catch (SQLException e) {\n" +
				"	e.printStackTrace();\n" +
				"}";
		assertNoChange(original);
	}

	@Test
	void visit_AssignmentToStatementUsedByMethod_shouldNotTransform() throws Exception {
		String original = "" +
				"class TestVariablesBefore {\n" +
				"	void useStatement(Statement statement) {\n" +
				"	}\n" +
				"	void test() {\n" +
				"		String departmentId1 = \"40\";\n" +
				"		String departmentId2 = \"140\";\n" +
				"		Connection connection = null;\n" +
				"		String query = \"SELECT employee_id, first_name FROM employee WHERE department_id ='\" + departmentId1 + \"'\" + //\n"
				+
				"				\" OR department_id ='\" + departmentId2 + \"'\" + //\n" +
				"				\" ORDER BY last_name\";\n" +
				"		Statement statement;\n" +
				"		try {\n" +
				"			useStatement(statement = connection.createStatement());\n" +
				"			ResultSet resultSet = statement.executeQuery(query);\n" +
				"		} catch (Exception e) {\n" +
				"		}\n" +
				"	}\n" +
				"}";
		assertNoChange(original);
	}

	@Test
	void visit_StatementContainingExecuteQueryNotInBlock_shouldNotTransform() throws Exception {
		String original = "" +
				"class TestStatementContainingExecuteQueryNotInBlock {\n" +
				"	void test() {\n" +
				"		Connection connection = null;\n" +
				"		Statement statement;\n" +
				"		ResultSet resultSet;\n" +
				"		try {\n" +
				"			statement = connection.createStatement();\n" +
				"			String departmentId1 = \"40\";\n" +
				"			String query = \"\" + \"SELECT employee_id FROM employee WHERE department_id = '\" + departmentId1 + \"'\";\n"
				+
				"			query += \" OR department_id = '\";\n" +
				"			String departmentId2 = \"140\";\n" +
				"			query += departmentId2;\n" +
				"			query += \"'\";\n" +
				"			if (true) resultSet = statement.executeQuery(query);\n" +
				"		} catch (Exception e) {\n" +
				"		}\n" +
				"	}\n" +
				"}";

		assertNoChange(original);
	}

	@Test
	void visit_ConnectionCreateStatementUsedByMethod_shouldNotTransform() throws Exception {
		String original = "" +
				"class TestVariablesBefore {\n" +
				"	Statement useStatement(Statement statement) {\n" +
				"		return statement; \n" +
				"	}\n" +
				"	void test() {\n" +
				"		String departmentId1 = \"40\";\n" +
				"		String departmentId2 = \"140\";\n" +
				"		Connection connection = null;\n" +
				"		String query = \"SELECT employee_id, first_name FROM employee WHERE department_id ='\" + departmentId1 + \"'\" + //\n"
				+
				"				\" OR department_id ='\" + departmentId2 + \"'\" + //\n" +
				"				\" ORDER BY last_name\";\n" +
				"		Statement statement;\n" +
				"		try {\n" +
				"			statement = useStatement(connection.createStatement());\n" +
				"			ResultSet resultSet = statement.executeQuery(query);\n" +
				"		} catch (Exception e) {\n" +
				"		}\n" +
				"	}\n" +
				"}";
		assertNoChange(original);
	}

	@Test
	void visit_prepareCallInsteadOfCreateStatement_shouldNotTransform() throws Exception {
		String original = "" +
				"class Test_prepareCallInsteadCreateStatement {\n" +
				"	void test() {\n" +
				"		Connection connection = null;\n" +
				"		Statement statement;\n" +
				"		try {\n" +
				"			\n" +
				"			String departmentId1 = \"40\";\n" +
				"			String query = \"\" + \"SELECT employee_id FROM employee WHERE department_id = '\" + departmentId1 + \"'\";\n"
				+
				"			query += \" OR department_id = '\";\n" +
				"			String departmentId2 = \"140\";\n" +
				"			query += departmentId2;\n" +
				"			query += \"'\";\n" +
				"			statement = connection.prepareCall(\"\");\n" +
				"			ResultSet resultSet = statement.executeQuery(query);\n" +
				"		} catch (Exception e) {\n" +
				"		}\n" +
				"	}\n" +
				"}";
		assertNoChange(original);
	}

	@Test
	void visit_createStatementWithArguments_shouldNotTransform() throws Exception {
		String original = "" +
				"class Test_createStatementWithArguments {\n" +
				"	void test() {\n" +
				"		Connection connection = null;\n" +
				"		Statement statement;\n" +
				"		try {				\n" +
				"			String departmentId1 = \"40\";\n" +
				"			String query = \"\" + \"SELECT employee_id FROM employee WHERE department_id = '\" + departmentId1 + \"'\";\n"
				+
				"			query += \" OR department_id = '\";\n" +
				"			String departmentId2 = \"140\";\n" +
				"			query += departmentId2;\n" +
				"			query += \"'\";\n" +
				"			statement = connection.createStatement(0, 0);\n" +
				"			ResultSet resultSet = statement.executeQuery(query);\n" +
				"		} catch (Exception e) {\n" +
				"		}\n" +
				"	}\n" +
				"}";
		assertNoChange(original);
	}

	@Test
	void visit_referencingStatementBeforeUsage_shouldNotTransform() throws Exception {
		String original = "" +
				"Connection connection = null;\n" +
				"try {\n" +
				"	Statement statement = connection.createStatement();\n" +
				"	String departmentId = \"40\";\n" +
				"	statement.equals(null);\n" +
				"    String query = \"SELECT employee_id, first_name FROM employee WHERE department_id = '\" + departmentId + \"'\";\n"
				+
				"    ResultSet resultSet = statement.executeQuery(query);\n" +
				"} catch (Exception e) {\n" +
				"	\n" +
				"}";
		assertNoChange(original);
	}

	@Test
	void visit_executeAfterExecuteQuery_shouldNotTransform() throws Exception {
		String original = "" +
				"			Connection connection = null;\n" +
				"			Statement statement;\n" +
				"			String departmentId1 = \"40\";\n" +
				"			String query = \"SELECT id FROM employee WHERE department_id = '\" + departmentId1 + \"'\";\n"
				+
				"			String query2 = \"SELECT id FROM employee WHERE department_id = '\" + departmentId1 + \"'\";\n"
				+
				"			try {\n" +
				"				statement = connection.createStatement();\n" +
				"				statement.executeQuery(query);\n" +
				"				statement.execute(query2);\n" +
				"			} catch (Exception e) {\n" +
				"			}";
		assertNoChange(original);
	}

	@Test
	void visit_ConditionalPlusAssign_shouldNotTransform() throws Exception {
		String original = "" +
				"		Connection connection = null;\n" +
				"		String query = \"SELECT employee_id, first_name FROM employee WHERE\";\n" +
				"		if(true){\n" +
				"			String departmentId1 = \"40\";\n" +
				"			query += \" department_id ='\" + departmentId1 + \"'\";\n" +
				"		}			\n" +
				"		try {\n" +
				"			Statement statement = connection.createStatement();\n" +
				"			ResultSet resultSet = statement.executeQuery(query);\n" +
				"			while (resultSet.next()) {\n" +
				"				String firstName = resultSet.getString(2);\n" +
				"			}\n" +
				"		} catch (Exception e) {\n" +
				"\n" +
				"		}";
		assertNoChange(original);
	}

	@Test
	void visit_ConditionalPlusAssignNotWithinBlock_shouldNotTransform() throws Exception {
		String original = "" +
				"		Connection connection = null;\n" +
				"		String query = \"SELECT employee_id, first_name FROM employee WHERE\";\n" +
				"		String departmentId1 = \"40\";\n" +
				"		if (true)\n" +
				"			query += \" department_id ='\" + departmentId1 + \"'\";\n" +
				"		try {\n" +
				"			Statement statement = connection.createStatement();\n" +
				"			ResultSet resultSet = statement.executeQuery(query);\n" +
				"			while (resultSet.next()) {\n" +
				"				String firstName = resultSet.getString(2);\n" +
				"			}\n" +
				"		} catch (Exception e) {\n" +
				"\n" +
				"		}";
		assertNoChange(original);
	}

	@Test
	void visit_AssignmentAsOtherAssignmentLeftHandSide_shouldNotTransform() throws Exception {
		String original = "" +
				"		Connection connection = null;\n" +
				"		String departmentId1 = \"40\";\n" +
				"		String query0; \n" +
				"		String query1; \n" +
				"		query0 = query1 = \"SELECT employee_id, first_name FROM employee WHERE department_id ='\" + departmentId1 + \"'\";\n"
				+
				"		try {\n" +
				"			Statement statement = connection.createStatement();\n" +
				"			ResultSet resultSet = statement.executeQuery(query1);\n" +
				"			while (resultSet.next()) {\n" +
				"				String firstName = resultSet.getString(2);\n" +
				"			}\n" +
				"		} catch (Exception e) {\n" +
				"\n" +
				"		}";
		assertNoChange(original);
	}
}
