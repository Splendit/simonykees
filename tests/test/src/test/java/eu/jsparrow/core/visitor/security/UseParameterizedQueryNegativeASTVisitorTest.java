package eu.jsparrow.core.visitor.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.core.visitor.impl.UsesSimpleJDTUnitFixture;

@SuppressWarnings("nls")
public class UseParameterizedQueryNegativeASTVisitorTest extends UsesSimpleJDTUnitFixture {
	
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
	public void test_missingParameters_shouldNotTransform() throws Exception {
		String original = "" + 
				"        Connection connection = null;\n" + 
				"        String query = \"SELECT first_name FROM employee WHERE department_id ='40' ORDER BY last_name\";\n" + 
				"        try {\n" + 
				"            Statement statement = connection.createStatement();\n" + 
				"            statement.executeQuery(query);\n" + 
				"        } catch (Exception e) {}";
		assertNoChange(original);
	}
	
	@Test
	public void test_missingQueryDeclaration_shouldNotTransform() throws Exception {
		String original = "" + 
				"class Foo {\n" + 
				"    public void runQuery(String query, Connection connection, String departmentId) throws Exception {\n" + 
				"        Statement statement = connection.createStatement();\n" + 
				"        statement.executeQuery(query);\n" + 
				"    }\n" + 
				"}";
		assertNoChange(original);
	}
	
	@Test
	public void test_multipleQueryUsages_shouldNotTransform() throws Exception {
		String original = "" + 
				"String departmentId = \"40\";\n" + 
				"Connection connection = null;\n" + 
				"String query = \"SELECT first_name FROM employee WHERE department_id ='\" + departmentId + \"' ORDER BY last_name\";\n" + 
				"System.out.println(query);\n" + 
				"try {\n" + 
				"    Statement statement = connection.createStatement();\n" + 
				"    statement.executeQuery(query);\n" + 
				"} catch (Exception e) {}";
		assertNoChange(original);
	}

	@Test
	public void visit_usingFieldAsQuery_shouldNotTransform() throws Exception {
		String original = "" +
				"class Foo {\n" + 
				"    String departmentId = \"40\";\n" + 
				"    String query = \"SELECT first_name FROM employee WHERE department_id ='\" + departmentId + \"' ORDER BY last_name\";\n" + 
				"    public void runQuery(Connection connection, String departmentId) throws Exception {\n" + 
				"        Statement statement = connection.createStatement();\n" + 
				"        statement.executeQuery(query);\n" + 
				"    }\n" + 
				"}";
		assertNoChange(original);
	}

	@Test
	public void visit_consecutiveVariablesAsQueryComponents_shouldNotTransform() throws Exception {
		String original = "" +
				"String departmentId = \"40\";\n" + 
				"Connection connection = null;\n" + 
				"String zero = \"0\";\n" + 
				"String query = \"SELECT first_name FROM employee WHERE department_id ='\" + departmentId + zero + \"' ORDER BY last_name\";\n" + 
				"try {\n" + 
				"    Statement statement = connection.createStatement();\n" + 
				"    statement.executeQuery(query);\n" + 
				"} catch (Exception e) {}";
		assertNoChange(original);
	}
	
