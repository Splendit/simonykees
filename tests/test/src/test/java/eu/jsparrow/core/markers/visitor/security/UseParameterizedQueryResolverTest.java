package eu.jsparrow.core.markers.visitor.security;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.common.UsesSimpleJDTUnitFixture;
import eu.jsparrow.rules.common.markers.RefactoringMarkerEvent;
import eu.jsparrow.rules.common.markers.RefactoringMarkers;

class UseParameterizedQueryResolverTest extends UsesSimpleJDTUnitFixture {

	@BeforeEach
	public void setUpVisitor() throws Exception {
		RefactoringMarkers.clear();
		fixture.addImport(java.sql.Connection.class.getName());
		fixture.addImport(java.sql.Statement.class.getName());
		fixture.addImport(java.sql.ResultSet.class.getName());
	}
	
	
	@Test
	void test_AlwaysFalsePredicate_shouldGenerateNoMarkers() throws Exception {
		UseParameterizedQueryResolver visitor = new UseParameterizedQueryResolver(node -> false);
		visitor.addMarkerListener(RefactoringMarkers.getFor("UseParameterizedQueryResolver"));
		setVisitor(visitor);
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

		assertNoChange(original);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertTrue(events.isEmpty());
	}

	@Test
	void test_markerGeneration_shouldGenerateOneMarkerEvent() throws Exception {
		UseParameterizedQueryResolver visitor = new UseParameterizedQueryResolver(node -> true);
		visitor.addMarkerListener(RefactoringMarkers.getFor("UseParameterizedQueryResolver"));
		setVisitor(visitor);
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
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertEquals(1, events.size());
		RefactoringMarkerEvent event = events.get(0);
		String description = ""
				+ "Replaces java.sql.Statement with java.sql.PreparedStatement (aka parameterized queries) if "
				+ "the SQL query is constructed by concatenating string literals with user defined expressions "
				+ "(e.g. variables, method invocations, user input, etc). Parameterized queries enforce a distinction "
				+ "between the SQL code and the data passed through parameters, thus reducing the SQL injection vulnerabilities.";
		
		assertAll(
				() -> assertEquals("Use Parameterized Query", event.getName()),
				() -> assertEquals(description, event.getMessage()), 
				() -> assertEquals("UseParameterizedQueryResolver", event.getResolver()),
				() -> assertEquals(description, event.getCodePreview()),
				() -> assertEquals(0, event.getHighlightLength()),
				() -> assertEquals(539, event.getOffset()),
				() -> assertEquals(29, event.getLength()),
				() -> assertEquals(17, event.getLineNumber()),
				() -> assertEquals(20, event.getWeightValue()));
	}

	@Test
	void test_resolveMarkers_shouldResolveOne() throws Exception {
		UseParameterizedQueryResolver visitor = new UseParameterizedQueryResolver(node -> node.getStartPosition() == 540);
		visitor.addMarkerListener(RefactoringMarkers.getFor("UseParameterizedQueryResolver"));
		setVisitor(visitor);
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
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertEquals(1, events.size());
	}
}
