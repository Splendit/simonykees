package eu.jsparrow.ui.preference.marker;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

public class MarkerItemWrapperFilter extends ViewerFilter {

	private final Set<MarkerItemWrapper> matchingCategories;
	private final Set<MarkerItemWrapper> matchingMarkers;

	public MarkerItemWrapperFilter(MarkerTreeViewWrapper markertreeViewWrapper, String searchText) {

		List<MarkerItemWrapper> allMarkerItemWrappers = markertreeViewWrapper.getAllMarkerItemWrappers();

		matchingCategories = new HashSet<>();
		matchingMarkers = new HashSet<>();
		final String lowerCaseSearchText = StringUtils.lowerCase(searchText);

		for (MarkerItemWrapper category : allMarkerItemWrappers) {
			if (StringUtils.contains(StringUtils.lowerCase(category.getName()), lowerCaseSearchText)) {
				matchingCategories.add(category);
			}

			List<MarkerItemWrapper> markers = category.getChildren();
			for (MarkerItemWrapper marker : markers) {
				if (!matchingMarkers.contains(marker)
						&& StringUtils.contains(StringUtils.lowerCase(marker.getName()), lowerCaseSearchText)) {
					matchingMarkers.add(marker);
				}
			}
		}
	}

	@Override
	public boolean select(Viewer viewer, Object parentElement, Object element) {

		if (parentElement != null && matchingCategories.contains(parentElement)) {
			return true;
		}

		if (matchingCategories.contains(element) || matchingMarkers.contains(element)) {
			return true;
		}

		if (element instanceof MarkerItemWrapper) {
			MarkerItemWrapper markerItemWrapper = (MarkerItemWrapper) element;

			if (markerItemWrapper.isParent()) {
				return markerItemWrapper.getChildren()
					.stream()
					.anyMatch(matchingMarkers::contains);
			}
		}
		return false;
	}
}
