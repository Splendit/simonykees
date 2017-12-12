package eu.jsparrow.ui.preview;

import java.util.Arrays;

import org.eclipse.jface.viewers.ITreeContentProvider;

/**
 * This class provides the content for the tree in FileTree
 * 
 * @author Andreja Sambolec
 * @since 2.3.0
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
			Arrays.asList((DocumentChangeWrapper[]) element)
				.sort((e1, e2) -> {
					if (e1.getOldIdentifier()
						.equals(e2.getOldIdentifier())) {
						return e1.getCompilationUnitName()
							.compareTo(e2.getCompilationUnitName());
					}
					return e1.getOldIdentifier()
						.compareTo(e2.getOldIdentifier());
				});
			return (DocumentChangeWrapper[]) element;
		}
		return new Object[] {};
	}
}
