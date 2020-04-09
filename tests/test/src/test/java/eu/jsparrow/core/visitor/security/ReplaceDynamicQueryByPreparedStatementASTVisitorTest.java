package eu.jsparrow.core.visitor.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.core.visitor.impl.UsesSimpleJDTUnitFixture;

@SuppressWarnings("nls")
public class ReplaceDynamicQueryByPreparedStatementASTVisitorTest extends UsesSimpleJDTUnitFixture {
	
	@BeforeEach
	public void setUpVisitor() throws Exception {
		setVisitor(new ReplaceDynamicQueryByPreparedStatementASTVisitor());
		fixture.addImport(java.sql.Connection.class.getName());
		fixture.addImport(java.sql.Statement.class.getName());
		fixture.addImport(java.sql.PreparedStatement.class.getName());
		fixture.addImport(java.sql.ResultSet.class.getName());
	}
	
	@Test
	public void visit_executeQuery_shouldTransform() throws Exception {
		
		String original = "" + 
				"		String departmentId = \"40\";\n" + 
				"        Connection connection = null;\n" + 
				"        String query = \"SELECT employee_id, first_name FROM employee WHERE department_id ='\" + departmentId + \"' ORDER BY last_name\";\n" + 
				"        query += \"\";\n" + 
				"        Statement statement;\n" + 
				"		try {\n" + 
				"			statement = connection.createStatement();\n" + 
				"	        ResultSet resultSet = statement.executeQuery(query);\n" + 
				"	        while(resultSet.next()) {\n" + 
				"	        	String firstName = resultSet.getString(2);\n" + 
				"	        }\n" + 
				"		} catch (Exception e) {\n" + 
				"			return;\n" + 
				"		}";
		
		String expected = "" +
				"		String departmentId = \"40\";\n" + 
				"        Connection connection = null;\n" + 
				"        String query = \"SELECT employee_id, first_name FROM employee WHERE department_id = ?\" + \" ORDER BY last_name\";\n" + 
				"        query += \"\";\n" + 
				"        PreparedStatement statement;\n" + 
				"		try {\n" + 
				"			statement = connection.prepareStatement(query);\n" + 
				"			statement.setString(1, departmentId);" +			
				"	        ResultSet resultSet = statement.executeQuery();\n" + 
				"	        while(resultSet.next()) {\n" + 
				"	        	String firstName = resultSet.getString(2);\n" + 
				"	        }\n" + 
				"		} catch (Exception e) {\n" + 
				"			return;\n" + 
				"		}";
		
		
		assertChange(original, expected);
	}
	
