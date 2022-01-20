package org.eu.jsparrow.rules.java16.javarecords;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.common.UsesJDTUnitFixture;
import eu.jsparrow.rules.common.markers.RefactoringMarkerEvent;
import eu.jsparrow.rules.common.markers.RefactoringMarkers;
import eu.jsparrow.rules.java16.javarecords.UseJavaRecordsResolver;

class UseJavaRecordsResolverTest extends UsesJDTUnitFixture {

	@BeforeEach
	void setUp() throws Exception {
		RefactoringMarkers.clear();
	}

	@AfterEach
	public void tearDown() throws Exception {
		fixtureProject.clear();
	}

	String original = "" +
			"	private static final class NestedClassWithPrivateFinalIntX {\n"
			+ "\n"
			+ "		private final int x;\n"
			+ "\n"
			+ "		NestedClassWithPrivateFinalIntX (int x) {\n"
			+ "			this.x = x;\n"
			+ "		}\n"
			+ "	}";

	String expected = "" +
			"	private record NestedClassWithPrivateFinalIntX(int x) {\n"
			+ "	}";

	@Test
	void test_AlwaysFalsePredicate_shouldGenerateNoMarkers() throws Exception {
		UseJavaRecordsResolver visitor = new UseJavaRecordsResolver(node -> false);
		visitor.addMarkerListener(RefactoringMarkers.getFor("UseJavaRecordsResolver"));
		setDefaultVisitor(visitor);
		String original = "" +
				"	private static final class NestedClassWithPrivateFinalIntX {\n"
				+ "\n"
				+ "		private final int x;\n"
				+ "\n"
				+ "		NestedClassWithPrivateFinalIntX (int x) {\n"
				+ "			this.x = x;\n"
				+ "		}\n"
				+ "	}";

		assertNoChange(original);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertTrue(events.isEmpty());
	}

	@Test
	void test_markerGeneration_shouldGenerateOneMarkerEvent() throws Exception {
		UseJavaRecordsResolver visitor = new UseJavaRecordsResolver(node -> true);
		visitor.addMarkerListener(RefactoringMarkers.getFor("UseJavaRecordsResolver"));
		setDefaultVisitor(visitor);
		String original = "" +
				"	private static final class NestedClassWithPrivateFinalIntX {\n"
				+ "\n"
				+ "		private final int x;\n"
				+ "\n"
				+ "		NestedClassWithPrivateFinalIntX (int x) {\n"
				+ "			this.x = x;\n"
				+ "		}\n"
				+ "	}";

		defaultFixture.addTypeDeclarationFromString(DEFAULT_TYPE_DECLARATION_NAME, original);

		getDefaultVisitor().setASTRewrite(defaultFixture.getAstRewrite());
		defaultFixture.accept(getDefaultVisitor());
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertEquals(1, events.size());
		RefactoringMarkerEvent event = events.get(0);
		String description = ""
				+ "Since Java 16, record classes are a new kind of class in the Java language. Record classes help to model plain data aggregates "
				+ "with less ceremony than normal classes. This rule replaces the declarations of local classes, inner classes, and package "
				+ "private root classes with record class declarations.";

		assertAll(
				() -> assertEquals("Use Java Records", event.getName()),
				() -> assertEquals(description, event.getMessage()), 
				() -> assertEquals("UseJavaRecordsResolver", event.getResolver()),
				() -> assertEquals(description, event.getCodePreview()),
				() -> assertEquals(0, event.getHighlightLength()),
				() -> assertEquals(45, event.getOffset()),
				() -> assertEquals(178, event.getLength()),
				() -> assertEquals(5, event.getLineNumber()),
				() -> assertEquals(20, event.getWeightValue()));
	}

	@Test
	void test_resolveMarkers_shouldResolveOne() throws Exception {
		UseJavaRecordsResolver visitor = new UseJavaRecordsResolver(node -> node.getStartPosition() == 45);
		visitor.addMarkerListener(RefactoringMarkers.getFor("UseJavaRecordsResolver"));
		setDefaultVisitor(visitor);
		String original = "" +
				"	private static final class NestedClassWithPrivateFinalIntX {\n"
				+ "\n"
				+ "		private final int x;\n"
				+ "\n"
				+ "		NestedClassWithPrivateFinalIntX (int x) {\n"
				+ "			this.x = x;\n"
				+ "		}\n"
				+ "	}";
		defaultFixture.addTypeDeclarationFromString(DEFAULT_TYPE_DECLARATION_NAME, original);
		getDefaultVisitor().setASTRewrite(defaultFixture.getAstRewrite());
		defaultFixture.accept(getDefaultVisitor());
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertEquals(1, events.size());
	}
}
