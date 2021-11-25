package eu.jsparrow.ui.preference.profile;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class DefaultActiveMarkers {

	private List<String> activeMarkers;

	@SuppressWarnings("nls")
	public DefaultActiveMarkers() {
		List<String> active = Arrays.asList(
				"eu.jsparrow.core.markers.visitor.EnumsWithoutEqualsResolver",
				"eu.jsparrow.core.markers.visitor.FunctionalInterfaceResolver",
				"eu.jsparrow.core.markers.visitor.InefficientConstructorResolver",
				"eu.jsparrow.core.markers.visitor.LambdaToMethodReferenceResolver",
				"eu.jsparrow.core.markers.visitor.PrimitiveBoxedForStringResolver",
				"eu.jsparrow.core.markers.visitor.PutIfAbsentResolver",
				"eu.jsparrow.core.markers.visitor.RemoveNullCheckBeforeInstanceofResolver",
				"eu.jsparrow.core.markers.visitor.StringLiteralEqualityCheckResolver",
				"eu.jsparrow.core.markers.visitor.UseComparatorMethodsResolver",
				"eu.jsparrow.core.markers.visitor.UseIsEmptyOnCollectionsResolver");
		this.activeMarkers = Collections.unmodifiableList(active);
	}

	public List<String> getActiveMarkers() {
		return activeMarkers;
	}
}
