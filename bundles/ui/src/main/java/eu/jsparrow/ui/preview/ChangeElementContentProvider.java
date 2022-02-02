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
		} else if (o instanceof RemoveDeadCodeDocumentChangeWrapper) {
			return ((RemoveDeadCodeDocumentChangeWrapper) o).getChildren();
		}
		return new Object[] {};
	}

	/*
	 * non Java-doc
	 * 
	 * @see ITreeContentProvider#getParent
	 */
	@Override
	public Object getParent(Object element) {
		if(element instanceof RemoveDeadCodeDocumentChangeWrapper) {
			return ((RemoveDeadCodeDocumentChangeWrapper) element).getParent();
		}
		return ((DocumentChangeWrapper) element).getParent();
	}

	/*
	 * non Java-doc
	 * 
	 * @see ITreeContentProvider#hasChildren
	 */
	@Override
	public boolean hasChildren(Object element) {
		Object[] children = getChildren(element);
		return children != null && children.length > 0;
	}

	/*
	 * non Java-doc
	 * 
	 * @see ITreeContentProvider#getElements
	 */
	@Override
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
		} else if (element instanceof RemoveDeadCodeDocumentChangeWrapper[]) {
			Arrays.asList((RemoveDeadCodeDocumentChangeWrapper[]) element)
			.sort((e1, e2) -> {
				if(e1.getIdentifier().equals(e2.getIdentifier())) {
					return e1.getCompilationUnitName().compareTo(e2.getCompilationUnitName());
				}
				return e1.getIdentifier().compareTo(e2.getIdentifier());
			});
			return (RemoveDeadCodeDocumentChangeWrapper[]) element;
		}
		return new Object[] {};
	}
}
