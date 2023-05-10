package eu.jsparrow.ui.treeview;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CheckBoxSelectionStateStore<T extends ICheckBoxTreeViewNode<T>> {
	private final Set<T> selectedElements = new HashSet<>();
	private final Set<T> grayedElements = new HashSet<>();

	public void setSelectionState(T element, boolean checked) {
		storeElementWithSelectionState(element, checked, false);
		storeChildrenSelectionState(element, checked);
		storeParentSelectionState(element);
	}

	private void storeElementWithSelectionState(T element, boolean checked,
			boolean grayed) {
		if (checked) {
			selectedElements.add(element);
			grayedElements.remove(element);
		} else if (grayed) {
			selectedElements.remove(element);
			grayedElements.add(element);
		} else {
			selectedElements.remove(element);
			grayedElements.remove(element);
		}
	}

	private void storeChildrenSelectionState(T element, boolean checked) {
		if (element.hasChildListAtHand()) {
			List<T> children = element.getChildren();
			if (children != null) {
				for (T child : children) {
					storeElementWithSelectionState(child, checked, false);
					storeChildrenSelectionState(child, checked);
				}
			}
		}
	}

	private void storeParentSelectionState(T element) {
		T parent = element.getParent();
		if (parent == null) {
			return;
		}
		List<T> childrenOfParent = parent.getChildren();

		boolean checked;
		boolean greyed;
		if (areAllChildrenSelected(childrenOfParent)) {
			checked = true;
			greyed = false;
		} else if (areAllChildrenUnselected(childrenOfParent)) {
			checked = false;
			greyed = false;
		} else {
			checked = false;
			greyed = true;
		}
		storeElementWithSelectionState(parent, checked, greyed);
		storeParentSelectionState(parent);
	}

	private boolean areAllChildrenSelected(List<T> childrenOfParent) {
		for (T child : childrenOfParent) {
			if (!selectedElements.contains(child)) {
				return false;
			}
		}
		return true;
	}

	private boolean areAllChildrenUnselected(List<T> childrenOfParent) {
		for (T child : childrenOfParent) {
			if (selectedElements.contains(child)) {
				return false;
			}
			if (grayedElements.contains(child)) {
				return false;
			}
		}
		return true;
	}

	public Set<T> getSelectedElements() {
		return selectedElements;
	}

	public Set<T> getGrayedElements() {
		return grayedElements;
	}
}
