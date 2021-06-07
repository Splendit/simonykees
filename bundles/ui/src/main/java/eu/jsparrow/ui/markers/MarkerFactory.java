package eu.jsparrow.ui.markers;

import static eu.jsparrow.ui.markers.JSparrowMarkerPropertyKeys.CODE_PREVIEW_KEY;
import static eu.jsparrow.ui.markers.JSparrowMarkerPropertyKeys.HIGHLIGHT_LENGTH_KEY;
import static eu.jsparrow.ui.markers.JSparrowMarkerPropertyKeys.NAME_KEY;
import static eu.jsparrow.ui.markers.JSparrowMarkerPropertyKeys.RESOLVER_KEY;
import static eu.jsparrow.ui.markers.JSparrowMarkerPropertyKeys.JSPARROW_MARKER_COLOR_KEY;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.jsparrow.rules.common.markers.RefactoringMarkerEvent;
import eu.jsparrow.ui.Activator;

/**
 * A factory class for creating {@link IMarker}s for the generated
 * {@link RefactoringMarkerEvent}s.
 * 
 * @since 3.31.0
 *
 */
public class MarkerFactory {

	private static final Logger logger = LoggerFactory.getLogger(MarkerFactory.class);

	public static final String JSPARROW_MARKER = "jsparrow.marker"; //$NON-NLS-1$

	private String markerColor;

	public MarkerFactory(String markerColor) {
		this.markerColor = markerColor;
	}

	/**
	 * Creates an {@link IMarker} for the given {@link RefactoringMarkerEvent}.
	 * 
	 * @param event
	 *            the event to create the {@link IMarker} for.
	 */
	public void create(RefactoringMarkerEvent event) {
		IJavaElement javaElement = event.getJavaElement();
		IResource resource = javaElement.getResource();
		String resolver = event.getResolver();
		String name = event.getName();
		String message = event.getMessage();
		int offset = event.getOffset();
		int length = event.getLength();
		int highlightLength = event.getHighlightLength();
		String codePreview = event.getCodePreview();
		String[] markerAttributeKeys = {
				RESOLVER_KEY, NAME_KEY,
				IMarker.MESSAGE,
				IMarker.CHAR_START,
				IMarker.CHAR_END,
				HIGHLIGHT_LENGTH_KEY,
				JSPARROW_MARKER_COLOR_KEY,
				CODE_PREVIEW_KEY,
				IMarker.SOURCE_ID };
		Object[] attributeValues = {
				resolver,
				name,
				message,
				Integer.valueOf(offset),
				Integer.valueOf(offset + length),
				Integer.valueOf(highlightLength),
				markerColor,
				codePreview,
				Activator.PLUGIN_ID };
		try {
			createWithProperties(resource, markerAttributeKeys, attributeValues);
		} catch (CoreException e) {
			logger.error(e.getMessage(), e);
		}
	}

	private void createWithProperties(
			final IResource resource, String[] attributeKeys, Object[] attributeValues)
			throws CoreException {
		if (resource.exists()) {
			IMarker marker = resource.createMarker(JSPARROW_MARKER);
			marker.setAttributes(attributeKeys, attributeValues);
		}
	}

	public IMarker[] find(IResource target) throws CoreException {
		return target.findMarkers(JSPARROW_MARKER, true, IResource.DEPTH_INFINITE);
	}

	public void clear(final IResource resource) throws CoreException {
		resource.deleteMarkers(JSPARROW_MARKER, true, IResource.DEPTH_INFINITE);
	}

}
