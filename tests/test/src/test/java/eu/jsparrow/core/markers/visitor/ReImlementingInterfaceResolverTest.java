package eu.jsparrow.core.markers.visitor;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.common.UsesJDTUnitFixture;
import eu.jsparrow.rules.common.markers.RefactoringMarkerEvent;
import eu.jsparrow.rules.common.markers.RefactoringMarkers;

class ReImlementingInterfaceResolverTest extends UsesJDTUnitFixture {
	@BeforeEach
	void setUpVisitor() throws Exception {
		defaultFixture.addImport(java.io.Serializable.class.getName());
		RefactoringMarkers.clear();
	}
	
	@AfterEach
	void tearDown() throws Exception {
		fixtureProject.clear();
	}

	@Test
	void test_AlwaysFalsePredicate_shouldGenerateNoMarkers() throws Exception {
		ReImplementingInterfaceResolver visitor = new ReImplementingInterfaceResolver(node -> false);
		visitor.addMarkerListener(RefactoringMarkers.getFor("ReImplementingInterfaceResolver"));
		setDefaultVisitor(visitor);
		String original = "" +
				"class ReimplementingInterface extends Parent implements Comparable<User>, Foo, Serializable {\n"
				+ "	@Override\n"
				+ "	public int compareTo(User o) {\n"
				+ "		return 0;\n"
				+ "	}\n"
				+ "}\n"
				+ "\n"
				+ "class Parent implements Comparable<User>, Foo {\n"
				+ "	@Override\n"
				+ "	public int compareTo(User o) {\n"
				+ "		return 0;\n"
				+ "	}\n"
				+ "}\n"
				+ "\n"
				+ "interface Foo {}\n"
				+ "\n"
				+ "class User{}" +
				"";

		assertNoChange(original);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertTrue(events.isEmpty());
	}

	@Test
	void test_markerGeneration_shouldGenerateOneMarkerEvent() throws Exception {
		ReImplementingInterfaceResolver visitor = new ReImplementingInterfaceResolver(node -> true);
		visitor.addMarkerListener(RefactoringMarkers.getFor("ReImplementingInterfaceResolver"));
		setDefaultVisitor(visitor);
		String original = "" +
				"class ReimplementingInterface extends Parent implements Comparable<User>, Foo, Serializable {\n"
				+ "	@Override\n"
				+ "	public int compareTo(User o) {\n"
				+ "		return 0;\n"
				+ "	}\n"
				+ "}\n"
				+ "\n"
				+ "class Parent implements Comparable<User>, Foo {\n"
				+ "	@Override\n"
				+ "	public int compareTo(User o) {\n"
				+ "		return 0;\n"
				+ "	}\n"
				+ "}\n"
				+ "\n"
				+ "interface Foo {}\n"
				+ "\n"
				+ "class User{}" +
				"";
		String expected = "" +
				"class ReimplementingInterface extends Parent implements Serializable {\n"
				+ "    @Override public int compareTo(    User o){\n"
				+ "      return 0;\n"
				+ "    }\n"
				+ "  }\n"
				+ "class Parent implements Comparable<User>, Foo {\n"
				+ "    @Override public int compareTo(    User o){\n"
				+ "      return 0;\n"
				+ "    }\n"
				+ "  }\n"
				+ "interface Foo {\n"
				+ "  }\n"
				+ "class User {\n"
				+ "}\n"
				+ "";

		assertChange(original, expected);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertEquals(2, events.size());
		RefactoringMarkerEvent event = events.get(0);
		String description = ""
				+ "Remove interfaces from class declaration which are already implemented by a super class. "
				+ "These interfaces are inherited from the super class.";
		
		assertAll(
				() -> assertEquals("Remove Inherited Interfaces from Class Declaration", event.getName()),
				() -> assertEquals(description, event.getMessage()), 
				() -> assertEquals("ReImplementingInterfaceResolver", event.getResolver()),
				() -> assertEquals(description, event.getCodePreview()),
				() -> assertEquals(0, event.getHighlightLength()),
				() -> assertEquals(131, event.getOffset()),
				() -> assertEquals(16, event.getLength()),
				() -> assertEquals(7, event.getLineNumber()),
				() -> assertEquals(2, event.getWeightValue()));
	}

	@Test
	void test_resolveMarkers_shouldResolveOne() throws Exception {
		ReImplementingInterfaceResolver visitor = new ReImplementingInterfaceResolver(node -> node.getStartPosition() == 131);
		visitor.addMarkerListener(RefactoringMarkers.getFor("ReImplementingInterfaceResolver"));
		setDefaultVisitor(visitor);
		String original = "" +
				"class ReimplementingInterface extends Parent implements Comparable<User>, Foo, Serializable {\n"
				+ "	@Override\n"
				+ "	public int compareTo(User o) {\n"
				+ "		return 0;\n"
				+ "	}\n"
				+ "}\n"
				+ "\n"
				+ "class Parent implements Comparable<User>, Foo {\n"
				+ "	@Override\n"
				+ "	public int compareTo(User o) {\n"
				+ "		return 0;\n"
				+ "	}\n"
				+ "}\n"
				+ "\n"
				+ "interface Foo {}\n"
				+ "\n"
				+ "class User{}" +
				"";
		String expected = ""
				+ "class ReimplementingInterface extends Parent implements Serializable {\n"
				+ "    @Override public int compareTo(    User o){\n"
				+ "      return 0;\n"
				+ "    }\n"
				+ "  }\n"
				+ "class Parent implements Comparable<User>, Foo {\n"
				+ "    @Override public int compareTo(    User o){\n"
				+ "      return 0;\n"
				+ "    }\n"
				+ "  }\n"
				+ "interface Foo {\n"
				+ "  }\n"
				+ "class User {\n"
				+ "}"
				+ "";
		assertChange(original, expected);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertEquals(2, events.size());
	}
}
