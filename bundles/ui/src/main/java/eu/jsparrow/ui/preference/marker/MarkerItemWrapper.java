package eu.jsparrow.ui.preference.marker;

import java.util.ArrayList;
import java.util.List;

/**
 * A recursive data structure for the nodes of the {@link CheckboxTreeViewer} of
 * the markers preference page.
 * 
 * @since 4.10.0
 *
 */
public class MarkerItemWrapper {
	private MarkerItemWrapper parent;
	private boolean isParent;
	private String markerId;
	private String name;
	private List<MarkerItemWrapper> childern = new ArrayList<>();
	private boolean expanded;

	public MarkerItemWrapper(MarkerItemWrapper parent, boolean isParent,
			String markerId, String name,
			List<MarkerItemWrapper> childern) {
		this.parent = parent;
		this.isParent = isParent;
		this.markerId = markerId;
		this.name = name;
		this.childern = childern;
	}

	public MarkerItemWrapper getParent() {
		return parent;
	}

	public boolean isParent() {
		return isParent;
	}

	public String getMarkerId() {
		return markerId;
	}

	public String getName() {
		return name;
	}

	public List<MarkerItemWrapper> getChildern() {
		return childern;
	}

	public void addChild(String markerId, String markerName) {
		MarkerItemWrapper item = new MarkerItemWrapper(this, false, markerId, markerName, new ArrayList<>());
		this.childern.add(item);
	}

	public boolean isExpanded() {
		return expanded;
	}

	public void setExpanded(boolean expanded) {
		this.expanded = expanded;
	}	
	
}