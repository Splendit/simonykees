package eu.jsparrow.core.visitor.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.core.visitor.impl.UsesSimpleJDTUnitFixture;

public class UseParameterizedQueryASTVisitorTest extends UsesSimpleJDTUnitFixture {

	@BeforeEach
	public void setUpVisitor() throws Exception {
		setVisitor(new UseParameterizedQueryASTVisitor());
		fixture.addImport(java.sql.Connection.class.getName());
		fixture.addImport(java.sql.Statement.class.getName());
		fixture.addImport(java.sql.PreparedStatement.class.getName());
		fixture.addImport(java.sql.ResultSet.class.getName());
	}

	@Test
	public void visit_executeQuery_shouldTransform() throws Exception {

		String original = "" +
				"String departmentId = \"40\";\n" +
				"  Connection connection = null;\n" +
				"  String query = \"SELECT employee_id, first_name FROM employee WHERE department_id ='\" + departmentId + \"' ORDER BY last_name\";\n"
				+
				"  query += \"\";\n" +
				"  Statement statement;\n" +
				"try {\n" +
				"	statement = connection.createStatement();\n" +
				"     ResultSet resultSet = statement.executeQuery(query);\n" +
				"     while(resultSet.next()) {\n" +
				"     	String firstName = resultSet.getString(2);\n" +
				"     }\n" +
				"} catch (Exception e) {\n" +
				"	return;\n" +
				"}";

		String expected = "" +
				"String departmentId = \"40\";\n" +
				"  Connection connection = null;\n" +
				"  String query = \"SELECT employee_id, first_name FROM employee WHERE department_id = ?\" + \" ORDER BY last_name\";\n"
				+
				"  query += \"\";\n" +
				"  PreparedStatement statement;\n" +
				"try {\n" +
				"	statement = connection.prepareStatement(query);\n" +
				"	statement.setString(1, departmentId);" +
				"     ResultSet resultSet = statement.executeQuery();\n" +
				"     while(resultSet.next()) {\n" +
				"     	String firstName = resultSet.getString(2);\n" +
				"     }\n" +
				"} catch (Exception e) {\n" +
				"	return;\n" +
				"}";

		assertChange(original, expected);
	}

	@Test
	public void visit_executeQuery_assignedToResultSet_shouldTransform() throws Exception {

		String original = "" +
				"String departmentId = \"40\";\n" +
				"  Connection connection = null;\n" +
				"  String query = \"SELECT employee_id, first_name FROM employee WHERE department_id ='\" + departmentId + \"' ORDER BY last_name\";\n"
				+
				"  query += \"\";\n" +
				"  Statement statement;\n" +
				"try {\n" +
				"	statement = connection.createStatement();\n" +
				"     ResultSet resultSet;\n" +
				"     resultSet = statement.executeQuery(query);\n" +
				"     while(resultSet.next()) {\n" +
				"     	String firstName = resultSet.getString(2);\n" +
				"     }\n" +
				"} catch (Exception e) {\n" +
				"	return;\n" +
				"}";

		String expected = "" +
				"String departmentId = \"40\";\n" +
				"  Connection connection = null;\n" +
				"  String query = \"SELECT employee_id, first_name FROM employee WHERE department_id = ?\" + \" ORDER BY last_name\";\n"
				+
				"  query += \"\";\n" +
				"  PreparedStatement statement;\n" +
				"try {\n" +
				"	ResultSet resultSet;\n" +
				"	statement = connection.prepareStatement(query);\n" +
				"	statement.setString(1, departmentId);" +
				"	resultSet = statement.executeQuery();\n" +
				"	while(resultSet.next()) {\n" +
				"		String firstName = resultSet.getString(2);\n" +
				"	}\n" +
				"} catch (Exception e) {\n" +
				"	return;\n" +
				"}";

		assertChange(original, expected);
	}

