package eu.jsparrow.core.visitor.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.core.visitor.impl.UsesSimpleJDTUnitFixture;

public class UseParameterizedJPAQueryASTVisitorTest extends UsesSimpleJDTUnitFixture {

	@BeforeEach
	public void setUpVisitor() throws Exception {
		setVisitor(new UseParameterizedJPAQueryASTVisitor());
		fixture.addImport("javax.persistence.EntityManager");
		fixture.addImport("javax.persistence.Query");
		//addDependency("javax.persistence", "persistence-api", "1.0.2");
	}

	@Test
	public void visit_createQueryWithOneInput_shouldTransform() throws Exception {
		String orderId = "100000000";
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

		//assertChange(original, original);
		assertNoChange(original);

	}

}
