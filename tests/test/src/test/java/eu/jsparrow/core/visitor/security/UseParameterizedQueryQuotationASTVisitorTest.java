package eu.jsparrow.core.visitor.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.common.UsesSimpleJDTUnitFixture;

@SuppressWarnings("nls")
public class UseParameterizedQueryQuotationASTVisitorTest extends UsesSimpleJDTUnitFixture {

	@BeforeEach
	public void setUpVisitor() throws Exception {
		setVisitor(new UseParameterizedQueryASTVisitor());
		fixture.addImport(java.sql.Connection.class.getName());
		fixture.addImport(java.sql.Statement.class.getName());
		fixture.addImport(java.sql.ResultSet.class.getName());
	}

	@Test
	public void visit_ExecuteQuery_AllQuotationsWellFormed() throws Exception {

		String original = "" +
				"	String departmentId1 = \"40\";\n" +
				"	String departmentId2 = \"140\";\n" +
				"	Connection connection = null;\n" +
				"	String query = " +
				"			\"SELECT employee_id, first_name FROM employee WHERE department_id ='\" + " +
				"			departmentId1 + \"'\" + " +
				"			\" OR department_id ='\" + " +
				"			departmentId2 + \"'\" + " +
				"			\" ORDER BY last_name\";\n" +
				"	Statement statement;\n" +
				"	try {\n" +
				"		statement = connection.createStatement();\n" +
				"		ResultSet resultSet = statement.executeQuery(query);\n" +
				"		while (resultSet.next()) {\n" +
				"			String firstName = resultSet.getString(2);\n" +
				"		}\n" +
				"	} catch (Exception e) {\n" +
				"	}";

		String expected = "" +
				"	String departmentId1 = \"40\";\n" +
				"	String departmentId2 = \"140\";\n" +
				"	Connection connection = null;\n" +
				"	String query = " +
				"			\"SELECT employee_id, first_name FROM employee WHERE department_id = ?\" + " +
				"			\"\" + " +
				"			\" OR department_id = ?\" + " +
				"			\"\" + " +
				"			\" ORDER BY last_name\";\n" +
				"	PreparedStatement statement;\n" +
				"	try {\n" +
				"		statement = connection.prepareStatement(query);\n" +
				"		statement.setString(1, departmentId1);\n" +
				"		statement.setString(2, departmentId2);\n" +
				"		ResultSet resultSet = statement.executeQuery();\n" +
				"		while (resultSet.next()) {\n" +
				"			String firstName = resultSet.getString(2);\n" +
				"		}\n" +
				"	} catch (Exception e) {\n" +
				"	}";

		assertChange(original, expected);
	}

	@Test
	public void visit_ExecuteQuery_MissingOpeningQuote() throws Exception {

		String original = "" +
				"	String departmentId1 = \"'40\";\n" +
				"	String departmentId2 = \"140\";\n" +
				"	Connection connection = null;\n" +
				"	String query = " +
				"			\"SELECT employee_id, first_name FROM employee WHERE department_id =\" + " +
				"			departmentId1 + \"'\" + " +
				"			\" OR department_id ='\" + " +
				"			departmentId2 + \"'\" + " +
				"			\" ORDER BY last_name\";\n" +
				"	Statement statement;\n" +
				"	try {\n" +
				"		statement = connection.createStatement();\n" +
				"		ResultSet resultSet = statement.executeQuery(query);\n" +
				"		while (resultSet.next()) {\n" +
				"			String firstName = resultSet.getString(2);\n" +
				"		}\n" +
				"	} catch (Exception e) {\n" +
				"	}";

		String expected = "" +
				"	String departmentId1 = \"'40\";\n" +
				"	String departmentId2 = \"140\";\n" +
				"	Connection connection = null;\n" +
				"	String query = " +
				"			\"SELECT employee_id, first_name FROM employee WHERE department_id =\" + " +
				"			departmentId1 + \"'\" + " +
				"			\" OR department_id = ?\" + " +
				"			\"\" + " +
				"			\" ORDER BY last_name\";" +
				"	PreparedStatement statement;\n" +
				"	try {\n" +
				"		statement = connection.prepareStatement(query);\n" +
				"		statement.setString(1, departmentId2);\n" +
				"		ResultSet resultSet = statement.executeQuery();\n" +
				"		while (resultSet.next()) {\n" +
				"			String firstName = resultSet.getString(2);\n" +
				"		}\n" +
				"	} catch (Exception e) {\n" +
				"	}";

		assertChange(original, expected);
	}

