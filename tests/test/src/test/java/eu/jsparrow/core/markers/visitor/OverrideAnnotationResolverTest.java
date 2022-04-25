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

class OverrideAnnotationResolverTest extends UsesJDTUnitFixture {

	@BeforeEach
	void setUpVisitor() throws Exception {
		RefactoringMarkers.clear();
	}
	
	@AfterEach
	void tearDown() throws Exception {
		fixtureProject.clear();
	}

	@Test
	void test_AlwaysFalsePredicate_shouldGenerateNoMarkers() throws Exception {
		OverrideAnnotationResolver visitor = new OverrideAnnotationResolver(node -> false);
		visitor.addMarkerListener(RefactoringMarkers.getFor("OverrideAnnotationResolver"));
		setDefaultVisitor(visitor);
		String original = "" 
				+ "abstract class InnerType {\n"
				+ "	abstract void foo();\n"
				+ "}\n"
				+ "void initInnerType() {\n"
				+ "	final var innerTypeInstance = new InnerType() {\n"
				+ "		void foo() {\n"
				+ "			System.out.println(\"foo\");\n"
				+ "		}\n"
				+ "	};\n"
				+ "	innerTypeInstance.foo();\n"
				+ "}";

		assertNoChange(original);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertTrue(events.isEmpty());
	}

	@Test
	void test_markerGeneration_shouldGenerateOneMarkerEvent() throws Exception {
		OverrideAnnotationResolver visitor = new OverrideAnnotationResolver(node -> true);
		visitor.addMarkerListener(RefactoringMarkers.getFor("OverrideAnnotationResolver"));
		setDefaultVisitor(visitor);
		String original = ""
				+ "abstract class InnerType {\n"
				+ "	abstract void foo();\n"
				+ "}\n"
				+ "void initInnerType() {\n"
				+ "	final var innerTypeInstance = new InnerType() {\n"
				+ "		void foo() {\n"
				+ "			System.out.println(\"foo\");\n"
				+ "		}\n"
				+ "	};\n"
				+ "	innerTypeInstance.foo();\n"
				+ "}";
		String expected = ""
				+ "abstract class InnerType {\n"
				+ "	abstract void foo();\n"
				+ "}\n"
				+ "void initInnerType() {\n"
				+ "	final var innerTypeInstance = new InnerType() {\n"
				+ "		@Override void foo() {\n"
				+ "			System.out.println(\"foo\");\n"
				+ "		}\n"
				+ "	};\n"
				+ "	innerTypeInstance.foo();\n"
				+ "}";

		assertChange(original, expected);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertEquals(1, events.size());
		RefactoringMarkerEvent event = events.get(0);
		String description = ""
				+ "This rule adds the @Override annotation to methods overriding or implementing another method declared in a parent class. Even though using @Override it is not mandatory, using this annotation is considered a best practice for two main reasons: \n"
				+ " 1) It ensures that the method signature is a subsignature of the overridden method (otherwise, a compile error is indicated). \n"
				+ " 2) It improves the readability. ";
		
		assertAll(
				() -> assertEquals("Use @Override Annotation", event.getName()),
				() -> assertEquals(description, event.getMessage()), 
				() -> assertEquals("OverrideAnnotationResolver", event.getResolver()),
				() -> assertEquals(description, event.getCodePreview()),
				() -> assertEquals(0, event.getHighlightLength()),
				() -> assertEquals(207, event.getOffset()),
				() -> assertEquals(3, event.getLength()),
				() -> assertEquals(10, event.getLineNumber()),
				() -> assertEquals(5, event.getWeightValue()));
	}

	@Test
	void test_resolveMarkers_shouldResolveOne() throws Exception {
		OverrideAnnotationResolver visitor = new OverrideAnnotationResolver(node -> node.getStartPosition() == 207);
		visitor.addMarkerListener(RefactoringMarkers.getFor("OverrideAnnotationResolver"));
		setDefaultVisitor(visitor);
		String original = ""
				+ "abstract class InnerType {\n"
				+ "	abstract void foo();\n"
				+ "}\n"
				+ "void initInnerType() {\n"
				+ "	final var innerTypeInstance = new InnerType() {\n"
				+ "		void foo() {\n"
				+ "			System.out.println(\"foo\");\n"
				+ "		}\n"
				+ "	};\n"
				+ "	innerTypeInstance.foo();\n"
				+ "}";
		String expected = ""
				+ "abstract class InnerType {\n"
				+ "	abstract void foo();\n"
				+ "}\n"
				+ "void initInnerType() {\n"
				+ "	final var innerTypeInstance = new InnerType() {\n"
				+ "		@Override void foo() {\n"
				+ "			System.out.println(\"foo\");\n"
				+ "		}\n"
				+ "	};\n"
				+ "	innerTypeInstance.foo();\n"
				+ "}";
		assertChange(original, expected);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertEquals(1, events.size());
	}
}
