package eu.jsparrow.ui.treeview;

import java.util.List;

public interface ICheckBoxTreeViewNode<T extends ICheckBoxTreeViewNode<?>> {

	T getParent();

	List<T> getChildren();

	boolean hasChildren();

	/**
	 * 
	 * @return Expected to return true if the following two conditions are
	 *         fulfilled:
	 *         <ul>
	 *         <li>The node can have children.</li>
	 *         <li>The child list can be returned by the {@link #getChildren()}
	 *         method without the need of time consuming operations because -
	 *         for example - all possible children have been already loaded into
	 *         the child list.</li>
	 *         </ul>
	 *         On the other hand, if an implementation of
	 *         {@link #hasChildListAtHand()} returns true, then the
	 *         implementation of {@link #getChildren()} in the same class is
	 *         expected to return a list containing at least one child element.
	 */
	boolean hasChildListAtHand();

}
