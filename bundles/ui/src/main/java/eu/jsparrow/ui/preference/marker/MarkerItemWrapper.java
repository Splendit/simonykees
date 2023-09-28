package eu.jsparrow.ui.preference.marker;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import eu.jsparrow.ui.treeview.ICheckBoxTreeViewNode;

/**
 * A recursive data structure for the nodes of the {@code CheckboxTreeViewer} of
 * the markers preference page.
 * 
 * @since 4.10.0
 *
 */
public class MarkerItemWrapper implements ICheckBoxTreeViewNode<MarkerItemWrapper> {
	private MarkerItemWrapper parent;
	private boolean isParent;
	private String markerId;
	private String name;
	private List<MarkerItemWrapper> children = new ArrayList<>();

	public MarkerItemWrapper(MarkerItemWrapper parent, boolean isParent,
			String markerId, String name,
			List<MarkerItemWrapper> childern) {
		this.parent = parent;
		this.isParent = isParent;
		this.markerId = markerId;
		this.name = name;
		this.children = childern;
	}

	public String getMarkerId() {
		return markerId;
	}

	public String getName() {
		return name;
	}

	@Override
	public List<MarkerItemWrapper> getChildren() {
		return children;
	}

	public void addChild(String markerId, String markerName) {
		MarkerItemWrapper item = new MarkerItemWrapper(this, false, markerId, markerName, new ArrayList<>());
		this.children.add(item);
	}

	@Override
	public MarkerItemWrapper getParent() {
		return parent;
	}

	public boolean isParent() {
		return isParent;
	}

	@Override
	public boolean hasChildren() {
		return isParent && !children.isEmpty();
	}

	@Override
	public boolean hasChildListAtHand() {
		return  hasChildren();
	}

	@Override
	public String getLabelText() {
		return StringUtils.capitalize(getName());
	}
}