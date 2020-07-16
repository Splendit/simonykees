package eu.jsparrow.core.visitor.security;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.core.visitor.impl.UsesJDTUnitFixture;

@SuppressWarnings("nls")
public class EscapeUserInputsInSQLQueriesNegativeASTVisitorTest extends UsesJDTUnitFixture {

	private static final String TRY_EXECUTE = "" +
			"		try {\n" +
			"			Connection connection = null;\n" +
			"			Statement statement = connection.createStatement();\n" +
			"			ResultSet results = statement.executeQuery(query);\n" +
			"		} catch (SQLException e) {\n" +
			"			e.printStackTrace();\n" +
			"		}\n";

	@BeforeEach
	public void setUp() throws Exception {
		setDefaultVisitor(new EscapeUserInputsInSQLQueriesASTVisitor());
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
	public void visit_LocalFinalStringVariables_shouldNotTransform() throws Exception {

		String original = "" +
				"	void test() {			\n" +
				"		final String userName = \"userName\";\n" +
				"		final String userPWD = \"userPWD\";\n" +
				"		String query = \"SELECT user_id FROM user_data WHERE user_name = '\" + \n" +
				"				userName + \n" +
				"				\"' and user_password = '\" + \n" +
				"				userPWD + \n" +
				"				\"'\";\n" +
				TRY_EXECUTE +
				"	}";

		assertNoChange(original);
	}

	@Test
	public void visit_FinalStringFields_shouldNotTransform() throws Exception {

		String original = "" +
				"	private final String userName = \"userName\";\n" +
				"	private final String userPWD = \"userPWD\";\n" +
				"	void test() {\n" +
				"		String query = \"SELECT user_id FROM user_data WHERE user_name = '\" + \n" +
				"				this.userName + \n" +
				"				\"' and user_password = '\" + \n" +
				"				this.userPWD + \n" +
				"				\"'\";\n" +
				TRY_EXECUTE +
				"	}";

		assertNoChange(original);
	}

	@Test
	public void visit_PrimitiveIntVariable_shouldNotTransform() throws Exception {

		String original = "" +
				"	void test() {\n" +
				"		int userID = 100000;\n" +
				"		String query = \n" +
				"			\"SELECT user_name FROM user_data WHERE user_id = '\" + \n" +
				"			userID + \n" +
				"			\"'\";\n" +
				TRY_EXECUTE +
				"	}";

		assertNoChange(original);
	}

	/**
	 * This test is expected to fail as soon as Bugfix SIM-1775 is solved.
	 */
	@Test
	public void visit_ConditionalUserSuppliedInput_shouldNotTransform() throws Exception {
		String original = "" +
				"	void test() {\n" + 
				"			String userName = \"userName\";\n" + 
				"			String query = \"SELECT user_id FROM user_data WHERE user_name = '\" + userName + \"'\";\n" + 
				"			if (true) {\n" + 
				"				String userPassword = \"userPassword\";\n" + 
				"				query += \" and user_password = '\" + userPassword + \"'\";\n" + 
				"			}\n" + 
				TRY_EXECUTE +
				"		}";
		assertNoChange(original);
	}

}
