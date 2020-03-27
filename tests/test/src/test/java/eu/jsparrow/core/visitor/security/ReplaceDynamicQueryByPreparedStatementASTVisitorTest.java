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
	public void setUpVisitor() {
		setVisitor(new ReplaceDynamicQueryByPreparedStatementASTVisitor());
	}
	
	@Test
	public void visit_() throws Exception {
		
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
				"        String query = \"SELECT employee_id, first_name FROM employee WHERE department_id =? ORDER BY last_name\";\n" + 
				"        query += \"\";\n" + 
				"        PreparedStatement statement;\n" + 
				"		try {\n" + 
				"			statement = connection.prepareStatement(query);\n" + 
				"	        ResultSet resultSet = statement.executeQuery();\n" + 
				"	        while(resultSet.next()) {\n" + 
				"	        	String firstName = resultSet.getString(2);\n" + 
				"	        }\n" + 
				"		} catch (Exception e) {\n" + 
				"			return;\n" + 
				"		}";
		
		
		
		fixture.addImport(java.sql.Connection.class.getName());
		fixture.addImport(java.sql.Statement.class.getName());
		fixture.addImport(java.sql.ResultSet.class.getName());
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
		
		fixture.addImport(java.sql.Connection.class.getName());
		fixture.addImport(java.sql.Statement.class.getName());
		fixture.addImport(java.sql.ResultSet.class.getName());
		
		
		
	}

}
