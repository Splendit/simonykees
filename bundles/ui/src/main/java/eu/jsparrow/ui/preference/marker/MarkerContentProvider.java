package eu.jsparrow.ui.preference.marker;

import java.util.Arrays;
import java.util.Comparator;

import org.eclipse.jface.viewers.ITreeContentProvider;

/**
 * A content provider for the markers preference page tree.
 * 
 * @since 4.10.0
 *
 */
public class MarkerContentProvider implements ITreeContentProvider {

	@Override
	public Object[] getElements(Object inputElement) {
		Comparator<MarkerItemWrapper> comparator = Comparator
			.comparing(MarkerItemWrapper::getName);
		if (inputElement instanceof MarkerItemWrapper[]) {
			Arrays.asList((MarkerItemWrapper[]) inputElement)
				.sort(comparator);
			return (MarkerItemWrapper[]) inputElement;
		}
		return new Object[] {};
	}

	@Override
	public Object[] getChildren(Object parentElement) {
		if (parentElement instanceof MarkerItemWrapper) {
			MarkerItemWrapper markerItemWrapper = (MarkerItemWrapper) parentElement;
			return markerItemWrapper.getChildern()
				.toArray();
		}
		return new Object[] {};
	}

	@Override
	public Object getParent(Object element) {
		return ((MarkerItemWrapper) element).getParent();
	}

	@Override
	public boolean hasChildren(Object element) {
		Object[] children = getChildren(element);
		return children != null && children.length > 0;
	}

}