	@Test
	public void visit_multipleConcatenationStatements_shouldTransform() throws Exception {
		String original = "" +
				"try {\n" +
				"	String departmentId = \"40\";\n" +
				"     Connection connection = null;\n" +
				"     String query = \"SELECT employee_id, first_name FROM employee WHERE department_id ='\";\n" +
				"     query += departmentId;\n" +
				"     query += \"' ORDER BY last_name\";\n" +
				"     Statement statement = connection.createStatement();\n" +
				"     ResultSet resultSet = statement.executeQuery(query);\n" +
				"     while(resultSet.next()) {\n" +
				"     	String firstName = resultSet.getString(2);\n" +
				"     }\n" +
				"} catch (Exception e) {\n" +
				"	\n" +
				"}";
		String expected = "" +
				"try {\n" +
				"	String departmentId = \"40\";\n" +
				"     Connection connection = null;\n" +
				"     String query = \"SELECT employee_id, first_name FROM employee WHERE department_id = ?\";\n" +
				"     query += \" ORDER BY last_name\";\n" +
				"     PreparedStatement statement = connection.prepareStatement(query);\n" +
				"	statement.setString(1, departmentId);" +
				"     ResultSet resultSet = statement.executeQuery();\n" +
				"     while(resultSet.next()) {\n" +
				"     	String firstName = resultSet.getString(2);\n" +
				"     }\n" +
				"} catch (Exception e) {\n" +
				"	\n" +
				"}";
		assertChange(original, expected);

	}

	@Test
	public void visit_shiftingGetResultSet_shouldTransform() throws Exception {
		String original = "" +
				"String departmentId = \"40\";\n" +
				"Connection connection = null;\n" +
				"String query = \"SELECT first_name FROM employee WHERE department_id ='\" + departmentId + \"'\";\n" +
				"try {\n" +
				"	Statement statement = connection.createStatement();\n" +
				"    statement.execute(query);\n" +
				"    ResultSet resultSet = statement.getResultSet();\n" +
				"    while(resultSet.next()) {\n" +
				"    	String firstName = resultSet.getString(1);\n" +
				"    }\n" +
				"} catch (Exception e) { }";

		String expected = "" +
				"String departmentId = \"40\";\n" +
				"Connection connection = null;\n" +
				"String query = \"SELECT first_name FROM employee WHERE department_id = ?\" + \"\";\n" +
				"try {\n" +
				"	PreparedStatement statement = connection.prepareStatement(query);\n" +
				"	statement.setString(1, departmentId);\n" +
				"    ResultSet resultSet = statement.executeQuery();\n" +
				"    while(resultSet.next()) {\n" +
				"    	String firstName = resultSet.getString(1);\n" +
				"    }\n" +
				"} catch (Exception e) { }";

		assertChange(original, expected);
	}

	@Test
	public void visit_multipleStatementDeclarations_shouldTransform() throws Exception {
		String original = "" +
				"String departmentId = \"40\";\n" +
				"Connection connection = null;\n" +
				"String query = \"SELECT first_name FROM employee WHERE department_id ='\" + departmentId + \"' \";\n" +
				"try {\n" +
				"	Statement statement = connection.createStatement(), statement2;\n" +
				"    ResultSet resultSet = statement.executeQuery(query);\n" +
				"    while(resultSet.next()) {\n" +
				"    	String firstName = resultSet.getString(2);\n" +
				"    }\n" +
				"} catch (Exception e) {}";
		String expected = "" +
				"String departmentId = \"40\";\n" +
				"Connection connection = null;\n" +
				"String query = \"SELECT first_name FROM employee WHERE department_id = ?\" + \" \";\n" +
				"try {\n" +
				"	PreparedStatement statement = connection.prepareStatement(query);\n" +
				"	Statement statement2;\n" +
				"	statement.setString(1, departmentId);\n" +
				"   ResultSet resultSet = statement.executeQuery();\n" +
				"   while(resultSet.next()) {\n" +
				"   	String firstName = resultSet.getString(2);\n" +
				"    }\n" +
				"} catch (Exception e) {}";
		assertChange(original, expected);
	}

