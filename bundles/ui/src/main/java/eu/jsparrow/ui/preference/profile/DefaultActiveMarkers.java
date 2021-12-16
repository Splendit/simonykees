package eu.jsparrow.ui.preference.profile;

import java.util.Collections;
import java.util.List;

import eu.jsparrow.core.markers.ResolverVisitorsFactory;

/**
 * Represents the list of the default active jSparrow Markers
 * 
 * @since 4.6.0
 *
 */
public class DefaultActiveMarkers {

	private List<String> activeMarkers;

	public DefaultActiveMarkers() {
		List<String> allMarkerIds = ResolverVisitorsFactory.getAllResolverIds();
		this.activeMarkers = Collections.unmodifiableList(allMarkerIds);
	}

	public List<String> getActiveMarkers() {
		return activeMarkers;
	}
}
