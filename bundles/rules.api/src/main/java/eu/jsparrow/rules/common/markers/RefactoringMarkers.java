package eu.jsparrow.rules.common.markers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Maintains channels for {@link RefactoringMarkerEvent}s.
 *
 * @since 4.0.0
 */
public class RefactoringMarkers implements RefactoringMarkerListener {

	private static final Map<String, RefactoringMarkers> markers = new HashMap<>();
	private List<RefactoringMarkerEvent> markersPerResolver = new ArrayList<>();

	/**
	 * 
	 * @param resolver
	 *            resolver name (i.e., the fully qualified name of a resolver).
	 * @return a channel listening for events from a specific resolver.
	 */
	public static RefactoringMarkers getFor(String resolver) {
		markers.putIfAbsent(resolver, new RefactoringMarkers());
		return markers.get(resolver);
	}

	public static void clear() {
		markers.clear();
	}

	/**
	 * 
	 * @return all events from all recorded channels.
	 */
	public static List<RefactoringMarkerEvent> getAllEvents() {
		return markers.values()
			.stream()
			.map(RefactoringMarkers::getEvents)
			.flatMap(List::stream)
			.collect(Collectors.toList());
	}

	@Override
	public void update(RefactoringMarkerEvent event) {
		markersPerResolver.add(event);
	}

	/**
	 * 
	 * @return the events recorded for this channel.
	 */
	public List<RefactoringMarkerEvent> getEvents() {
		return markersPerResolver;
	}
}