	@Test
	public void visit_multipleConcatenations_shouldTransform() throws Exception {
		String original = "" +
				"String departmentId = \"40\";\n" +
				"String id = \"1\";\n" +
				"Connection connection = null;\n" +
				"String query = \"SELECT employee_id, first_name FROM employee WHERE department_id ='\" + departmentId + \"' AND employee_id = '\" + id +  \"' ORDER BY last_name\";\n"
				+
				"try {\n" +
				"	Statement statement = connection.createStatement();\n" +
				"    ResultSet resultSet = statement.executeQuery(query);\n" +
				"} catch (Exception e) { }";
		String expected = "" +
				"String departmentId = \"40\";\n" +
				"String id = \"1\";\n" +
				"Connection connection = null;\n" +
				"String query = \"SELECT employee_id, first_name FROM employee WHERE department_id = ?\" + \"' AND employee_id =  ?\" + \" ORDER BY last_name\";\n"
				+
				"try {\n" +
				"	PreparedStatement statement = connection.prepareStatement(query);\n" +
				"	statement.setString(1, departmentId);\n" +
				"	statement.setString(2, id);\n" +
				"   ResultSet resultSet = statement.executeQuery();\n" +
				"} catch (Exception e) { }";
		assertChange(original, expected);
	}

	@Test
	public void visit_minimalCase_shouldTransform() throws Exception {
		String original = "" +
				"String departmentId = \"40\";\n" +
				"Connection connection = null;\n" +
				"String query = \"SELECT first_name FROM employee WHERE department_id ='\" + departmentId + \"' ORDER BY last_name\";\n"
				+
				"try {\n" +
				"    Statement statement = connection.createStatement();\n" +
				"    statement.executeQuery(query);\n" +
				"} catch (Exception e) {}";
		String expected = "" +
				"String departmentId = \"40\";\n" +
				"Connection connection = null;\n" +
				"String query=\"SELECT first_name FROM employee WHERE department_id = ?\" + \" ORDER BY last_name\";\n"
				+
				"try {\n" +
				"    PreparedStatement statement=connection.prepareStatement(query);\n" +
				"    statement.setString(1, departmentId);" +
				"    statement.executeQuery();\n" +
				"} catch (Exception e) {}";
		assertChange(original, expected);
	}

	@Test
	public void visit_parenthesizedParameterExpression_shouldTransform() throws Exception {
		String original = "" +
				"Connection connection = null;\n" +
				"String query = \"SELECT first_name FROM employee WHERE department_id ='\" + (2 + 38) + \"' ORDER BY last_name\";\n"
				+
				"try {\n" +
				"    Statement statement = connection.createStatement();\n" +
				"    statement.executeQuery(query);\n" +
				"} catch (Exception e) {}";
		String expected = "" +
				"Connection connection = null;\n" +
				"String query = \"SELECT first_name FROM employee WHERE department_id = ?\" + \" ORDER BY last_name\";\n"
				+
				"try {\n" +
				"    PreparedStatement statement = connection.prepareStatement(query);\n" +
				"    statement.setInt(1, (2 + 38));\n" +
				"    statement.executeQuery();\n" +
				"} catch (Exception e) {}";
		assertChange(original, expected);
	}

	@Test
	public void visit_resultSetMultipleDeclarationFragments_shouldTransform() throws Exception {
		String original = "" +
				"Connection connection = null;\n" +
				"long id = 40;\n" +
				"String query = \"SELECT first_name FROM employee WHERE department_id ='\" + id + \"' ORDER BY last_name\";\n"
				+
				"try {\n" +
				"    Statement statement = connection.createStatement();\n" +
				"    statement.execute(query);\n" +
				"    ResultSet rs = statement.getResultSet(), rs2 = rs;\n" +
				"} catch (Exception e) {}";
		String expected = "" +
				"Connection connection = null;\n" +
				"long id = 40;\n" +
				"String query = \"SELECT first_name FROM employee WHERE department_id = ?\" + \" ORDER BY last_name\";\n"
				+
				"try {\n" +
				"    PreparedStatement statement = connection.prepareStatement(query);\n" +
				"    statement.setLong(1, id);\n" +
				"    ResultSet rs = statement.executeQuery();\n" +
				"    ResultSet rs2 = rs;\n" +
				"} catch (Exception e) {}";
		assertChange(original, expected);
	}

	@Test
	public void visit_primitiveTypeParameters_shouldTransform() throws Exception {
		String original = "" +
				"Connection connection = null;\n" +
				"long id = 40;\n" +
				"String query = \"SELECT first_name FROM employee WHERE department_id ='\" + id + \"' ORDER BY last_name\";\n"
				+
				"try {\n" +
				"    Statement statement = connection.createStatement();\n" +
				"    statement.executeQuery(query);\n" +
				"} catch (Exception e) {}";
		String expected = "" +
				"Connection connection = null;\n" +
				"long id = 40;\n" +
				"String query = \"SELECT first_name FROM employee WHERE department_id = ?\" + \" ORDER BY last_name\";\n"
				+
				"try {\n" +
				"    PreparedStatement statement = connection.prepareStatement(query);\n" +
				"    statement.setLong(1, id);\n" +
				"    statement.executeQuery();\n" +
				"} catch (Exception e) {}";
		assertChange(original, expected);
	}

