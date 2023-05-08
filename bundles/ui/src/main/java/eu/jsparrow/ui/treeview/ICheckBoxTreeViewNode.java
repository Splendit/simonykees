package eu.jsparrow.ui.treeview;

public interface ICheckBoxTreeViewNode {

	Object getParent();

	Object[] getChildrenAsObjectArray();

	boolean hasChildren();

}
