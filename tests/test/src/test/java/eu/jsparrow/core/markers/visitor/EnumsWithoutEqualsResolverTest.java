package eu.jsparrow.core.markers.visitor;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.common.UsesSimpleJDTUnitFixture;
import eu.jsparrow.rules.common.markers.RefactoringMarkerEvent;
import eu.jsparrow.rules.common.markers.RefactoringMarkers;

class EnumsWithoutEqualsResolverTest extends UsesSimpleJDTUnitFixture {

	@BeforeEach
	void setUp() throws Exception {
		fixture.addImport("java.math.RoundingMode");
		RefactoringMarkers.clear();
	}
	
	@Test
	void test_AlwaysFalsePredicate_shouldGenerateNoMarkers() throws Exception {
		EnumsWithoutEqualsResolver visitor = new EnumsWithoutEqualsResolver(node -> false);
		visitor.addMarkerListener(RefactoringMarkers.getFor("eu.jsparrow.core.markers.visitor.EnumsWithoutEqualsResolver"));
		setVisitor(visitor);
		String original = "RoundingMode roundingMode = RoundingMode.UP; if(roundingMode.equals(RoundingMode.UP)){}";

		assertNoChange(original);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertTrue(events.isEmpty());
	}
	
	@Test
	void test_markerGeneration_shouldGenerateOneMarkerEvent() throws Exception {
		EnumsWithoutEqualsResolver visitor = new EnumsWithoutEqualsResolver(node -> true);
		visitor.addMarkerListener(RefactoringMarkers.getFor("eu.jsparrow.core.markers.visitor.EnumsWithoutEqualsResolver"));
		setVisitor(visitor);
		String original = "RoundingMode roundingMode = RoundingMode.UP; if(roundingMode.equals(RoundingMode.UP)){}";
		String expected = "RoundingMode roundingMode = RoundingMode.UP; if(roundingMode == RoundingMode.UP){}";
		assertChange(original, expected);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertEquals(1, events.size());
		RefactoringMarkerEvent event = events.get(0);
		assertAll(
				() -> assertEquals("Replace equals() on Enum constants", event.getName()),
				() -> assertEquals("Replace occurrences of equals() on Enum constants with an identity comparison (==).", event.getMessage()), 
				() -> assertEquals("eu.jsparrow.core.markers.visitor.EnumsWithoutEqualsResolver", event.getResolver()),
				() -> assertEquals("roundingMode == RoundingMode.UP", event.getCodePreview()),
				() -> assertEquals(31, event.getHighlightLength()),
				() -> assertEquals(177, event.getOffset()),
				() -> assertEquals(36, event.getLength()),
				() -> assertEquals(1, event.getWeightValue()));
	}
	
	@Test
	void test_resolveMarkers_shouldResolveOne() throws Exception {
		EnumsWithoutEqualsResolver visitor = new EnumsWithoutEqualsResolver(node -> node.getStartPosition() == 178);
		visitor.addMarkerListener(RefactoringMarkers.getFor("eu.jsparrow.core.markers.visitor.EnumsWithoutEqualsResolver"));
		setVisitor(visitor);
		String original = ""
				+ "RoundingMode roundingMode = RoundingMode.UP; if(roundingMode.equals(RoundingMode.UP)){}"
				+ "RoundingMode roundingMode2 = RoundingMode.UP; if(roundingMode2.equals(RoundingMode.UP)){}";
		String expected = ""
				+ "RoundingMode roundingMode = RoundingMode.UP; if(roundingMode == RoundingMode.UP){}"
				+ "RoundingMode roundingMode2 = RoundingMode.UP; if(roundingMode2.equals(RoundingMode.UP)){}";
		assertChange(original, expected);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertEquals(1, events.size());
	}
}