	@Test
	public void visit_discardedResultSet_shouldTransform() throws Exception {
		String original = "" +
				"int id = 40;\n" +
				"Connection connection = null;\n" +
				"String query = \"SELECT first_name FROM employee WHERE department_id ='\" + id + \"' ORDER BY last_name\";\n"
				+
				"try {\n" +
				"    Statement statement = connection.createStatement();\n" +
				"    statement.execute(query);\n" +
				"    statement.getResultSet();\n" +
				"} catch (Exception e) {}";
		String expected = "" +
				"int id = 40;\n" +
				"Connection connection = null;\n" +
				"String query = \"SELECT first_name FROM employee WHERE department_id = ?\" + \" ORDER BY last_name\";\n"
				+
				"try {\n" +
				"    PreparedStatement statement = connection.prepareStatement(query);\n" +
				"    statement.setInt(1, id);\n" +
				"    statement.executeQuery();\n" +
				"} catch (Exception e) {}";
		assertChange(original, expected);
	}

	@Test
	public void visit_multipleParametersMultipleConcatenationLines_shouldTransform() throws Exception {
		String original = "" +
				"class Foo {\n" +
				"	public void sampleMethod(Connection connection, String departmentId, int id) throws Exception {\n" +
				"        String query = \"SELECT first_name FROM employee WHERE\";\n" +
				"        query += \" id > '\" + id + \"'\";\n" +
				"        query += \" AND department_id ='\" + departmentId + \"'\";\n" +
				"        query += \" ORDER BY last_name\";\n" +
				"        Statement statement = connection.createStatement();\n" +
				"        ResultSet resultSet = statement.executeQuery(query);\n" +
				"	}\n" +
				"}";
		String expected = "" +
				"class Foo {\n" +
				"	public void sampleMethod(Connection connection, String departmentId, int id) throws Exception {\n" +
				"        String query = \"SELECT first_name FROM employee WHERE\";\n" +
				"        query += \" id >  ?\" + \"\";\n" +
				"        query += \" AND department_id = ?\" + \"\";\n" +
				"        query += \" ORDER BY last_name\";\n" +
				"        PreparedStatement statement = connection.prepareStatement(query);\n" +
				"		 statement.setInt(1, id);\n" +
				"		 statement.setString(2, departmentId);\n" +
				"        ResultSet resultSet = statement.executeQuery();\n" +
				"	}\n" +
				"}";
		assertChange(original, expected);
	}

	@Test
	public void visit_infixExpressions_shouldTransform() throws Exception {
		String original = "" +
				"class Foo {\n" +
				"	public void sampleMethod(Connection connection, String departmentId1, String departmentId2) throws Exception {\n"
				+
				"	    String query = \"\" + \n" +
				"	            \"SELECT\" +\n" +
				"	            \"   employee_id\" +\n" +
				"	            \"   , first_name\" +\n" +
				"	            \"   FROM\" +\n" +
				"	            \"       employee\" +\n" +
				"	            \"   WHERE\" + \n" +
				"	            \"       department_id =\" + \"'\" + departmentId1 + \"'\" + \n" +
				"	            \"   OR\" +\n" +
				"	            \"       department_id =\" + \"'\" + departmentId2 + \"'\" + \n" +
				"	            \"   ORDER BY last_name\";\n" +
				"	    Statement statement = connection.createStatement();\n" +
				"	    ResultSet resultSet = statement.executeQuery(query);\n" +
				"	}\n" +
				"}";
		String expected = "" +
				"class Foo {\n" +
				"	public void sampleMethod(Connection connection, String departmentId1, String departmentId2) throws Exception {\n"
				+
				"	    String query = \"\" + \n" +
				"	            \"SELECT\" +\n" +
				"	            \"   employee_id\" +\n" +
				"	            \"   , first_name\" +\n" +
				"	            \"   FROM\" +\n" +
				"	            \"       employee\" +\n" +
				"	            \"   WHERE\" + \n" +
				"	            \"       department_id =\" + \" ?\" + \"\" + \n" +
				"	            \"   OR\" +\n" +
				"	            \"       department_id =\" + \" ?\" + \"\" + \n" +
				"	            \"   ORDER BY last_name\";\n" +
				"	    PreparedStatement statement = connection.prepareStatement(query);\n" +
				"		statement.setString(1, departmentId1);\n" +
				"		statement.setString(2, departmentId2);\n" +
				"	    ResultSet resultSet = statement.executeQuery();\n" +
				"	}\n" +
				"}";
		assertChange(original, expected);
	}

