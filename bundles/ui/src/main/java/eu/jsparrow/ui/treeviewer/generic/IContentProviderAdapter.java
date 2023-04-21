package eu.jsparrow.ui.treeviewer.generic;

public interface IContentProviderAdapter {

	boolean isParent();

	IContentProviderAdapter getParent();

	String getComparisonKey();

	Object[] getChildrenAsObjectArray();

	boolean hasChildren();
}
