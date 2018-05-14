package eu.jsparrow.ui.util;

import java.util.Map;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.TableViewer;

/**
 * {@link IStructuredContentProvider} for {@link Map}. Used for
 * {@link TableViewer}. Taken from
 * {@link http://pinnau.blogspot.co.at/2015/08/jface-tableviewer-with-javautilmap-input.html}
 * 
 */
public class MapContentProvider implements IStructuredContentProvider {

	private static MapContentProvider instance;

	public static MapContentProvider getInstance() {
		synchronized (ArrayContentProvider.class) {
			if (instance == null) {
				instance = new MapContentProvider();
			}
			return instance;
		}
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Object[] getElements(Object inputElement) {
		if (inputElement == null)
			return new Object[0];

		if (inputElement instanceof Map) {
			return ((Map) inputElement).entrySet()
				.toArray();
		}

		throw new RuntimeException("Invalid input für MapContentProvider: " + inputElement.getClass()); //$NON-NLS-1$
	}
}