	@Test
	public void visit_VariableDeclarationsAfterCreateStatement_shouldTransform() throws Exception {
		String original = "" +
				"Connection connection = null;\n" +
				"Statement statement;\n" +
				"try {\n" +
				"	statement = connection.createStatement();\n" +
				"	String departmentId1 = \"40\";\n" +
				"	String query = \"\" + \"SELECT employee_id FROM employee WHERE department_id = '\" + departmentId1 + \"'\";\n"
				+
				"	query += \" OR department_id = '\";\n" +
				"	String departmentId2 = \"140\";\n" +
				"	query += departmentId2;\n" +
				"	query += \"'\";\n" +
				"	ResultSet resultSet = statement.executeQuery(query);\n" +
				"} catch (Exception e) {}";

		String expected = "" +
				"Connection connection = null;\n" +
				"PreparedStatement statement;\n" +
				"try {\n" +
				"	String departmentId1 = \"40\";\n" +
				"	String query = \"\" + \"SELECT employee_id FROM employee WHERE department_id =  ?\" + \"\";\n" +
				"	query += \" OR department_id =  ?\";\n" +
				"	String departmentId2 = \"140\";\n" +
				"	query += \"\";\n" +
				"	statement = connection.prepareStatement(query);\n" +
				"	statement.setString(1, departmentId1);\n" +
				"	statement.setString(2, departmentId2);\n" +
				"	ResultSet resultSet = statement.executeQuery();\n" +
				"} catch (Exception e) {}";

		assertChange(original, expected);
	}

	@Test
	public void visit_movingStatementDeclarationFragmentAfterParameterDeclarations_shouldTransform() throws Exception {
		String original = "" +
				"Connection connection = null;\n" +
				"try {\n" +
				"	Statement statement  = connection.createStatement();\n" +
				"	String departmentId1 = \"40\";\n" +
				"	String query = \"\" + \"SELECT employee_id FROM employee WHERE department_id = '\" + departmentId1 + \"'\";\n"
				+
				"	ResultSet resultSet = statement.executeQuery(query);\n" +
				"} catch (Exception e) {}";

		String expected = "" +
				"Connection connection = null;\n" +
				"try {\n" +
				"	PreparedStatement statement;\n" +
				"	String departmentId1 = \"40\";\n" +
				"	String query = \"\" + \"SELECT employee_id FROM employee WHERE department_id =  ?\" + \"\";\n" +
				"	statement = connection.prepareStatement(query);\n" +
				"	statement.setString(1, departmentId1);\n" +
				"	ResultSet resultSet = statement.executeQuery();\n" +
				"} catch (Exception e) {}";

		assertChange(original, expected);
	}

	@Test
	public void visit_ExecuteQueryAsOnlyStatementInNestedBlock_shouldTransform() throws Exception {
		String original = "" +
				"class TestExecuteQueryAsOnlyStatementInNestedBlock {\n" +
				"	void test() {\n" +
				"		Connection connection = null;\n" +
				"		String departmentId1 = \"40\";			\n" +
				"		String query = \"SELECT employee_id, first_name FROM employee WHERE department_id ='\" + departmentId1 + \"'\";\n"
				+
				"		Statement statement;\n" +
				"		try {\n" +
				"			statement = connection.createStatement();\n" +
				"			{\n" +
				"				statement.executeQuery(query);\n" +
				"			}\n" +
				"		} catch (Exception e) {}\n" +
				"	}\n" +
				"}";

		String expected = "" +
				"class TestExecuteQueryAsOnlyStatementInNestedBlock {\n" +
				"	void test() {\n" +
				"		Connection connection = null;\n" +
				"		String departmentId1 = \"40\";			\n" +
				"		String query = \"SELECT employee_id, first_name FROM employee WHERE department_id = ?\" + \"\";\n"
				+
				"		PreparedStatement statement;\n" +
				"		try {\n" +
				"			{\n" +
				"				statement = connection.prepareStatement(query);\n" +
				"				statement.setString(1, departmentId1);\n" +
				"				statement.executeQuery();\n" +
				"			}\n" +
				"		} catch (Exception e) {}\n" +
				"	}\n" +
				"}\n";
		assertChange(original, expected);
	}

