package eu.jsparrow.core.visitor.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.core.visitor.impl.UsesSimpleJDTUnitFixture;

@SuppressWarnings("nls")
public class EscapingDynamicQueriesNegativeASTVisitorTest extends UsesSimpleJDTUnitFixture {

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
	public void visit_LocalClassCodec_shouldNotTransform() throws Exception {

		String original = "" + //
				"		class Codec {\n" +
				"		}\n" +
				"		String departmentId = \"40\";\n" +
				"		String query = " +
				"			\"SELECT employee_id, first_name FROM employee WHERE department_id ='\" + " +
				"			departmentId + " +
				"			\"' ORDER BY last_name\";\n" +
				TRY_EXECUTING_QUERY;

		assertNoChange(original);
	}

}
