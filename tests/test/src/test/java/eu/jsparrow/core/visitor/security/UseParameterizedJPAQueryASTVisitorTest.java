package eu.jsparrow.core.visitor.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.core.visitor.impl.UsesSimpleJDTUnitFixture;

public class UseParameterizedJPAQueryASTVisitorTest extends UsesSimpleJDTUnitFixture {

	@BeforeEach
	public void setUpVisitor() throws Exception {
		addDependency("javax.persistence", "persistence-api", "1.0.2");
		fixture.addImport("javax.persistence.EntityManager");
		fixture.addImport("javax.persistence.Query");
		setVisitor(new UseParameterizedJPAQueryASTVisitor());
	}
	
	@Test
	public void visit_createQueryWithOneInputAsAssignmentRHS_shouldTransform() throws Exception {

		String original = "" + //
				"		String orderId = \"100000000\";\n" +
				"		EntityManager entityManager = null;\n" +
				"		Query jpqlQuery;\n" +
				"		jpqlQuery = entityManager.createQuery(\"Select order from Orders order where order.id = \" + orderId);\n";

		String expected = "" + //
				"		String orderId = \"100000000\";\n" +
				"		EntityManager entityManager = null;\n" +
				"		Query jpqlQuery;\n" +
				"		jpqlQuery = entityManager.createQuery(\"Select order from Orders order where order.id =  ?1\");\n"
				+
				"		jpqlQuery.setParameter(1, orderId);";

		assertChange(original, expected);

	}

	@Test
	public void visit_createQueryWithOneInput_shouldTransform() throws Exception {

		String original = "" + //
				"		String orderId = \"100000000\";\n" +
				"		EntityManager entityManager = null;\n" +
				"		Query jpqlQuery = entityManager.createQuery(\"Select order from Orders order where order.id = \" + orderId);";

		String expected = "" + //
				"		String orderId = \"100000000\";\n" +
				"		EntityManager entityManager = null;\n" +
				"		Query jpqlQuery = entityManager.createQuery(\"Select order from Orders order where order.id =  ?1\");\n"
				+
				"		jpqlQuery.setParameter(1, orderId);";

		assertChange(original, expected);

	}

	@Test
	public void visit_createQueryWithTwoInputs_shouldTransform() throws Exception {

		String original = "" + //
				"			String firstName = \"Max\";\n" +
				"			String lastName = \"Mustermann\";\n" +
				"			EntityManager entityManager = null;\n" +
				"			Query jpqlQuery = entityManager.createQuery( //\n" +
				"					\"Select id\"	+ \n" +
				"						\" from Persons p\" + \n" +
				"						\" where p.firstName = \" + firstName + \n" +
				"						\" and p.lastName = \" + lastName\n" +
				"			);";

		String expected = "" + //
				"			String firstName = \"Max\";\n" +
				"			String lastName = \"Mustermann\";\n" +
				"			EntityManager entityManager = null;\n" +
				"			Query jpqlQuery = entityManager.createQuery( //\n" +
				"					\"Select id\"	+ \n" +
				"						\" from Persons p\" + \n" +
				"						\" where p.firstName =  ?1\" + \n" +
				"						\" and p.lastName =  ?2\"\n" +
				"			);\n" +
				"			jpqlQuery.setParameter(1, firstName);\n" +
				"			jpqlQuery.setParameter(2, lastName);";

		assertChange(original, expected);

	}

}
