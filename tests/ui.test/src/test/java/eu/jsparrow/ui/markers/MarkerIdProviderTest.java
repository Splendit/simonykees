package eu.jsparrow.ui.markers;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.Tag;

class MarkerIdProviderTest {
	
	private MarkerIdProvider markerIdProvider;
	
	@BeforeEach
	void setUp() {
		Map<String, RuleDescription>  markersDescriptionMap = new HashMap<>();
		markersDescriptionMap.put("MarkerOne", new RuleDescription("Marker One", "Description one", Duration.ofMinutes(1), Tag.PERFORMANCE, Tag.READABILITY));
		markersDescriptionMap.put("MarkerTwo", new RuleDescription("Marker Two", "Description Two", Duration.ofMinutes(5), Tag.SECURITY, Tag.READABILITY));
		markerIdProvider = new MarkerIdProvider(markersDescriptionMap);
	}

	@Test
	void filterWithAvailableCredit_allMarkersAvailable_shouldReturnMarkerOne() {
		List<String> actual = markerIdProvider.filterWithSufficientCredit(1, Arrays.asList("MarkerOne", "MarkerTwo"));
		assertAll(
				() -> assertEquals(1, actual.size()),
				() -> assertTrue(actual.contains("MarkerOne")));
	}

	@Test
	void filterWithAvailableCredit_noMarkersAvailable_shouldReturnEmpty() {
		List<String> actual = markerIdProvider.filterWithSufficientCredit(1, Collections.emptyList());
		assertTrue(actual.isEmpty());
	}
	
	@Test
	void filterWithAvailableCredit_oneMarkerAvailable_shouldReturnEmpty() {
		List<String> actual = markerIdProvider.filterWithSufficientCredit(1, Collections.singletonList("MarkerTwo"));
		assertTrue(actual.isEmpty());
	}
}
