package eu.jsparrow.core.markers.visitor.security;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.common.UsesSimpleJDTUnitFixture;
import eu.jsparrow.rules.common.markers.RefactoringMarkerEvent;
import eu.jsparrow.rules.common.markers.RefactoringMarkers;

class UseParameterizedJPAQueryResolverTest extends UsesSimpleJDTUnitFixture {
	
	@BeforeEach
	void setUpVisitor() throws Exception {
		RefactoringMarkers.clear();
		addDependency("javax.persistence", "persistence-api", "1.0.2");
		fixture.addImport("javax.persistence.EntityManager");
		fixture.addImport("javax.persistence.Query");
	}
	
	@Test
	void test_AlwaysFalsePredicate_shouldGenerateNoMarkers() throws Exception {
		UseParameterizedJPAQueryResolver visitor = new UseParameterizedJPAQueryResolver(node -> false);
		visitor.addMarkerListener(RefactoringMarkers.getFor("UseParameterizedJPAQueryResolver"));
		setVisitor(visitor);
		String original = "" + //
				"		String orderId = \"100000000\";\n" +
				"		EntityManager entityManager = null;\n" +
				"		Query jpqlQuery = entityManager.createQuery(\"Select order from Orders order where order.id = \" + orderId);\n"
				+
				"		jpqlQuery.getResultList();";

		assertNoChange(original);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertTrue(events.isEmpty());
	}

	@Test
	void test_markerGeneration_shouldGenerateOneMarkerEvent() throws Exception {
		UseParameterizedJPAQueryResolver visitor = new UseParameterizedJPAQueryResolver(node -> true);
		visitor.addMarkerListener(RefactoringMarkers.getFor("UseParameterizedJPAQueryResolver"));
		setVisitor(visitor);
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
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertEquals(1, events.size());
		RefactoringMarkerEvent event = events.get(0);
		String description = ""
				+ "A query string in the JPQL language (Java Persistence Query Language) may be constructed "
				+ "by concatenating string literals with user defined expressions, therefore they are also "
				+ "vulnerable to SQL injections.\n"
				+ "This rule looks for queries of type javax.persistence.Query which are created using the "
				+ "createQuery method on javax.persistence.EntityManager.\n"
				+ "The vulnerable concats of the JPQL query strings are parameterized, thus they can only "
				+ "be considered as data and not as code. ";
		
		assertAll(
				() -> assertEquals("Use Parameterized JPA Query", event.getName()),
				() -> assertEquals(description, event.getMessage()), 
				() -> assertEquals("UseParameterizedJPAQueryResolver", event.getResolver()),
				() -> assertEquals(description, event.getCodePreview()),
				() -> assertEquals(0, event.getHighlightLength()),
				() -> assertEquals(261, event.getOffset()),
				() -> assertEquals(87, event.getLength()),
				() -> assertEquals(10, event.getLineNumber()),
				() -> assertEquals(10, event.getWeightValue()));
	}

	@Test
	void test_resolveMarkers_shouldResolveOne() throws Exception {
		UseParameterizedJPAQueryResolver visitor = new UseParameterizedJPAQueryResolver(node -> node.getStartPosition() == 262);
		visitor.addMarkerListener(RefactoringMarkers.getFor("UseParameterizedJPAQueryResolver"));
		setVisitor(visitor);
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
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertEquals(1, events.size());
	}

}
