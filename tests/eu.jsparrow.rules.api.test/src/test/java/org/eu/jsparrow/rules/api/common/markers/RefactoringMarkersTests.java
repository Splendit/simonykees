package org.eu.jsparrow.rules.api.common.markers;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

import eu.jsparrow.rules.api.test.dummies.DummyRefactoringEvent;
import eu.jsparrow.rules.api.test.dummies.DummyVisitor;
import eu.jsparrow.rules.common.markers.RefactoringMarkerEvent;
import eu.jsparrow.rules.common.markers.RefactoringMarkers;

class RefactoringMarkersTests {

	@Test
	void getForRule() {

		RefactoringMarkers rm = RefactoringMarkers.getFor(DummyVisitor.class.getName());
		RefactoringMarkerEvent event = new DummyRefactoringEvent("Event 1");
		rm.update(event);
		List<RefactoringMarkerEvent> events = rm.getEvents();
		assertAll(
				() -> assertEquals(1, events.size()),
				() -> assertEquals(event, events.get(0)));
	}
	
	@Test
	void getAllEvents() {

		RefactoringMarkers rm1 = RefactoringMarkers.getFor("Dummy1");
		RefactoringMarkers rm2 = RefactoringMarkers.getFor("Dummy2");

		RefactoringMarkerEvent event1 = new DummyRefactoringEvent("Event 1");
		RefactoringMarkerEvent event2 = new DummyRefactoringEvent("Event 1");
		rm1.update(event1);
		rm2.update(event2);                   
		
		List<RefactoringMarkerEvent> events1 = rm1.getEvents();
		List<RefactoringMarkerEvent> events2 = rm2.getEvents();
		List<RefactoringMarkerEvent> allEvents = RefactoringMarkers.getAllEvents();
		assertAll(
				() -> assertEquals(1, events1.size()),
				() -> assertEquals(event1, events1.get(0)),
				() -> assertEquals(1, events2.size()),
				() -> assertEquals(event2, events2.get(0)),
				() -> assertEquals(2, allEvents.size()));
	}

}