	@Test
	public void visit_referencingStatementAfterUsage_shouldTransform() throws Exception {
		String original = "" +
				"Connection connection = null;\n" +
				"try {\n" +
				"	Statement statement = connection.createStatement();\n" +
				"	String departmentId = \"40\";\n" +

				"	String query = \"SELECT employee_id, first_name FROM employee WHERE department_id = '\" + departmentId + \"'\";\n"
				+
				"	ResultSet resultSet = statement.executeQuery(query);\n" +
				"	statement.equals(null);\n" +
				"}	catch (Exception e) {\n" +
				"	\n" +
				"}";

		String expected = "" +
				"Connection connection=null;\n" +
				"try {\n" +
				"	PreparedStatement statement;\n" +
				"	String departmentId=\"40\";\n" +
				"	String query=\"SELECT employee_id, first_name FROM employee WHERE department_id =  ?\" + \"\";\n" +
				"	statement=connection.prepareStatement(query);\n" +
				"	statement.setString(1,departmentId);\n" +
				"	ResultSet resultSet=statement.executeQuery();\n" +
				"	statement.equals(null);\n" +
				"}	catch (Exception e) {\n" +
				"}";
		assertChange(original, expected);
	}

	@Test
	public void visit_executeUpdate_shouldTransform() throws Exception {
		String original = "" +
				"			Connection connection = null;\n" +
				"			Statement statement;\n" +
				"			String salary = \"1000000\";\n" +
				"			String id = \"1000001\";\n" +
				"			String query = \"UPDATE employee SET salary  ='\" + salary + \"' WHERE id = '\" + id + \"'\";\n"
				+
				"			try {\n" +
				"				statement = connection.createStatement();\n" +
				"				statement.executeUpdate(query);\n" +
				"			} catch (Exception e) {\n" +
				"			}";

		String expected = "" +
				"			Connection connection = null;\n" +
				"			PreparedStatement statement;\n" +
				"			String salary = \"1000000\";\n" +
				"			String id = \"1000001\";\n" +
				"			String query = \"UPDATE employee SET salary  = ?\" + \"' WHERE id =  ?\" + \"\";\n" +
				"			try {\n" +
				"				statement = connection.prepareStatement(query);\n" +
				"				statement.setString(1, salary);\n" +
				"				statement.setString(2, id);\n" +
				"				statement.executeUpdate();\n" +
				"			} catch (Exception e) {\n" +
				"			}";
		assertChange(original, expected);
	}
	
	@Test
	public void visit_ExecuteQueryArgumentInfixExpression_shouldTransform() throws Exception {

		String original = "" +
				"		String departmentId1 = \"40\";\n" + 
				"		try {\n" + 
				"			Connection connection = null;\n" + 
				"			Statement statement = connection.createStatement();\n" + 
				"			ResultSet resultSet = statement\n" + 
				"					.executeQuery(\"SELECT id FROM employee WHERE department_id = '\" + departmentId1 + \"'\");\n" + 
				"		} catch (Exception e) {\n" + 
				"		}";
		
		String expected = "" +
				"		String departmentId1=\"40\";\n" + 
				"		try {\n" + 
				"			Connection connection=null;\n" + 
				"			PreparedStatement statement=connection.prepareStatement(\"SELECT id FROM employee WHERE department_id =  ?\" + \"\");\n" + 
				"			statement.setString(1,departmentId1);\n" + 
				"			ResultSet resultSet=statement.executeQuery();\n" + 
				"		} catch (  Exception e) {\n" + 
				"		}";

		assertChange(original, expected);
	}
}
