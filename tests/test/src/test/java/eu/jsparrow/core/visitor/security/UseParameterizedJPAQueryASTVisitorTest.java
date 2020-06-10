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
	public void visit_createQueryInMethod_shouldTransform() throws Exception {

		String original = "" + //
				"		String orderId = \"100000000\";\n" +
				"		EntityManager entityManager = null;\n" +
				"		Query jpqlQuery = entityManager.createQuery(\"Select order from Orders order where order.id = \" + orderId);\n"
				+
				"		jpqlQuery.getResultList();";

		String expected = "" + //
				"		String orderId = \"100000000\";\n" +
				"		EntityManager entityManager = null;\n" +
				"		Query jpqlQuery = entityManager.createQuery(\"Select order from Orders order where order.id =  ?1\");\n"
				+
				"		jpqlQuery.setParameter(1, orderId);\n" +
				"		jpqlQuery.getResultList();";

		assertChange(original, expected);

	}

	@Test
	public void visit_useRelationalGT_shouldTransform() throws Exception {
		String original = "" +
				"			int price = 1000;\n" +
				"			EntityManager entityManager = null;\n" +
				"			Query jpqlQuery = entityManager.createQuery(\"Select order from Orders order where order.price > \" + price);\n"
				+
				"			jpqlQuery.getResultList();";
		String expected = "" +
				"			int price = 1000;\n" +
				"			EntityManager entityManager = null;\n" +
				"			Query jpqlQuery = entityManager.createQuery(\"Select order from Orders order where order.price >  ?1\");\n"
				+
				"			jpqlQuery.setParameter(1, price);\n" +
				"			jpqlQuery.getResultList();";

		assertChange(original, expected);

	}

	@Test
	public void visit_useRelationalLT_shouldTransform() throws Exception {
		String original = "" +
				"			int price = 1000;\n" +
				"			EntityManager entityManager = null;\n" +
				"			Query jpqlQuery = entityManager.createQuery(\"Select order from Orders order where order.price < \" + price);\n"
				+
				"			jpqlQuery.getResultList();";

		String expected = "" +
				"			int price = 1000;\n" +
				"			EntityManager entityManager = null;\n" +
				"			Query jpqlQuery = entityManager.createQuery(\"Select order from Orders order where order.price <  ?1\");\n"
				+
				"			jpqlQuery.setParameter(1, price);\n" +
				"			jpqlQuery.getResultList();";

		assertChange(original, expected);

	}

	@Test
	public void visit_createQueryInInitializer_shouldTransform() throws Exception {

		String original = "" + //
				"class LocalClass {\n" +
				"	{\n" +
				"		EntityManager entityManager = null;\n" +
				"		String orderId = \"100000000\";\n" +
				"		Query jpqlQuery = entityManager.createQuery(\"Select order from Orders order where order.id = \" + orderId);\n"
				+
				"		jpqlQuery.getResultList();\n" +
				"	}\n" +
				"}";

		String expected = "" + //
				"class LocalClass {\n" +
				"	{\n" +
				"		EntityManager entityManager = null;\n" +
				"		String orderId = \"100000000\";\n" +
				"		Query jpqlQuery = entityManager.createQuery(\"Select order from Orders order where order.id =  ?1\");\n"
				+
				"		jpqlQuery.setParameter(1, orderId);\n" +
				"		jpqlQuery.getResultList();\n" +
				"	}\n" +
				"}";

		assertChange(original, expected);

	}

	@Test
	public void visit_createQueryInitializedAfterDeclaration_shouldTransform() throws Exception {

		String original = "" + //
				"		String orderId = \"100000000\";\n" +
				"		EntityManager entityManager = null;\n" +
				"		Query jpqlQuery;\n" +
				"		jpqlQuery = entityManager.createQuery(\"Select order from Orders order where order.id = \" + orderId);\n"
				+
				"		jpqlQuery.getResultList();";

		String expected = "" + //
				"		String orderId = \"100000000\";\n" +
				"		EntityManager entityManager = null;\n" +
				"		Query jpqlQuery;\n" +
				"		jpqlQuery = entityManager.createQuery(\"Select order from Orders order where order.id =  ?1\");\n"
				+
				"		jpqlQuery.setParameter(1, orderId);\n" +
				"		jpqlQuery.getResultList();";

		assertChange(original, expected);

	}

	@Test
	public void visit_ReassignQueryInitializedWithNull_shouldTransform() throws Exception {
		String original = "" +
				"		String orderId = \"100000000\";\n" +
				"		EntityManager entityManager = null;\n" +
				"		Query jpqlQuery = null;\n" +
				"		jpqlQuery = entityManager.createQuery(\"Select order from Orders order where order.id = \" + orderId);\n"
				+
				"		jpqlQuery.getResultList();";

		String expected = "" +
				"		String orderId = \"100000000\";\n" +
				"		EntityManager entityManager = null;\n" +
				"		Query jpqlQuery = null;\n" +
				"		jpqlQuery = entityManager.createQuery(\"Select order from Orders order where order.id =  ?1\");\n"
				+
				"		jpqlQuery.setParameter(1, orderId);\n" +
				"		jpqlQuery.getResultList();";

		assertChange(original, expected);
	}

	@Test
	public void visit_createQueryWithTwoInputs_shouldTransform() throws Exception {

		String original = "" + //
				"		String firstName = \"Max\";\n" +
				"			String lastName = \"Mustermann\";\n" +
				"			EntityManager entityManager = null;\n" +
				"			Query jpqlQuery = entityManager.createQuery( //\n" +
				"					\"Select id from Persons p where p.firstName = \" + firstName + \n" +
				"					\" and p.lastName = \" + lastName);\n" +
				"			jpqlQuery.getResultList();";

		String expected = "" + //
				"		String firstName=\"Max\";\n" +
				"			String lastName=\"Mustermann\";\n" +
				"			EntityManager entityManager=null;\n" +
				"			Query jpqlQuery=entityManager.createQuery(\"Select id from Persons p where p.firstName =  ?1\" + \" and p.lastName =  ?2\");\n"
				+
				"			jpqlQuery.setParameter(1,firstName);\n" +
				"			jpqlQuery.setParameter(2,lastName);\n" +
				"			jpqlQuery.getResultList();";

		assertChange(original, expected);

	}

	@Test
	public void visit_UpdateQuery_shouldTransformAfterWhere() throws Exception {

		String original = "" +
				"	EntityManager entityManager = null;\n" +
				"			int salary = 100000000;\n" +
				"			int persId = 111111111;\n" +
				"			Query jpqlQuery = entityManager\n" +
				"					.createQuery(\"UPDATE employee SET salary  = \" + salary + \" WHERE id = \" + persId);\n"
				+
				"			jpqlQuery.executeUpdate();";

		String expected = "" +
				"	EntityManager entityManager=null;\n" +
				"		int salary=100000000;\n" +
				"		int persId=111111111;\n" +
				"		Query jpqlQuery=entityManager.createQuery(\"UPDATE employee SET salary  = \" + salary + \" WHERE id =  ?1\");\n"
				+
				"		jpqlQuery.setParameter(1,persId);\n" +
				"		jpqlQuery.executeUpdate();";

		assertChange(original, expected);
	}

}
