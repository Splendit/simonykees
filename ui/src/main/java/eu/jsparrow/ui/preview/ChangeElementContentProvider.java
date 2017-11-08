package eu.jsparrow.ui.preview;

import org.eclipse.jface.viewers.ITreeContentProvider;

/**
 * This class provides the content for the tree in FileTree
 * 
 * @author Andreja Sambolec
 * @since 2.1.1
 *
 */
public class ChangeElementContentProvider implements ITreeContentProvider {

	/*
	 * non Java-doc
	 * 
	 * @see ITreeContentProvider#getChildren
	 */
	@Override
	public Object[] getChildren(Object o) {
		if (o instanceof DocumentChangeWrapper) {
			return ((DocumentChangeWrapper) o).getChildren();
		}
		return new Object[] {};
	}

	/*
	 * non Java-doc
	 * 
	 * @see ITreeContentProvider#getParent
	 */
	public Object getParent(Object element) {
		return ((DocumentChangeWrapper) element).getParent();
	}

	/*
	 * non Java-doc
	 * 
	 * @see ITreeContentProvider#hasChildren
	 */
	public boolean hasChildren(Object element) {
		Object[] children = getChildren(element);
		return children != null && children.length > 0;
	}

	/*
	 * non Java-doc
	 * 
	 * @see ITreeContentProvider#getElements
	 */
	public Object[] getElements(Object element) {
		if (element instanceof DocumentChangeWrapper[]) {
			return (DocumentChangeWrapper[]) element;
		}
		return new Object[] {};
	}
}
