package eu.jsparrow.ui.preference.marker;

import java.util.ArrayList;
import java.util.List;

public class MarkerItemWrapper {
	private MarkerItemWrapper parent;
	private boolean isParent;
	private String markerId;
	private String name;
	private List<MarkerItemWrapper> childern = new ArrayList<>();

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
}