package eu.jsparrow.rules.common.markers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class RefactoringMarkers implements RefactoringMarkerListener {
	
	private static final Map<String, RefactoringMarkers> markers = new HashMap<>();
	private List<RefactoringMarkerEvent> markersPerResolver = new ArrayList<>();
	
	public static RefactoringMarkers getFor(String resolver) {
		markers.putIfAbsent(resolver, new RefactoringMarkers());
		return markers.get(resolver);
	}

	public static void clear() {
		markers.clear();
	}
	
	public static List<RefactoringMarkerEvent> getAllEvents() {
		return markers.values().stream()
				.map(me -> me.getEvents())
				.flatMap(List::stream)
				.collect(Collectors.toList());
	}

	@Override
	public void update(RefactoringMarkerEvent event) {
		markersPerResolver.add(event);
	}

	public List<RefactoringMarkerEvent> getEvents() {
		return markersPerResolver;
	}
}