	@Test
	public void visit_ExecuteQuery_MissingClosingQuote() throws Exception {

		String original = "" +
				"	String departmentId1 = \"40'\";\n" +
				"	String departmentId2 = \"140\";\n" +
				"	Connection connection = null;\n" +
				"	String query = " +
				"			\"SELECT employee_id, first_name FROM employee WHERE department_id ='\" + " +
				"			departmentId1 + \"\" + " +
				"			\" OR department_id ='\" + " +
				"			departmentId2 + \"'\" + " +
				"			\" ORDER BY last_name\";\n" +
				"	Statement statement;\n" +
				"	try {\n" +
				"		statement = connection.createStatement();\n" +
				"		ResultSet resultSet = statement.executeQuery(query);\n" +
				"		while (resultSet.next()) {\n" +
				"			String firstName = resultSet.getString(2);\n" +
				"		}\n" +
				"	} catch (Exception e) {\n" +
				"	}";

		String expected = "" +
				"	String departmentId1 = \"40'\";\n" +
				"	String departmentId2 = \"140\";\n" +
				"	Connection connection = null;\n" +
				"	String query = " +
				"			\"SELECT employee_id, first_name FROM employee WHERE department_id ='\" + " +
				"			departmentId1 + \"\" + " +
				"			\" OR department_id = ?\" + " +
				"           \"\" + " +
				"			\" ORDER BY last_name\";" +
				"	PreparedStatement statement;\n" +
				"	try {\n" +
				"		statement = connection.prepareStatement(query);\n" +
				"		statement.setString(1, departmentId2);\n" +
				"		ResultSet resultSet = statement.executeQuery();\n" +
				"		while (resultSet.next()) {\n" +
				"			String firstName = resultSet.getString(2);\n" +
				"		}\n" +
				"	} catch (Exception e) {\n" +
				"	}";

		assertChange(original, expected);
	}

	@Test
	public void visit_ExecuteQuery_SpaceAfterOpeningQuote() throws Exception {
		String original = "" +
				"	String departmentId1 = \"40\";\n" +
				"	String departmentId2 = \"140\";\n" +
				"	Connection connection = null;\n" +
				"	String query = " +
				"			\"SELECT employee_id, first_name FROM employee WHERE department_id =' \" + " +
				"			departmentId1 + \"'\" + " +
				"			\" OR department_id ='\" + " +
				"			departmentId2 + \"'\" + " +
				"			\" ORDER BY last_name\";\n" +
				"	Statement statement;\n" +
				"	try {\n" +
				"		statement = connection.createStatement();\n" +
				"		ResultSet resultSet = statement.executeQuery(query);\n" +
				"		while (resultSet.next()) {\n" +
				"			String firstName = resultSet.getString(2);\n" +
				"		}\n" +
				"	} catch (Exception e) {\n" +
				"	}";

		String expected = "" +
				"	String departmentId1 = \"40\";\n" +
				"	String departmentId2 = \"140\";\n" +
				"	Connection connection = null;\n" +
				"	String query = " +
				"			\"SELECT employee_id, first_name FROM employee WHERE department_id =' \" + " +
				"			departmentId1 + \"'\" + " +
				"			\" OR department_id = ?\" + " +
				"			\"\" + " +
				"			\" ORDER BY last_name\";" +
				"	PreparedStatement statement;\n" +
				"	try {\n" +
				"		statement = connection.prepareStatement(query);\n" +
				"		statement.setString(1, departmentId2);\n" +
				"		ResultSet resultSet = statement.executeQuery();\n" +
				"		while (resultSet.next()) {\n" +
				"			String firstName = resultSet.getString(2);\n" +
				"		}\n" +
				"	} catch (Exception e) {\n" +
				"	}";

		assertChange(original, expected);
	}

	@Test
	public void visit_ExecuteQuery_SpaceBeforeClosingQuot() throws Exception {
		String original = "" +
				"	String departmentId1 = \"40\";\n" +
				"	String departmentId2 = \"140\";\n" +
				"	Connection connection = null;\n" +
				"	String query = " +
				"			\"SELECT employee_id, first_name FROM employee WHERE department_id ='\" + " +
				"			departmentId1 + \" '\" + " +
				"			\" OR department_id ='\" + " +
				"			departmentId2 + \"'\" + " +
				"			\" ORDER BY last_name\";\n" +
				"	Statement statement;\n" +
				"	try {\n" +
				"		statement = connection.createStatement();\n" +
				"		ResultSet resultSet = statement.executeQuery(query);\n" +
				"		while (resultSet.next()) {\n" +
				"			String firstName = resultSet.getString(2);\n" +
				"		}\n" +
				"	} catch (Exception e) {\n" +
				"	}";

		String expected = "" +
				"	String departmentId1 = \"40\";\n" +
				"	String departmentId2 = \"140\";\n" +
				"	Connection connection = null;\n" +
				"   String query = " +
				"			\"SELECT employee_id, first_name FROM employee WHERE department_id ='\" + " +
				"			departmentId1 + \" '\" + " +
				"			\" OR department_id = ?\" + " +
				"			\"\" + " +
				"			\" ORDER BY last_name\";" +
				"	PreparedStatement statement;\n" +
				"	try {\n" +
				"		statement = connection.prepareStatement(query);\n" +
				"		statement.setString(1, departmentId2);\n" +
				"		ResultSet resultSet = statement.executeQuery();\n" +
				"		while (resultSet.next()) {\n" +
				"			String firstName = resultSet.getString(2);\n" +
				"		}\n" +
				"	} catch (Exception e) {\n" +
				"	}";

		assertChange(original, expected);
	}

}
