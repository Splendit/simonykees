package eu.jsparrow.ui.preview;

import java.util.Arrays;
import java.util.Comparator;

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
		} else if (o instanceof RemoveUnusedCodeDocumentChangeWrapper) {
			return ((RemoveUnusedCodeDocumentChangeWrapper) o).getChildren();
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
		if (element instanceof RemoveUnusedCodeDocumentChangeWrapper) {
			return ((RemoveUnusedCodeDocumentChangeWrapper) element).getParent();
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
			Comparator<DocumentChangeWrapper> comparator = Comparator.comparing(DocumentChangeWrapper::getOldIdentifier)
				.thenComparing(DocumentChangeWrapper::getCompilationUnitName);
			Arrays.asList((DocumentChangeWrapper[]) element)
				.sort(comparator);
			return (DocumentChangeWrapper[]) element;
		} else if (element instanceof RemoveUnusedCodeDocumentChangeWrapper[]) {
			Comparator<RemoveUnusedCodeDocumentChangeWrapper> comparator = Comparator
				.comparing(RemoveUnusedCodeDocumentChangeWrapper::getCompilationUnitName)
				.thenComparing(RemoveUnusedCodeDocumentChangeWrapper::getIdentifier);
			Arrays.asList((RemoveUnusedCodeDocumentChangeWrapper[]) element)
				.sort(comparator);
			return (RemoveUnusedCodeDocumentChangeWrapper[]) element;
		}
		return new Object[] {};
	}
}
