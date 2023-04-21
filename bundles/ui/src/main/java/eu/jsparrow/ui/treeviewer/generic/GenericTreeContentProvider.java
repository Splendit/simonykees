package eu.jsparrow.ui.treeviewer.generic;

import java.util.Arrays;

import org.eclipse.jface.viewers.ITreeContentProvider;

/**
 * A content provider for instances of {@link IContentProviderAdapter}
 * 
 * @since 4.17.0
 *
 */
public class GenericTreeContentProvider implements ITreeContentProvider {

	@Override
	public Object[] getElements(Object inputElement) {
		if (inputElement instanceof IContentProviderAdapter[]) {
			return Arrays.asList((IContentProviderAdapter[]) inputElement)
				.toArray();
		}
		return new Object[] {};
	}

	@Override
	public Object[] getChildren(Object parentElement) {
		if (parentElement instanceof IContentProviderAdapter) {
			IContentProviderAdapter adapter = (IContentProviderAdapter) parentElement;
			return adapter.getChildrenAsObjectArray();
		}
		return new Object[] {};
	}

	@Override
	public Object getParent(Object element) {
		if (element instanceof IContentProviderAdapter) {
			return ((IContentProviderAdapter) element).getParent();
		}
		return null;
	}

	@Override
	public boolean hasChildren(Object element) {
		if (element instanceof IContentProviderAdapter) {
			return ((IContentProviderAdapter) element).hasChildren();
		}
		return false;
	}

}