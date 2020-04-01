package eu.jsparrow.core.visitor.security;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

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
	
	public void originalSample() {
		String departmentId = "40";
        Connection connection = null;
        String query = "SELECT employee_id, first_name FROM employee WHERE department_id ='" + departmentId + "' ORDER BY last_name";
        query += "";
        Statement statement;
		try {
			statement = connection.createStatement();

	        ResultSet resultSet = statement.executeQuery(query);
	        while(resultSet.next()) {
	        	String firstName = resultSet.getString(2);
	        }
		} catch (Exception e) {
			return;
		}
	}
	
	public void expectedSample() throws Exception {
		
		String departmentId = "40";
        Connection connection = null;
        String query = "SELECT employee_id, first_name FROM employee WHERE department_id =? ORDER BY last_name";
        query += "";
        PreparedStatement statement;
		try {
			statement = connection.prepareStatement(query);
			statement.setString(1, departmentId);

	        ResultSet resultSet = statement.executeQuery();
	        while(resultSet.next()) {
	        	String firstName = resultSet.getString(2);
	        }
		} catch (Exception e) {
			return;
		}
		
		
		
		
	}

}