	@Test
	public void visit_queryComponentsStartingWithVariable_shouldNotTransform() throws Exception {
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
	public void visit_variableAsLastQueryComponent_shouldNotTransform() throws Exception {
		String original = "" +
				"String departmentId = \"40\";\n" + 
				"Connection connection = null;\n" + 
				"String orderBy = \"' ORDER BY last_name\";\n" + 
				"String query = \"SELECT first_name FROM employee WHERE department_id ='\" + departmentId + orderBy;\n" + 
				"try {\n" + 
				"    Statement statement = connection.createStatement();\n" + 
				"    statement.executeQuery(query);\n" + 
				"} catch (Exception e) {}";
		assertNoChange(original);
	}
	
	/*
	 *  SQL Statement tests
	 */
	
	@Test
	public void test_storingExecuteResult_shouldNotTransform() throws Exception {
		String original = "" + 
				"String departmentId = \"40\";\n" + 
				"Connection connection = null;\n" + 
				"String query = \"SELECT first_name FROM employee WHERE department_id ='\" + departmentId + \"' ORDER BY last_name\";\n" + 
				"try {\n" + 
				"    Statement statement = connection.createStatement();\n" + 
				"    boolean b = statement.execute(query);\n" + 
				"} catch (Exception e) {}";
		assertNoChange(original);
	}
	
	@Test
	public void test_multipleExecute_shouldNotTransform() throws Exception {
		String original = "" + 
				"String departmentId = \"40\";\n" + 
				"Connection connection = null;\n" + 
				"String query = \"SELECT first_name FROM employee WHERE department_id ='\" + departmentId + \"' ORDER BY last_name\";\n" + 
				"try {\n" + 
				"    Statement statement = connection.createStatement();\n" + 
				"    statement.executeQuery(query);\n" + 
				"    statement.execute(query);\n" + 
				"} catch (Exception e) {}";
		assertNoChange(original);
	}
	
	@Test
	public void test_wrongConnectionType_shouldNotTransform() throws Exception {
		String original = "" + 
				"class Foo {\n" + 
				"    public void runQuery(String departmentId) throws Exception {\n" + 
				"        String query = \"SELECT first_name FROM employee WHERE department_id ='\" + departmentId + \"' ORDER BY last_name\";\n" + 
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
	public void visit_missingConnectionCreateStatementInitializer_shouldNotTransform() throws Exception {
		String original = "" +
				"class Foo {\n" + 
				"    public void runQuery(Statement statement, String departmentId) throws Exception {\n" + 
				"        String query = \"SELECT first_name FROM employee WHERE department_id ='\" + departmentId + \"' ORDER BY last_name\";\n" + 
				"        statement.executeQuery(query);\n" + 
				"    }\n" + 
				"} ";
		assertNoChange(original);
	}

	@Test
	public void visit_sqlStatementAssignmentRHS_shouldNotTransform() throws Exception {
		String original = "" +
				"class Foo {\n" + 
				"    public void runQuery(Connection connection, String departmentId) throws Exception {\n" + 
				"        String query = \"SELECT first_name FROM employee WHERE department_id ='\" + departmentId + \"' ORDER BY last_name\";\n" + 
				"        Statement statement = connection.createStatement();\n" + 
				"        Statement statement1;\n" + 
				"        statement1 = statement;\n" + 
				"        statement.executeQuery(query);\n" + 
				"    }\n" + 
				"}";
		assertNoChange(original);
	}
	
	@Test
	public void test_multipleStatementUsages_shouldNotTransform() throws Exception {
		String original = "" + 
				"String departmentId = \"40\";\n" + 
				"Connection connection = null;\n" + 
				"String query = \"SELECT first_name FROM employee WHERE department_id ='\" + departmentId + \"' ORDER BY last_name\";\n" + 
				"try {\n" + 
				"    Statement statement = connection.createStatement(), statement1 = statement;\n" + 
				"    statement.executeQuery(query);\n" + 
				"} catch (Exception e) {}";
		assertNoChange(original);
	}

	@Test
	public void visit_nonSimpleNameExpression_shouldNotTransform() throws Exception {
		String original = "" +
				"class Foo {\n" + 
				"    public void runQuery(Connection connection, String departmentId) throws Exception {\n" + 
				"        String query = \"SELECT first_name FROM employee WHERE department_id ='\" + departmentId + \"' ORDER BY last_name\";\n" + 
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
	public void visit_simpleNameSqlStatementInitializer_shouldNotTransform() throws Exception {
		String original = "" +
				"class Foo {\n" + 
				"    public void runQuery(Statement statement2, Connection connection, String departmentId) throws Exception {\n" + 
				"        String query = \"SELECT first_name FROM employee WHERE department_id ='\" + departmentId + \"' ORDER BY last_name\";\n" + 
				"        Statement statement = statement2;\n" + 
				"        statement.executeQuery(query);\n" + 
				"    }\n" + 
				"}";
		assertNoChange(original);
	}
	
	@Test
	public void visit_missingExpressionInExecuteQuery_shouldNotTransform() throws Exception {
		String original = "" +
				"class FakeSqlStatement {\n" + 
				"    public void runQuery(Connection connection, String departmentId) {\n" + 
				"        String query = \"SELECT first_name FROM employee WHERE department_id ='\" + departmentId + \"' ORDER BY last_name\";\n" + 
				"        executeQuery(query);\n" + 
				"    }\n" + 
				"\n" + 
				"    public ResultSet executeQuery(String query) {\n" + 
				"        return null;\n" + 
				"    }\n" + 
				"}";
		assertNoChange(original);
	}
	
	@Test
	public void visit_reassignmentOfSqlStatement_shouldNotTransform() throws Exception {
		String original = "" +
				"Connection connection = null;\n" + 
				"long id = 40;\n" + 
				"String query = \"SELECT first_name FROM employee WHERE department_id ='\" + id + \"' ORDER BY last_name\";\n" + 
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
	public void test_multipleGetResultSet_shouldNotTransform() throws Exception {
		String original = "" + 
				"String departmentId = \"40\";\n" + 
				"Connection connection = null;\n" + 
				"String query = \"SELECT first_name FROM employee WHERE department_id ='\" + departmentId + \"' ORDER BY last_name\";\n" + 
				"try {\n" + 
				"    Statement statement = connection.createStatement();\n" + 
				"    statement.execute(query);\n" + 
				"    ResultSet rs = statement.getResultSet();\n" + 
				"    statement.getResultSet();\n" + 
				"} catch (Exception e) {}";
		assertNoChange(original);
	}

	@Test
	public void visit_usingGetResultSetAsParameter_shouldNotTransform() throws Exception {
        String original = "" +
                "class Foo {\n" +
                "    public void runQuery(Connection connection, String departmentId) throws Exception {\n" +
                "        String query = \"SELECT first_name FROM employee WHERE department_id ='\" + departmentId + \"' ORDER BY last_name\";\n" +
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
	public void test_conflictingRSVariableNames_shouldNotTransform() throws Exception {
		String original = "" + 
				"String departmentId = \"40\";\n" + 
				"Connection connection = null;\n" + 
				"String query = \"SELECT first_name FROM employee WHERE department_id ='\" + departmentId + \"' ORDER BY last_name\";\n" + 
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
	public void visit_singleVariableDeclarationGetResultSet_shouldNotTransform() throws Exception {
		String original = "" +
				"class Foo {\n" + 
				"    public void runQuery(Connection connection, String departmentId) throws Exception {\n" + 
				"        String query = \"SELECT first_name FROM employee WHERE department_id ='\" + departmentId + \"' ORDER BY last_name\";\n" + 
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
	public void visit_discardedGetResultSet_shouldNotTransform() throws Exception {
		String original = "" +
				"class Foo {\n" + 
				"    public void runQuery(Connection connection, String departmentId) throws Exception {\n" + 
				"        String query = \"SELECT first_name FROM employee WHERE department_id ='\" + departmentId + \"' ORDER BY last_name\";\n" + 
				"        Statement statement = connection.createStatement();\n" + 
				"        statement.execute(query);\n" + 
				"        statement.getResultSet();\n" + 
				"        statement.getResultSet();\n" + 
				"    }\n" + 
				"}";
		assertNoChange(original);
	}
	
	@Test
	public void visit_executeQueryAndGetResultSet_shouldNotTransform() throws Exception {
		String original = "" +
				"class Foo {\n" + 
				"    public void runQuery(Connection connection, String departmentId) throws Exception {\n" + 
				"        String query = \"SELECT first_name FROM employee WHERE department_id ='\" + departmentId + \"' ORDER BY last_name\";\n" + 
				"        Statement statement = connection.createStatement();\n" + 
				"        statement.executeQuery(query);\n" + 
				"        statement.getResultSet();\n" +
				"    }\n" + 
				"}";
		assertNoChange(original);
	}

	/*
	 * Conflicting imports
	 */
	
	@Test
	public void test_conflictingImport_shouldNotTransform() throws Exception {
		String original = "" + 
				"String departmentId = \"40\";\n" + 
				"Connection connection = null;\n" + 
				"String query = \"SELECT first_name FROM employee WHERE department_id ='\" + departmentId + \"' ORDER BY last_name\";\n" + 
				"try {\n" + 
				"    Statement statement = connection.createStatement();\n" + 
				"    statement.executeQuery(query);\n" + 
				"} catch (Exception e) {}";
		fixture.addImport("test.net.sql.PreparedStatement");
		assertNoChange(original);
	}
	
}