	@Test
	public void visit_multipleConcatenationStatements_shouldTransform() throws Exception {
		String original = "" +
				"		try {\n" + 
				"			String departmentId = \"40\";\n" + 
				"	        Connection connection = null;\n" + 
				"	        String query = \"SELECT employee_id, first_name FROM employee WHERE department_id ='\";\n" + 
				"	        query += departmentId;\n" + 
				"	        query += \"' ORDER BY last_name\";\n" + 
				"	        Statement statement = connection.createStatement();\n" + 
				"	        ResultSet resultSet = statement.executeQuery(query);\n" + 
				"	        while(resultSet.next()) {\n" + 
				"	        	String firstName = resultSet.getString(2);\n" + 
				"	        }\n" + 
				"		} catch (Exception e) {\n" + 
				"			\n" + 
				"		}";
		String expected = "" +
				"		try {\n" + 
				"			String departmentId = \"40\";\n" + 
				"	        Connection connection = null;\n" + 
				"	        String query = \"SELECT employee_id, first_name FROM employee WHERE department_id = ?\";\n" + 
				"	        query += \" ORDER BY last_name\";\n" + 
				"	        PreparedStatement statement = connection.prepareStatement(query);\n" + 
				"			statement.setString(1, departmentId);" +	
				"	        ResultSet resultSet = statement.executeQuery();\n" + 
				"	        while(resultSet.next()) {\n" + 
				"	        	String firstName = resultSet.getString(2);\n" + 
				"	        }\n" + 
				"		} catch (Exception e) {\n" + 
				"			\n" + 
				"		}";
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
				"String query = \"SELECT employee_id, first_name FROM employee WHERE department_id ='\" + departmentId + \"' AND employee_id = '\" + id +  \"' ORDER BY last_name\";\n" + 
				"try {\n" + 
				"	Statement statement = connection.createStatement();\n" + 
				"    ResultSet resultSet = statement.executeQuery(query);\n" + 
				"} catch (Exception e) { }";
		String expected = "" + 
				"String departmentId = \"40\";\n" + 
				"String id = \"1\";\n" + 
				"Connection connection = null;\n" + 
				"String query = \"SELECT employee_id, first_name FROM employee WHERE department_id = ?\" + \"' AND employee_id =  ?\" + \" ORDER BY last_name\";\n" + 
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
				"String query = \"SELECT first_name FROM employee WHERE department_id ='\" + departmentId + \"' ORDER BY last_name\";\n" + 
				"try {\n" + 
				"    Statement statement = connection.createStatement();\n" + 
				"    statement.executeQuery(query);\n" + 
				"} catch (Exception e) {}";
		String expected = "" + 
				"String departmentId = \"40\";\n" + 
				"Connection connection = null;\n" + 
				"String query=\"SELECT first_name FROM employee WHERE department_id = ?\" + \" ORDER BY last_name\";\n" + 
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
                "String query = \"SELECT first_name FROM employee WHERE department_id ='\" + (2 + 38) + \"' ORDER BY last_name\";\n" +
                "try {\n" +
                "    Statement statement = connection.createStatement();\n" +
                "    statement.executeQuery(query);\n" +
                "} catch (Exception e) {}";
        String expected = "" +
                "Connection connection = null;\n" +
                "String query = \"SELECT first_name FROM employee WHERE department_id = ?\" + \" ORDER BY last_name\";\n" +
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
				"String query = \"SELECT first_name FROM employee WHERE department_id ='\" + id + \"' ORDER BY last_name\";\n" + 
				"try {\n" + 
				"    Statement statement = connection.createStatement();\n" + 
				"    statement.execute(query);\n" + 
				"    ResultSet rs = statement.getResultSet(), rs2 = rs;\n" + 
				"} catch (Exception e) {}";
		String expected = "" + 
				"Connection connection = null;\n" + 
				"long id = 40;\n" + 
				"String query = \"SELECT first_name FROM employee WHERE department_id = ?\" + \" ORDER BY last_name\";\n" + 
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
                "String query = \"SELECT first_name FROM employee WHERE department_id ='\" + id + \"' ORDER BY last_name\";\n" +
                "try {\n" +
                "    Statement statement = connection.createStatement();\n" +
                "    statement.executeQuery(query);\n" +
                "} catch (Exception e) {}";
        String expected = "" +
                "Connection connection = null;\n" +
                "long id = 40;\n" +
                "String query = \"SELECT first_name FROM employee WHERE department_id = ?\" + \" ORDER BY last_name\";\n" +
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
				"String query = \"SELECT first_name FROM employee WHERE department_id ='\" + id + \"' ORDER BY last_name\";\n" + 
				"try {\n" + 
				"    Statement statement = connection.createStatement();\n" + 
				"    statement.execute(query);\n" + 
				"    statement.getResultSet();\n" + 
				"} catch (Exception e) {}";
		String expected = "" +
				"int id = 40;\n" + 
				"Connection connection = null;\n" + 
				"String query = \"SELECT first_name FROM employee WHERE department_id = ?\" + \" ORDER BY last_name\";\n" + 
				"try {\n" + 
				"    PreparedStatement statement = connection.prepareStatement(query);\n" + 
				"    statement.setInt(1, id);\n" + 
				"    statement.executeQuery();\n" + 
				"} catch (Exception e) {}";
		assertChange(original, expected);
	}
	
	@Test
	public void visit_multipleParametersMultipleConcatenationLines_shouldTransform () throws Exception {
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
}
