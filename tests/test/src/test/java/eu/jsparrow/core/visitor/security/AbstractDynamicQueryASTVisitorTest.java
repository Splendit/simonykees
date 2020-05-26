package eu.jsparrow.core.visitor.security;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.core.visitor.impl.UsesJDTUnitFixture;

@SuppressWarnings("nls")
public class AbstractDynamicQueryASTVisitorTest extends UsesJDTUnitFixture {

	@BeforeEach
	public void setUp() throws Exception {
		setDefaultVisitor(new UseParameterizedQueryASTVisitor());
		defaultFixture.addImport(java.sql.Connection.class.getName());
		defaultFixture.addImport(java.sql.Statement.class.getName());
		defaultFixture.addImport(java.sql.ResultSet.class.getName());
		defaultFixture.addImport(java.sql.SQLException.class.getName());
	}

	@AfterEach
	public void tearDown() throws Exception {
		fixtureProject.clear();
	}

	@Test
	public void visit_ExecuteQueryArgumentConcatenated_shouldNotTransform() throws Exception {

		String original = "" +
				"	void test() {\n" +
				"		String userID = \"100000\";\n" +
				"		String query = \n" +
				"			\"SELECT user_name FROM user_data WHERE user_id = '\" + \n" +
				"			userID + \n" +
				"			\"'\";\n" +
				"		try {\n" +
				"			Connection connection = null;\n" +
				"			Statement statement = connection.createStatement();\n" +
				"			ResultSet resultset = statement.executeQuery(query + \" ORDER BY last_name\");\n" +
				"		} catch (SQLException e) {\n" +
				"			e.printStackTrace();\n" +
				"		}" +
				"	}";
		assertNoChange(original);
	}

	@Test
	public void visit_TwoExecuteArguments_shouldNotTransform() throws Exception {

		String original = "" +
				"	void test() {\n" +
				"		String userID = \"100000\";\n" +
				"		String query = \n" +
				"			\"SELECT user_name FROM user_data WHERE user_id = '\" + \n" +
				"			userID + \n" +
				"			\"'\";\n" +
				"		try {\n" +
				"			Connection connection = null;\n" +
				"			Statement statement = connection.createStatement();\n" +
				"			statement.execute(query, 1);\n" +
				"		} catch (SQLException e) {\n" +
				"			e.printStackTrace();\n" +
				"		}" +
				"	}";
		assertNoChange(original);
	}
	
	@Test
	public void visit_WithStatementAsLocalClass_shouldNotTransform() throws Exception {

		String original = "" +
				"	void test() {\n" +
				"		String userID = \"100000\";\n" +
				"		String query = \n" +
				"			\"SELECT user_name FROM user_data WHERE user_id = '\" + \n" +
				"			userID + \n" +
				"			\"'\";\n" +
				"		class Statement {\n" + 
				"			boolean execute(String query) {\n" + 
				"				return false;\n" + 
				"			}\n" + 
				"		}\n" + 
				"		try {\n" + 
				"			Statement statement = new Statement();\n" + 
				"			statement.execute(query);\n" + 
				"		} catch (Exception e) {\n" + 
				"		}" +
				"	}";
		assertNoChange(original);
	}

	@Test
	public void visit_ExecuteQueryArgumentSimpleName_shouldTransform() throws Exception {

		String original = "" +
				"	void test() {\n" +
				"		String userID = \"100000\";\n" +
				"		String query = \n" +
				"			\"SELECT user_name FROM user_data WHERE user_id = '\" + \n" +
				"			userID + \n" +
				"			\"'\";\n" +
				"		try {\n" +
				"			Connection connection = null;\n" +
				"			Statement statement = connection.createStatement();\n" +
				"			ResultSet resultset = statement.executeQuery(query);\n" +
				"		} catch (SQLException e) {\n" +
				"			e.printStackTrace();\n" +
				"		}" +
				"	}";

		String expected = "" +
				"	void test() {\n" +
				"		String userID = \"100000\";\n" +
				"		String query = \n" +
				"			\"SELECT user_name FROM user_data WHERE user_id =  ?\" + \"\";\n" +
				"		try {\n" +
				"			Connection connection = null;\n" +
				"			PreparedStatement statement = connection.prepareStatement(query);\n" +
				"			statement.setString(1,userID);\n" +
				"			ResultSet resultset = statement.executeQuery();\n" +
				"		} catch (SQLException e) {\n" +
				"			e.printStackTrace();\n" +
				"		}" +
				"	}";

		assertChange(original, expected);
	}

}
